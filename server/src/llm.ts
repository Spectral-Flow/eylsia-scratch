import { logger } from './logger';

export interface LLMConfig {
  endpoint: string;
  model: string;
  enabled: boolean;
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface LLMResponse {
  success: boolean;
  message?: string;
  error?: string;
}

interface OllamaMessage {
  content: string;
}

interface OllamaResponse {
  message?: OllamaMessage;
}

interface OllamaModel {
  name: string;
}

interface OllamaTagsResponse {
  models?: OllamaModel[];
}

export class LLMService {
  private config: LLMConfig;

  constructor(config: LLMConfig) {
    this.config = config;
  }

  isAvailable(): boolean {
    return this.config.enabled;
  }

  async generateResponse(
    userMessage: string,
    conversationHistory: ChatMessage[] = []
  ): Promise<LLMResponse> {
    if (!this.isAvailable()) {
      return {
        success: false,
        error: 'LLM service is disabled',
      };
    }

    try {
      // Prepare messages for the LLM
      const messages: ChatMessage[] = [
        {
          role: 'system',
          content: 'You are a helpful assistant. Keep responses concise and friendly.',
        },
        ...conversationHistory.slice(-10), // Keep last 10 messages for context
        { role: 'user', content: userMessage },
      ];

      logger.info('Sending request to LLM', {
        endpoint: this.config.endpoint,
        model: this.config.model,
        messageCount: messages.length,
      });

      // Make request to Ollama API
      const response = await fetch(`${this.config.endpoint}/api/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          model: this.config.model,
          messages,
          stream: false,
        }),
      });

      if (!response.ok) {
        throw new Error(`LLM API error: ${response.status} ${response.statusText}`);
      }

      const data = (await response.json()) as OllamaResponse;

      if (data.message?.content) {
        logger.info('LLM response received', {
          responseLength: data.message.content.length,
        });

        return {
          success: true,
          message: data.message.content,
        };
      } else {
        throw new Error('Invalid response format from LLM');
      }
    } catch (error) {
      logger.error('LLM request failed', error);

      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown LLM error',
      };
    }
  }

  async checkHealth(): Promise<{ available: boolean; model?: string; error?: string }> {
    if (!this.isAvailable()) {
      return { available: false, error: 'LLM service is disabled' };
    }

    try {
      const response = await fetch(`${this.config.endpoint}/api/tags`, {
        method: 'GET',
      });

      if (!response.ok) {
        throw new Error(`Health check failed: ${response.status}`);
      }

      const data = (await response.json()) as OllamaTagsResponse;
      const hasModel = data.models?.some((m: OllamaModel) => m.name.includes(this.config.model));

      return {
        available: hasModel,
        model: this.config.model,
        error: hasModel ? undefined : `Model ${this.config.model} not found`,
      };
    } catch (error) {
      return {
        available: false,
        error: error instanceof Error ? error.message : 'Health check failed',
      };
    }
  }
}
