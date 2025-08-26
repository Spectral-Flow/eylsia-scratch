import type { Config } from './config';
import { logger } from './logger';

// We'll use dynamic imports to handle the ElevenLabs API more gracefully

/**
 * Voice synthesis request
 */
export interface VoiceSynthesisRequest {
  text: string;
  voiceId?: string;
  modelId?: string;
  voiceSettings?: {
    stability?: number;
    similarityBoost?: number;
    style?: number;
    useSpeakerBoost?: boolean;
  };
}

/**
 * Voice synthesis response
 */
export interface VoiceSynthesisResponse {
  success: boolean;
  audioUrl?: string;
  audioData?: Buffer;
  error?: string;
}

/**
 * Voice interface for ElevenLabs voices
 */
export interface Voice {
  voice_id: string;
  name: string;
  category: string;
  description: string;
}

/**
 * ElevenLabs API voice response interface
 */
interface ElevenLabsVoice {
  voice_id: string;
  name: string;
  category?: string;
  description?: string;
}

/**
 * ElevenLabs voices API response
 */
interface ElevenLabsVoicesResponse {
  voices: ElevenLabsVoice[];
}

/**
 * ElevenLabs service for voice synthesis and conversational AI
 * Enhanced with real API integration when API key is provided, with graceful fallback to mock
 */
export class ElevenLabsService {
  private config: Config['elevenLabs'];
  private isEnabled: boolean;
  private hasRealAPI: boolean = false;

  // Mock voices for development
  private mockVoices: Voice[] = [
    {
      voice_id: '21m00Tcm4TlvDq8ikWAM',
      name: 'Rachel',
      category: 'premade',
      description: 'A calm and soothing voice',
    },
    {
      voice_id: 'AZnzlk1XvdvUeBnXmlld',
      name: 'Domi',
      category: 'premade',
      description: 'A confident and engaging voice',
    },
    {
      voice_id: 'EXAVITQu4vr4xnSDxMaL',
      name: 'Bella',
      category: 'premade',
      description: 'A friendly and warm voice',
    },
  ];

  constructor(config: Config['elevenLabs']) {
    this.config = config;
    this.isEnabled = Boolean(config.apiKey);

    if (this.isEnabled) {
      // Check if we can use the real API
      this.checkAPIAvailability();
      logger.info('ElevenLabs service initialized - checking API availability');
    } else {
      logger.warn('ElevenLabs service running in mock mode - no API key provided');
    }
  }

  /**
   * Check if the real ElevenLabs API is available
   */
  private async checkAPIAvailability(): Promise<void> {
    try {
      // Try to make a simple API call to verify the key works
      const response = await fetch('https://api.elevenlabs.io/v1/voices', {
        headers: {
          'xi-api-key': this.config.apiKey as string,
        },
      });

      if (response.ok) {
        this.hasRealAPI = true;
        logger.info('ElevenLabs API key verified - real API available');
      } else {
        logger.warn('ElevenLabs API key verification failed - using mock mode');
      }
    } catch (error) {
      logger.warn('ElevenLabs API not accessible - using mock mode', error);
    }
  }

  /**
   * Check if the service is available
   */
  isAvailable(): boolean {
    return this.isEnabled;
  }

  /**
   * Convert text to speech using ElevenLabs API or mock implementation
   */
  async textToSpeech(request: VoiceSynthesisRequest): Promise<VoiceSynthesisResponse> {
    if (!this.isAvailable()) {
      return {
        success: false,
        error: 'ElevenLabs service is not available',
      };
    }

    // Use real API if available
    if (this.hasRealAPI) {
      return this.realTextToSpeech(request);
    } else {
      return this.mockTextToSpeech(request);
    }
  }

  /**
   * Real ElevenLabs API implementation using direct HTTP calls
   */
  private async realTextToSpeech(request: VoiceSynthesisRequest): Promise<VoiceSynthesisResponse> {
    try {
      const voiceId = request.voiceId || this.config.voiceId;

      logger.debug('Real text-to-speech conversion starting', {
        textLength: request.text.length,
        voiceId,
        hasVoiceSettings: !!request.voiceSettings,
      });

      const requestBody = {
        text: request.text,
        model_id: request.modelId || 'eleven_multilingual_v2',
        voice_settings: request.voiceSettings || {
          stability: 0.5,
          similarity_boost: 0.8,
          style: 0.0,
          use_speaker_boost: true,
        },
      };

      const response = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${voiceId}`, {
        method: 'POST',
        headers: {
          Accept: 'audio/mpeg',
          'Content-Type': 'application/json',
          'xi-api-key': this.config.apiKey as string,
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `ElevenLabs API error: ${response.status} ${response.statusText} - ${errorText}`
        );
      }

      const audioData = Buffer.from(await response.arrayBuffer());

      logger.info('Real text-to-speech conversion completed', {
        audioSize: audioData.length,
        voiceId,
      });

      return {
        success: true,
        audioData,
      };
    } catch (error) {
      logger.error('Real text-to-speech conversion failed', error);

      // Check if it's an API rate limit or auth error
      const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
      if (errorMessage.includes('quota') || errorMessage.includes('rate')) {
        return {
          success: false,
          error: 'ElevenLabs API quota exceeded or rate limited',
        };
      } else if (errorMessage.includes('unauthorized') || errorMessage.includes('authentication')) {
        return {
          success: false,
          error: 'ElevenLabs API authentication failed - check API key',
        };
      }

      return {
        success: false,
        error: errorMessage,
      };
    }
  }

  /**
   * Mock implementation for development/fallback
   */
  private async mockTextToSpeech(request: VoiceSynthesisRequest): Promise<VoiceSynthesisResponse> {
    try {
      logger.debug('Mock text-to-speech conversion', {
        textLength: request.text.length,
        voiceId: request.voiceId || this.config.voiceId,
      });

      // Simulate processing delay
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // Create a simple mock audio buffer (empty for now)
      const audioData = Buffer.alloc(1024, 0);

      logger.info('Mock text-to-speech conversion completed', {
        audioSize: audioData.length,
      });

      return {
        success: true,
        audioData,
      };
    } catch (error) {
      logger.error('Mock text-to-speech conversion failed', error);
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred',
      };
    }
  }

  /**
   * Get available voices from ElevenLabs API or return mock voices
   */
  async getVoices(): Promise<Voice[]> {
    if (!this.isAvailable()) {
      throw new Error('ElevenLabs service is not available');
    }

    // Use real API if available
    if (this.hasRealAPI) {
      return this.getRealVoices();
    } else {
      return this.getMockVoices();
    }
  }

  /**
   * Get voices from real ElevenLabs API
   */
  private async getRealVoices(): Promise<Voice[]> {
    try {
      logger.debug('Fetching voices from ElevenLabs API');

      const response = await fetch('https://api.elevenlabs.io/v1/voices', {
        headers: {
          'xi-api-key': this.config.apiKey as string,
        },
      });

      if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
      }

      const data = (await response.json()) as ElevenLabsVoicesResponse;

      const voices: Voice[] = data.voices.map((voice: ElevenLabsVoice) => ({
        voice_id: voice.voice_id,
        name: voice.name,
        category: voice.category || 'premade',
        description: voice.description || `${voice.name} voice`,
      }));

      logger.info('Retrieved voices from ElevenLabs API', { count: voices.length });
      return voices;
    } catch (error) {
      logger.warn('Failed to fetch voices from ElevenLabs API, falling back to mock', error);
      return this.getMockVoices();
    }
  }

  /**
   * Get mock voices for development
   */
  private async getMockVoices(): Promise<Voice[]> {
    logger.debug('Retrieved mock voices list', { count: this.mockVoices.length });
    return this.mockVoices;
  }

  /**
   * Stream text to speech with real-time audio generation
   * Note: For now, this uses the regular API and yields the result as chunks
   */
  async *streamTextToSpeech(text: string, voiceId?: string): AsyncGenerator<Buffer, void, unknown> {
    if (!this.isAvailable()) {
      throw new Error('ElevenLabs service is not available');
    }

    // Use real API streaming if available
    if (this.hasRealAPI) {
      yield* this.realStreamTextToSpeech(text, voiceId);
    } else {
      yield* this.mockStreamTextToSpeech(text, voiceId);
    }
  }

  /**
   * Real ElevenLabs streaming implementation
   * For now, gets the full audio and yields it in chunks
   */
  private async *realStreamTextToSpeech(
    text: string,
    voiceId?: string
  ): AsyncGenerator<Buffer, void, unknown> {
    try {
      const voice = voiceId || this.config.voiceId;

      logger.debug('Real streaming text-to-speech starting', {
        textLength: text.length,
        voiceId: voice,
      });

      // Get the full audio first (real streaming would require WebSocket or Server-Sent Events)
      const response = await this.realTextToSpeech({ text, voiceId: voice });

      if (response.success && response.audioData) {
        // Yield the audio in chunks to simulate streaming
        const chunkSize = 4096;
        const buffer = response.audioData;

        for (let i = 0; i < buffer.length; i += chunkSize) {
          const chunk = buffer.subarray(i, i + chunkSize);
          yield chunk;
          // Small delay to simulate streaming
          await new Promise((resolve) => setTimeout(resolve, 50));
        }
      } else {
        throw new Error(response.error || 'Text-to-speech failed');
      }

      logger.debug('Real streaming text-to-speech completed');
    } catch (error) {
      logger.error('Real streaming text-to-speech failed', error);
      throw error;
    }
  }

  /**
   * Mock streaming implementation for development
   */
  private async *mockStreamTextToSpeech(
    text: string,
    voiceId?: string
  ): AsyncGenerator<Buffer, void, unknown> {
    logger.debug('Mock streaming text-to-speech', {
      textLength: text.length,
      voiceId: voiceId || this.config.voiceId,
    });

    // Mock streaming by yielding chunks
    const chunkSize = 256;
    const totalChunks = 4;

    for (let i = 0; i < totalChunks; i++) {
      await new Promise((resolve) => setTimeout(resolve, 250));
      yield Buffer.alloc(chunkSize, i);
    }

    logger.debug('Mock streaming text-to-speech completed');
  }
}
