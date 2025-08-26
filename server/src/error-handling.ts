import { logger } from './logger';

/**
 * Custom error types for better error handling
 */
export class AppError extends Error {
  public readonly statusCode: number;
  public readonly isOperational: boolean;

  constructor(message: string, statusCode: number = 500, isOperational: boolean = true) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = isOperational;

    Error.captureStackTrace(this, this.constructor);
  }
}

export class ValidationError extends AppError {
  constructor(message: string) {
    super(message, 400);
  }
}

export class NotFoundError extends AppError {
  constructor(message: string = 'Resource not found') {
    super(message, 404);
  }
}

export class UnauthorizedError extends AppError {
  constructor(message: string = 'Unauthorized') {
    super(message, 401);
  }
}

export class RateLimitError extends AppError {
  constructor(message: string = 'Rate limit exceeded') {
    super(message, 429);
  }
}

export class ServiceUnavailableError extends AppError {
  constructor(message: string = 'Service temporarily unavailable') {
    super(message, 503);
  }
}

/**
 * Retry utility with exponential backoff
 */
export async function withRetry<T>(
  operation: () => Promise<T>,
  options: {
    maxRetries?: number;
    baseDelay?: number;
    maxDelay?: number;
    backoffFactor?: number;
    retryCondition?: (error: unknown) => boolean;
  } = {}
): Promise<T> {
  const {
    maxRetries = 3,
    baseDelay = 1000,
    maxDelay = 30000,
    backoffFactor = 2,
    retryCondition = (error) => !(error instanceof AppError && error.isOperational),
  } = options;

  let lastError: unknown;

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await operation();
    } catch (error) {
      lastError = error;

      if (attempt === maxRetries || !retryCondition(error)) {
        throw error;
      }

      const delay = Math.min(baseDelay * backoffFactor ** attempt, maxDelay);

      logger.warn(`Operation failed, retrying in ${delay}ms`, {
        attempt: attempt + 1,
        maxRetries,
        error: error instanceof Error ? error.message : String(error),
      });

      await new Promise((resolve) => setTimeout(resolve, delay));
    }
  }

  throw lastError;
}

/**
 * Circuit breaker for external services
 */
export class CircuitBreaker {
  private failureCount = 0;
  private lastFailureTime?: number;
  private state: 'CLOSED' | 'OPEN' | 'HALF_OPEN' = 'CLOSED';

  constructor(
    private failureThreshold: number = 5,
    private resetTimeout: number = 60000 // 1 minute
  ) {}

  async execute<T>(operation: () => Promise<T>): Promise<T> {
    if (this.state === 'OPEN') {
      if (this.shouldAttemptReset()) {
        this.state = 'HALF_OPEN';
      } else {
        throw new ServiceUnavailableError('Circuit breaker is OPEN');
      }
    }

    try {
      const result = await operation();
      this.onSuccess();
      return result;
    } catch (error) {
      this.onFailure();
      throw error;
    }
  }

  private onSuccess(): void {
    this.failureCount = 0;
    this.state = 'CLOSED';
  }

  private onFailure(): void {
    this.failureCount++;
    this.lastFailureTime = Date.now();

    if (this.failureCount >= this.failureThreshold) {
      this.state = 'OPEN';
      logger.warn('Circuit breaker opened due to failures', {
        failureCount: this.failureCount,
        threshold: this.failureThreshold,
      });
    }
  }

  private shouldAttemptReset(): boolean {
    return (
      this.lastFailureTime !== undefined && Date.now() - this.lastFailureTime >= this.resetTimeout
    );
  }

  getState() {
    return {
      state: this.state,
      failureCount: this.failureCount,
      lastFailureTime: this.lastFailureTime,
    };
  }
}

/**
 * Safe async wrapper that catches and logs errors
 */
export function safeAsync<T extends unknown[], R>(
  fn: (...args: T) => Promise<R>
): (...args: T) => Promise<R | undefined> {
  return async (...args: T): Promise<R | undefined> => {
    try {
      return await fn(...args);
    } catch (error) {
      logger.error('Async operation failed', error);
      return undefined;
    }
  };
}

/**
 * Timeout wrapper for operations
 */
export async function withTimeout<T>(
  operation: Promise<T>,
  timeoutMs: number,
  errorMessage: string = 'Operation timed out'
): Promise<T> {
  const timeoutPromise = new Promise<never>((_, reject) => {
    setTimeout(() => reject(new Error(errorMessage)), timeoutMs);
  });

  return Promise.race([operation, timeoutPromise]);
}

/**
 * Simple rate limiter for API endpoints
 */
export class RateLimiter {
  private requests = new Map<string, number[]>();

  constructor(
    private maxRequests: number = 100,
    private windowMs: number = 60000 // 1 minute
  ) {}

  /**
   * Check if request should be allowed
   * @param key - Usually IP address or user ID
   * @returns true if request is allowed, false if rate limited
   */
  isAllowed(key: string): boolean {
    const now = Date.now();
    const windowStart = now - this.windowMs;

    // Get existing requests for this key
    const requests = this.requests.get(key) || [];

    // Filter out old requests outside the window
    const recentRequests = requests.filter((time) => time > windowStart);

    // Check if under limit
    if (recentRequests.length >= this.maxRequests) {
      return false;
    }

    // Add current request
    recentRequests.push(now);
    this.requests.set(key, recentRequests);

    return true;
  }

  /**
   * Get remaining requests for a key
   */
  getRemaining(key: string): number {
    const now = Date.now();
    const windowStart = now - this.windowMs;
    const requests = this.requests.get(key) || [];
    const recentRequests = requests.filter((time) => time > windowStart);
    return Math.max(0, this.maxRequests - recentRequests.length);
  }

  /**
   * Clean up old entries to prevent memory leaks
   */
  cleanup(): void {
    const now = Date.now();
    const windowStart = now - this.windowMs;

    for (const [key, requests] of this.requests.entries()) {
      const recentRequests = requests.filter((time) => time > windowStart);
      if (recentRequests.length === 0) {
        this.requests.delete(key);
      } else {
        this.requests.set(key, recentRequests);
      }
    }
  }
}
