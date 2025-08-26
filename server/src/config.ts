/**
 * Application configuration
 */
export interface Config {
  port: number;
  nodeEnv: string;
  elevenLabs: {
    apiKey?: string;
    voiceId: string;
  };
  llm: {
    endpoint: string;
    model: string;
    enabled: boolean;
  };
  security: {
    allowedOrigins: string[];
  };
  logging: {
    level: string;
  };
}

/**
 * Load configuration from environment variables
 */
export function loadConfig(): Config {
  return {
    port: Number(process.env.PORT) || 3000,
    nodeEnv: process.env.NODE_ENV || 'development',
    elevenLabs: {
      apiKey: process.env.ELEVENLABS_API_KEY,
      voiceId: process.env.ELEVENLABS_VOICE_ID || '21m00Tcm4TlvDq8ikWAM',
    },
    llm: {
      endpoint: process.env.LLM_ENDPOINT || 'http://localhost:11434',
      model: process.env.LLM_MODEL || 'llama3.2',
      enabled: process.env.LLM_ENABLED !== 'false',
    },
    security: {
      allowedOrigins: process.env.ALLOWED_ORIGINS?.split(',') || [
        'http://localhost:3000',
        'http://localhost:3001',
        'https://*.vercel.app',
      ],
    },
    logging: {
      level: process.env.LOG_LEVEL || 'info',
    },
  };
}

/**
 * Validate required configuration
 */
export function validateConfig(config: Config): void {
  const errors: string[] = [];
  const warnings: string[] = [];

  // Port validation
  if (!config.port || config.port < 1 || config.port > 65535) {
    errors.push('PORT must be a valid port number (1-65535)');
  }

  // Production-specific validations
  if (config.nodeEnv === 'production') {
    if (!config.elevenLabs.apiKey) {
      warnings.push('ELEVENLABS_API_KEY not set - voice features will be disabled in production');
    }

    if (config.logging.level === 'debug') {
      warnings.push('Debug logging enabled in production - consider using "info" or "warn"');
    }
  }

  // Security validations
  if (config.security.allowedOrigins.includes('*')) {
    warnings.push('CORS configured to allow all origins - consider restricting for security');
  }

  // LLM endpoint validation
  if (config.llm.enabled && !config.llm.endpoint) {
    errors.push('LLM_ENDPOINT is required when LLM is enabled');
  }

  // Log warnings
  if (warnings.length > 0) {
    console.warn('Configuration warnings:');
    for (const warning of warnings) {
      console.warn(`  - ${warning}`);
    }
  }

  // Throw errors
  if (errors.length > 0) {
    throw new Error(`Configuration validation failed:\n${errors.join('\n')}`);
  }
}
