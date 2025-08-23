import { cors } from '@elysiajs/cors';
import { swagger } from '@elysiajs/swagger';
import { Elysia, t } from 'elysia';

const app = new Elysia()
  .use(
    cors({
      origin: true, // Allow all origins for development
      credentials: true,
    })
  )
  .use(
    swagger({
      documentation: {
        info: {
          title: 'Elysia Demo API',
          version: '1.0.0',
          description: 'A demo API built with Elysia and Bun',
        },
      },
    })
  )
  // Request ID middleware
  .derive((context) => ({
    requestId: context.headers['x-request-id'] || crypto.randomUUID(),
  }))
  // Logging middleware
  .onBeforeHandle((context) => {
    console.log(
      `[${context.requestId}] ${context.request.method} ${context.request.url} - ${new Date().toISOString()}`
    );
  })
  .onAfterHandle((context) => {
    console.log(
      `[${context.requestId}] Response: ${context.set.status || 200} - ${new Date().toISOString()}`
    );
  })
  // Error handling
  .onError((context) => {
    console.error(`[${context.requestId}] Error ${context.code}:`, context.error);
    return {
      error: true,
      message: context.code === 'VALIDATION' ? 'Invalid request data' : 'Internal server error',
      requestId: context.requestId,
      timestamp: new Date().toISOString(),
    };
  })
  // Auth middleware (simple bearer token stub)
  .derive((context) => ({
    user: context.headers.authorization === 'Bearer dev' ? { id: '1', name: 'Dev User' } : null,
  }))
  // Utility decorators
  .decorate('now', () => new Date().toISOString())
  // Health endpoint
  .get('/health', (context) => ({
    status: 'ok',
    timestamp: context.now(),
    requestId: context.requestId,
    service: 'elysia-demo',
    uptime: process.uptime(),
  }))
  // Hello API endpoint with validation
  .get(
    '/api/hello',
    (context) => {
      const name = context.query.name || 'World';
      return {
        message: `Hello, ${name}!`,
        timestamp: context.now(),
        requestId: context.requestId,
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
  // WebSocket for chat
  .ws('/ws', {
    open(ws) {
      console.log('WebSocket connection opened');
      ws.subscribe('chat');
      ws.send(
        JSON.stringify({
          type: 'system',
          message: 'Connected to chat server',
          timestamp: new Date().toISOString(),
        })
      );
    },
    message(ws, message) {
      const messageStr = String(message);
      console.log('WebSocket message received:', messageStr);

      const payload = {
        type: 'message',
        from: 'client',
        text: messageStr,
        timestamp: new Date().toISOString(),
      };

      // Broadcast to all connected clients
      ws.publish('chat', JSON.stringify(payload));
    },
    close() {
      console.log('WebSocket connection closed');
    },
  })
  .listen(3000);

console.log(`🚀 Elysia server is running at http://localhost:${app.server?.port}`);
console.log(`📖 Swagger documentation available at http://localhost:${app.server?.port}/swagger`);
