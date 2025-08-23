import { Elysia } from 'elysia';
import { cors } from '@elysiajs/cors';
import { bearer } from '@elysiajs/bearer';
import { swagger } from '@elysiajs/swagger';
import { routes } from './routes';
import { wsHandler } from './ws';

const app = new Elysia()
  // CORS
  .use(cors({
    origin: true, // Allow all origins for development (localhost, emulator)
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true,
  }))
  
  // Auth middleware
  .use(bearer())
  .derive(({ bearer }) => ({
    isAuthenticated: bearer === 'dev',
    user: bearer === 'dev' ? { id: 'dev-user', name: 'Developer' } : null,
  }))
  
  // Basic logging
  .onBeforeHandle(({ request }) => {
    console.log(`${request.method} ${request.url}`);
  })
  
  // Documentation
  .use(swagger({
    documentation: {
      info: {
        title: 'Elysia Scratch API',
        version: '1.0.0',
        description: 'Demo API with REST and WebSocket endpoints',
      },
      tags: [
        { name: 'Health', description: 'Health check endpoints' },
        { name: 'API', description: 'Main API endpoints' },
      ],
    },
  }))
  
  // Routes
  .use(routes)
  .use(wsHandler)
  
  // Global error handler
  .onError(({ code, error, set }) => {
    console.error('Global error:', error);
    
    switch (code) {
      case 'NOT_FOUND':
        set.status = 404;
        return { error: 'Not Found', message: 'The requested resource was not found' };
      case 'VALIDATION':
        set.status = 400;
        return { error: 'Validation Error', message: error.message };
      case 'INTERNAL_SERVER_ERROR':
        set.status = 500;
        return { error: 'Internal Server Error', message: 'Something went wrong' };
      default:
        set.status = 500;
        return { error: 'Unknown Error', message: 'An unexpected error occurred' };
    }
  })
  
  .listen(3000);

console.log(`🦊 Elysia server running at http://localhost:${app.server?.port}`);
console.log('📚 API Documentation: http://localhost:3000/swagger');