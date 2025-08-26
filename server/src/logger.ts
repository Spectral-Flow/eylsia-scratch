/**
 * Simple logger utility with different levels and basic metrics
 */
export class Logger {
  private level: string;
  private readonly levels = ['debug', 'info', 'warn', 'error'];
  private metrics = {
    debug: 0,
    info: 0,
    warn: 0,
    error: 0,
  };

  constructor(level: string = 'info') {
    this.level = level.toLowerCase();
  }

  private shouldLog(level: string): boolean {
    const currentIndex = this.levels.indexOf(this.level);
    const messageIndex = this.levels.indexOf(level);
    return messageIndex >= currentIndex;
  }

  private formatMessage(level: string, message: string, meta?: unknown): string {
    const timestamp = new Date().toISOString();
    const metaStr = meta ? ` ${JSON.stringify(meta)}` : '';
    return `[${timestamp}] [${level.toUpperCase()}] ${message}${metaStr}`;
  }

  debug(message: string, meta?: unknown): void {
    if (this.shouldLog('debug')) {
      this.metrics.debug++;
      console.log(this.formatMessage('debug', message, meta));
    }
  }

  info(message: string, meta?: unknown): void {
    if (this.shouldLog('info')) {
      this.metrics.info++;
      console.log(this.formatMessage('info', message, meta));
    }
  }

  warn(message: string, meta?: unknown): void {
    if (this.shouldLog('warn')) {
      this.metrics.warn++;
      console.warn(this.formatMessage('warn', message, meta));
    }
  }

  error(message: string, error?: Error | unknown): void {
    if (this.shouldLog('error')) {
      this.metrics.error++;
      const meta =
        error instanceof Error
          ? {
              message: error.message,
              stack: error.stack,
            }
          : error;
      console.error(this.formatMessage('error', message, meta));
    }
  }

  /**
   * Get logging metrics for monitoring
   */
  getMetrics() {
    return { ...this.metrics };
  }

  /**
   * Reset metrics counters
   */
  resetMetrics() {
    this.metrics = {
      debug: 0,
      info: 0,
      warn: 0,
      error: 0,
    };
  }
}

// Global logger instance
export const logger = new Logger();
