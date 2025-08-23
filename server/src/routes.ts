import { Elysia, t } from 'elysia';

export const routes = new Elysia({ prefix: '/api' })
  // Health check endpoint
  .get('/health', () => ({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  }), {
    detail: {
      tags: ['Health'],
      summary: 'Health check',
      description: 'Returns server health status and uptime',
    },
  })
  
  // Hello endpoint with query validation
  .get('/hello', ({ query: { name = 'World' } }) => ({
    message: `Hello, ${name}!`,
    timestamp: new Date().toISOString(),
  }), {
    query: t.Object({
      name: t.Optional(t.String({ 
        minLength: 1, 
        maxLength: 100,
        description: 'Name to greet',
        examples: ['World', 'Richie'],
      })),
    }),
    detail: {
      tags: ['API'],
      summary: 'Hello greeting',
      description: 'Returns a greeting message with optional name parameter',
    },
  })
  
  // Simple protected endpoint (check for bearer token in Authorization header)
  .get('/protected', ({ headers }) => {
    const authHeader = headers.authorization;
    if (!authHeader || !authHeader.includes('Bearer dev')) {
      return {
        error: 'Unauthorized', 
        message: 'Bearer token "dev" required'
      };
    }
    
    return {
      message: 'Hello, Developer! This is a protected endpoint.',
      userId: 'dev-user',
      timestamp: new Date().toISOString(),
    };
  }, {
    detail: {
      tags: ['API'],
      summary: 'Protected endpoint',
      description: 'Example protected endpoint requiring bearer token "dev"',
      security: [{ bearerAuth: [] }],
    },
  });