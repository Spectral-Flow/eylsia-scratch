"use client";

import { useState, useEffect, useRef } from 'react';

interface Message {
  id: string;
  type: string;
  from: 'user' | 'assistant';
  text: string;
  timestamp: string;
}

export default function Home() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const connectWebSocket = () => {
    if (isConnecting || isConnected) return;
    
    setIsConnecting(true);
    
    // Use environment variable for WebSocket URL, fallback to localhost
    const wsUrl = process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:3000/ws';
    
    try {
      const ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        setIsConnected(true);
        setIsConnecting(false);
        console.log('WebSocket connected');
      };

      ws.onmessage = (event) => {
        try {
          console.log('Raw WebSocket message:', event.data);
          const data = JSON.parse(event.data);
          console.log('Parsed WebSocket data:', data);
          
          if (data.type === 'message') {
            setMessages(prev => [...prev, {
              id: data.id || Date.now().toString(),
              type: data.type,
              from: data.from,
              text: data.text,
              timestamp: data.timestamp
            }]);
          } else if (data.type === 'welcome') {
            // Handle welcome message if needed
            console.log('Welcome:', data.message);
          } else if (data.type === 'history') {
            // Handle chat history
            setMessages(data.messages || []);
          } else if (data.type === 'error') {
            // Handle errors from the server
            console.error('Server error:', data.message);
            setMessages(prev => [...prev, {
              id: Date.now().toString(),
              type: 'error',
              from: 'assistant',
              text: `Error: ${data.message}`,
              timestamp: new Date().toISOString()
            }]);
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onclose = () => {
        setIsConnected(false);
        setIsConnecting(false);
        wsRef.current = null;
        console.log('WebSocket disconnected');
      };

      ws.onerror = (error) => {
        setIsConnected(false);
        setIsConnecting(false);
        console.error('WebSocket error:', error);
      };
    } catch (error) {
      setIsConnecting(false);
      console.error('Failed to connect WebSocket:', error);
    }
  };

  const disconnectWebSocket = () => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    setIsConnected(false);
  };

  const sendMessage = () => {
    if (!inputMessage.trim() || !isConnected || !wsRef.current) return;

    const message = {
      type: 'text',
      content: inputMessage.trim()
    };

    console.log('Sending message:', message);
    console.log('Message content type:', typeof message.content);
    console.log('Message content value:', message.content);
    console.log('Stringified message:', JSON.stringify(message));

    wsRef.current.send(JSON.stringify(message));
    setInputMessage('');
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header */}
      <header className="bg-white shadow-sm border-b p-4">
        <div className="max-w-4xl mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">
            AI Chat with Local LLM
          </h1>
          <div className="flex items-center gap-4">
            <div className={`px-3 py-1 rounded-full text-sm font-medium ${
              isConnected 
                ? 'bg-green-100 text-green-800' 
                : 'bg-red-100 text-red-800'
            }`}>
              {isConnected ? 'Connected' : 'Disconnected'}
            </div>
            {!isConnected ? (
              <button
                onClick={connectWebSocket}
                disabled={isConnecting}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {isConnecting ? 'Connecting...' : 'Connect'}
              </button>
            ) : (
              <button
                onClick={disconnectWebSocket}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
              >
                Disconnect
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Chat Messages */}
      <main className="flex-1 max-w-4xl mx-auto w-full p-4">
        <div className="bg-white rounded-lg shadow-sm border h-96 flex flex-col">
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 ? (
              <div className="text-center text-gray-500 mt-8">
                <p>No messages yet. Connect and start chatting!</p>
              </div>
            ) : (
              messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${message.from === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                      message.from === 'user'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-200 text-gray-900'
                    }`}
                  >
                    <p className="whitespace-pre-wrap">{message.text}</p>
                    <p className={`text-xs mt-1 ${
                      message.from === 'user' ? 'text-blue-100' : 'text-gray-500'
                    }`}>
                      {new Date(message.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              ))
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Message Input */}
          <div className="border-t p-4">
            <div className="flex gap-2">
              <textarea
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder={isConnected ? "Type your message..." : "Connect to start chatting"}
                disabled={!isConnected}
                className="flex-1 resize-none border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                rows={1}
              />
              <button
                onClick={sendMessage}
                disabled={!isConnected || !inputMessage.trim()}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Send
              </button>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t p-4 text-center text-gray-600">
        <p>Connected to Elysia backend with local LLM integration</p>
      </footer>
    </div>
  );
}
