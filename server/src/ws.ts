import { Elysia } from 'elysia';

// Simple in-memory store for connected clients
const clients = new Set<any>();

// Chat message type
interface ChatMessage {
  from: string;
  text: string;
  timestamp: string;
}

export const wsHandler = new Elysia()
  .ws('/ws', {
    open(ws) {
      clients.add(ws);
      console.log(`WebSocket client connected. Total clients: ${clients.size}`);
      
      // Send welcome message
      ws.send(JSON.stringify({
        type: 'system',
        message: 'Connected to chat server',
        timestamp: new Date().toISOString(),
      }));
    },
    
    message(ws, message) {
      try {
        const data = typeof message === 'string' ? JSON.parse(message) : message;
        
        // Validate message structure
        if (!data.from || !data.text) {
          ws.send(JSON.stringify({
            type: 'error',
            message: 'Invalid message format. Required: { from, text }',
            timestamp: new Date().toISOString(),
          }));
          return;
        }
        
        // Create chat message
        const chatMessage: ChatMessage = {
          from: data.from,
          text: data.text,
          timestamp: new Date().toISOString(),
        };
        
        console.log(`Chat message from ${chatMessage.from}: ${chatMessage.text}`);
        
        // Broadcast to all connected clients
        const messagePayload = JSON.stringify({
          type: 'chat',
          ...chatMessage,
        });
        
        clients.forEach((client) => {
          try {
            client.send(messagePayload);
          } catch (error) {
            console.error('Error sending message to client:', error);
            clients.delete(client);
          }
        });
        
      } catch (error) {
        console.error('Error processing WebSocket message:', error);
        ws.send(JSON.stringify({
          type: 'error',
          message: 'Failed to process message',
          timestamp: new Date().toISOString(),
        }));
      }
    },
    
    close(ws) {
      clients.delete(ws);
      console.log(`WebSocket client disconnected. Total clients: ${clients.size}`);
    },
    
    error(error) {
      console.error('WebSocket error:', error);
    },
  });