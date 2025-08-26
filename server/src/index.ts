import { cors } from '@elysiajs/cors';
import { swagger } from '@elysiajs/swagger';
import { Elysia, t } from 'elysia';
import { loadConfig, validateConfig } from './config';
import { ElevenLabsService, type Voice } from './elevenlabs';
import {
  AppError,
  CircuitBreaker,
  NotFoundError,
  RateLimiter,
  ValidationError,
  withRetry,
  withTimeout,
} from './error-handling';
import { type ChatMessage, LLMService } from './llm';
import { logger } from './logger';

// TypeScript interfaces for WebSocket and Chat
interface WebSocketClient {
  send: (data: string) => void;
  close?: () => void;
  readyState?: number;
}

interface ChatHistoryMessage {
  id: string;
  from: 'user' | 'assistant';
  text: string;
  timestamp: string;
  type?: string;
}

interface WebSocketMessage {
  type: 'text' | 'voice-request' | 'voice-response';
  content: string;
  voiceId?: string;
  originalMessageId?: string;
  timestamp?: string;
  audioData?: string;
}

// Load and validate configuration
const config = loadConfig();
validateConfig(config);

// Initialize logger with configured level
logger.info('Starting Elysia server', {
  nodeEnv: config.nodeEnv,
  port: config.port,
});

// Initialize services
const elevenLabsService = new ElevenLabsService(config.elevenLabs);
const elevenLabsCircuitBreaker = new CircuitBreaker(5, 60000);
const llmService = new LLMService(config.llm);
const llmCircuitBreaker = new CircuitBreaker(3, 30000);

// Initialize rate limiter for production
const rateLimiter = new RateLimiter(
  config.nodeEnv === 'production' ? 100 : 1000, // Stricter limits in production
  60000 // 1 minute window
);

// Cleanup rate limiter every 5 minutes
setInterval(() => rateLimiter.cleanup(), 300000);

// Store for connected WebSocket clients and chat history
const connectedClients = new Set<WebSocketClient>();
const chatHistory: ChatHistoryMessage[] = [];
const MAX_CHAT_HISTORY = 100;

const app = new Elysia()
  .use(
    cors({
      origin: config.security.allowedOrigins,
      credentials: true,
    })
  )
  .use(
    swagger({
      documentation: {
        info: {
          title: 'Elysia Demo API with Voice Integration',
          version: '2.0.0',
          description: 'A demo API built with Elysia, Bun, and ElevenLabs voice integration',
        },
        servers: [
          {
            url: `http://localhost:${config.port}`,
            description: 'Development server',
          },
        ],
      },
    })
  )
  // Request ID middleware
  .derive((context) => ({
    requestId: context.headers['x-request-id'] || crypto.randomUUID(),
    startTime: Date.now(),
  }))
  // Rate limiting middleware
  .onBeforeHandle((context) => {
    // Skip rate limiting for health checks
    if (context.request.url.includes('/health')) {
      return;
    }

    const clientIP =
      context.headers['x-forwarded-for'] || context.headers['x-real-ip'] || 'unknown';

    if (!rateLimiter.isAllowed(clientIP as string)) {
      logger.warn('Rate limit exceeded', {
        clientIP,
        requestId: context.requestId,
        url: context.request.url,
      });

      context.set.status = 429;
      context.set.headers = {
        'X-RateLimit-Remaining': '0',
        'Retry-After': '60',
      };

      return {
        error: true,
        message: 'Too many requests. Please try again later.',
        requestId: context.requestId,
        timestamp: new Date().toISOString(),
      };
    }

    // Add rate limit headers
    const remaining = rateLimiter.getRemaining(clientIP as string);
    context.set.headers = {
      'X-RateLimit-Remaining': remaining.toString(),
    };
  })
  // Logging middleware
  .onBeforeHandle((context) => {
    logger.info('Request received', {
      requestId: context.requestId,
      method: context.request.method,
      url: context.request.url,
      userAgent: context.headers['user-agent'],
    });
  })
  .onAfterHandle((context) => {
    logger.info('Request completed', {
      requestId: context.requestId,
      status: context.set.status || 200,
      duration: Date.now() - context.startTime,
    });
  })
  // Enhanced error handling
  .onError((context) => {
    const { error, code, requestId } = context;

    if (error instanceof AppError) {
      logger.warn('Application error', {
        requestId,
        error: error.message,
        statusCode: error.statusCode,
      });

      context.set.status = error.statusCode;
      return {
        error: true,
        message: error.message,
        requestId,
        timestamp: new Date().toISOString(),
      };
    }

    logger.error('Unexpected error', {
      requestId,
      code,
      error:
        error instanceof Error
          ? {
              message: error.message,
              stack: error.stack,
            }
          : error,
    });

    const statusCode = code === 'VALIDATION' ? 400 : 500;
    const message = code === 'VALIDATION' ? 'Invalid request data' : 'Internal server error';

    context.set.status = statusCode;
    return {
      error: true,
      message,
      requestId,
      timestamp: new Date().toISOString(),
    };
  })
  // Auth middleware (enhanced)
  .derive((context) => ({
    user: context.headers.authorization === 'Bearer dev' ? { id: '1', name: 'Dev User' } : null,
  }))
  // Utility decorators
  .decorate('now', () => new Date().toISOString())

  // Health endpoint with enhanced checks
  .get('/health', async (context) => {
    const llmHealth = await llmService.checkHealth();

    const health = {
      status: 'ok',
      timestamp: context.now(),
      requestId: context.requestId,
      service: 'elysia-demo',
      version: '2.0.0',
      uptime: process.uptime(),
      services: {
        elevenLabs: {
          available: elevenLabsService.isAvailable(),
          circuitBreaker: elevenLabsCircuitBreaker.getState(),
        },
        llm: {
          available: llmHealth.available,
          model: llmHealth.model,
          error: llmHealth.error,
          circuitBreaker: llmCircuitBreaker.getState(),
        },
      },
      system: {
        nodeVersion: process.version,
        platform: process.platform,
        memory: process.memoryUsage(),
      },
      logging: {
        metrics: logger.getMetrics(),
      },
    };

    logger.debug('Health check completed', health);
    return health;
  })

  // Hello API endpoint (enhanced with validation)
  .get(
    '/api/hello',
    (context) => {
      const name = context.query.name || 'World';

      logger.debug('Hello API called', { name, requestId: context.requestId });

      return {
        message: `Hello, ${name}!`,
        timestamp: context.now(),
        requestId: context.requestId,
        voiceEnabled: elevenLabsService.isAvailable(),
      };
    },
    {
      query: t.Object({
        name: t.Optional(
          t.String({
            minLength: 1,
            maxLength: 50,
            description: 'Name to greet',
          })
        ),
      }),
    }
  )

  // Voice synthesis endpoint
  .post(
    '/api/voice/synthesize',
    async (context) => {
      const { text, voiceId, voiceSettings } = context.body;

      if (!elevenLabsService.isAvailable()) {
        throw new ValidationError('Voice synthesis service is not available');
      }

      if (!text || typeof text !== 'string') {
        throw new ValidationError('Text is required and must be a string');
      }

      if (text.length > 500) {
        throw new ValidationError('Text must be 500 characters or less');
      }

      logger.info('Voice synthesis requested', {
        requestId: context.requestId,
        textLength: text.length,
        voiceId,
      });

      try {
        const result = await elevenLabsCircuitBreaker.execute(async () => {
          return await withTimeout(
            elevenLabsService.textToSpeech({
              text,
              voiceId,
              voiceSettings,
            }),
            30000, // 30 second timeout
            'Voice synthesis timed out'
          );
        });

        if (!result.success) {
          throw new AppError(result.error || 'Voice synthesis failed', 500);
        }

        // Set response headers for audio
        context.set.headers['Content-Type'] = 'audio/mpeg';
        context.set.headers['Content-Disposition'] = 'attachment; filename="speech.mp3"';

        return result.audioData;
      } catch (error) {
        logger.error('Voice synthesis failed', {
          requestId: context.requestId,
          error,
        });
        throw error;
      }
    },
    {
      body: t.Object({
        text: t.String({
          minLength: 1,
          maxLength: 500,
          description: 'Text to convert to speech',
        }),
        voiceId: t.Optional(
          t.String({
            description: 'ElevenLabs voice ID',
          })
        ),
        voiceSettings: t.Optional(
          t.Object({
            stability: t.Optional(t.Number({ minimum: 0, maximum: 1 })),
            similarityBoost: t.Optional(t.Number({ minimum: 0, maximum: 1 })),
            style: t.Optional(t.Number({ minimum: 0, maximum: 1 })),
            useSpeakerBoost: t.Optional(t.Boolean()),
          })
        ),
      }),
    }
  )

  // Get available voices
  .get('/api/voice/voices', async (context) => {
    if (!elevenLabsService.isAvailable()) {
      throw new ValidationError('Voice service is not available');
    }

    try {
      const voices = await withRetry(() => elevenLabsService.getVoices(), {
        maxRetries: 2,
        baseDelay: 1000,
      });

      return {
        voices: voices.map((voice: Voice) => ({
          id: voice.voice_id,
          name: voice.name,
          category: voice.category,
          description: voice.description,
        })),
        requestId: context.requestId,
        timestamp: context.now(),
      };
    } catch (error) {
      logger.error('Failed to fetch voices', {
        requestId: context.requestId,
        error,
      });
      throw new AppError('Failed to fetch available voices', 500);
    }
  })

  // WebSocket for enhanced chat with voice
  .ws('/ws', {
    open(ws) {
      connectedClients.add(ws);

      logger.info('WebSocket connection opened', {
        clientsCount: connectedClients.size,
      });

      // Send welcome message with chat history
      ws.send(
        JSON.stringify({
          type: 'system',
          message: 'Connected to enhanced chat server with voice support',
          timestamp: new Date().toISOString(),
          features: {
            voiceEnabled: elevenLabsService.isAvailable(),
            chatHistory: true,
          },
        })
      );

      // Send recent chat history
      if (chatHistory.length > 0) {
        ws.send(
          JSON.stringify({
            type: 'history',
            messages: chatHistory.slice(-10), // Last 10 messages
            timestamp: new Date().toISOString(),
          })
        );
      }
    },

    async message(ws, message) {
      // Handle different message types from Elysia WebSocket
      let messageStr: string;

      logger.info('Raw WebSocket message received', {
        messageType: typeof message,
        messageConstructor: message?.constructor?.name,
        messageKeys: message && typeof message === 'object' ? Object.keys(message) : null,
        message: typeof message === 'object' ? JSON.stringify(message) : message,
      });

      if (typeof message === 'string') {
        messageStr = message;
      } else if (message instanceof Buffer) {
        messageStr = message.toString('utf8');
      } else if (typeof message === 'object' && message !== null) {
        // If it's an object, try to find the actual message content
        if ('data' in message) {
          messageStr = String(message.data);
        } else if ('text' in message) {
          messageStr = String(message.text);
        } else if ('content' in message) {
          messageStr = String(message.content);
        } else {
          // As fallback, try to stringify the entire object
          messageStr = JSON.stringify(message);
        }
      } else {
        messageStr = String(message);
      }

      logger.info('Processed WebSocket message', {
        originalType: typeof message,
        messageStr,
        messageLength: messageStr.length,
      });

      try {
        // Try to parse as JSON for enhanced messages
        let parsedMessage: WebSocketMessage;
        try {
          parsedMessage = JSON.parse(messageStr) as WebSocketMessage;
        } catch {
          // Fallback to plain text
          parsedMessage = { type: 'text', content: messageStr };
        }

        logger.info('WebSocket message received', {
          type: parsedMessage.type || 'text',
          contentLength: parsedMessage.content?.length || messageStr.length,
          parsedMessageContent: parsedMessage.content,
          messageStr: messageStr,
        });

        const userMessage: ChatHistoryMessage = {
          type: 'message',
          from: 'user' as const,
          text: parsedMessage.content || messageStr,
          timestamp: new Date().toISOString(),
          id: crypto.randomUUID(),
        };

        logger.info('Created user message', {
          userMessage,
          textType: typeof userMessage.text,
        });

        // Add user message to chat history
        chatHistory.push(userMessage);
        if (chatHistory.length > MAX_CHAT_HISTORY) {
          chatHistory.shift();
        }

        // Broadcast user message to all connected clients
        const broadcastUserMessage = JSON.stringify(userMessage);
        connectedClients.forEach((client) => {
          try {
            client.send(broadcastUserMessage);
          } catch (error) {
            logger.warn('Failed to send message to client', error);
            connectedClients.delete(client);
          }
        });

        // Generate LLM response if available
        if (llmService.isAvailable()) {
          try {
            // Convert chat history to LLM format for context
            const conversationHistory: ChatMessage[] = chatHistory
              .slice(-10) // Last 10 messages for context
              .map((msg: ChatHistoryMessage) => ({
                role: (msg.from === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
                content: msg.text,
              }))
              .slice(0, -1); // Remove the current message since we're adding it separately

            const llmResponse = await llmCircuitBreaker.execute(async () => {
              return await llmService.generateResponse(
                parsedMessage.content || messageStr,
                conversationHistory
              );
            });

            if (llmResponse.success && llmResponse.message) {
              const assistantMessage: ChatHistoryMessage = {
                type: 'message',
                from: 'assistant' as const,
                text: llmResponse.message,
                timestamp: new Date().toISOString(),
                id: crypto.randomUUID(),
              };

              // Add assistant message to chat history
              chatHistory.push(assistantMessage);
              if (chatHistory.length > MAX_CHAT_HISTORY) {
                chatHistory.shift();
              }

              // Broadcast assistant response to all connected clients
              const broadcastAssistantMessage = JSON.stringify(assistantMessage);
              connectedClients.forEach((client) => {
                try {
                  client.send(broadcastAssistantMessage);
                } catch (error) {
                  logger.warn('Failed to send message to client', error);
                  connectedClients.delete(client);
                }
              });

              // If voice synthesis is requested and available for the assistant response
              if (
                parsedMessage.type === 'voice-request' &&
                elevenLabsService.isAvailable() &&
                llmResponse.message
              ) {
                try {
                  const voiceResponse = await elevenLabsCircuitBreaker.execute(async () => {
                    return await elevenLabsService.textToSpeech({
                      text: llmResponse.message as string, // Safe because we checked above
                      voiceId: parsedMessage.voiceId,
                    });
                  });

                  if (voiceResponse.success && voiceResponse.audioData) {
                    // Send voice response back to requesting client
                    ws.send(
                      JSON.stringify({
                        type: 'voice-response',
                        audioData: voiceResponse.audioData.toString('base64'),
                        originalMessageId: assistantMessage.id,
                        timestamp: new Date().toISOString(),
                      })
                    );
                  }
                } catch (error) {
                  logger.error('Voice synthesis for WebSocket failed', error);
                  ws.send(
                    JSON.stringify({
                      type: 'voice-error',
                      error: 'Voice synthesis failed',
                      originalMessageId: assistantMessage.id,
                      timestamp: new Date().toISOString(),
                    })
                  );
                }
              }
            } else {
              // Send error response if LLM failed
              ws.send(
                JSON.stringify({
                  type: 'error',
                  message: llmResponse.error || 'Failed to generate response',
                  timestamp: new Date().toISOString(),
                })
              );
            }
          } catch (error) {
            logger.error('LLM response generation failed', error);
            ws.send(
              JSON.stringify({
                type: 'error',
                message: 'Failed to generate AI response',
                timestamp: new Date().toISOString(),
              })
            );
          }
        }
      } catch (error) {
        logger.error('WebSocket message processing failed', error);
        ws.send(
          JSON.stringify({
            type: 'error',
            message: 'Failed to process message',
            timestamp: new Date().toISOString(),
          })
        );
      }
    },

    close(ws) {
      connectedClients.delete(ws);
      logger.info('WebSocket connection closed', {
        clientsCount: connectedClients.size,
      });
    },
  })

  // 404 handler
  .all('*', () => {
    throw new NotFoundError('Endpoint not found');
  })

  .listen(config.port);

// Graceful shutdown handling
const gracefulShutdown = () => {
  logger.info('Shutting down gracefully...');

  // Close all WebSocket connections
  connectedClients.forEach((client) => {
    try {
      client.send(
        JSON.stringify({
          type: 'system',
          message: 'Server shutting down',
          timestamp: new Date().toISOString(),
        })
      );
      client.close?.();
    } catch (_error) {
      // Ignore errors during shutdown
    }
  });

  // Close the server
  app.server?.stop();
  process.exit(0);
};

process.on('SIGTERM', gracefulShutdown);
process.on('SIGINT', gracefulShutdown);

logger.info('Server started successfully', {
  url: `http://localhost:${app.server?.port}`,
  swagger: `http://localhost:${app.server?.port}/swagger`,
  features: {
    voiceEnabled: elevenLabsService.isAvailable(),
    llmEnabled: llmService.isAvailable(),
    environment: config.nodeEnv,
  },
});

console.log(`🚀 Elysia server is running at http://localhost:${app.server?.port}`);
console.log(`📖 Swagger documentation available at http://localhost:${app.server?.port}/swagger`);
console.log(
  `🎤 Voice synthesis: ${elevenLabsService.isAvailable() ? 'ENABLED' : 'DISABLED (no API key)'}`
);
console.log(
  `🤖 Local LLM: ${llmService.isAvailable() ? `ENABLED (${config.llm.model})` : 'DISABLED'}`
);
