#!/bin/bash

# Elysia Voice App Demo Script
echo "🎤 Elysia Voice-Enhanced Demo"
echo "============================="

# Check if server is running
if ! curl -s http://localhost:3000/health > /dev/null; then
    echo "❌ Server not running. Start with: cd server && bun run dev"
    exit 1
fi

echo "✅ Server is running!"
echo ""

# Test basic health endpoint
echo "📊 Server Health Status:"
curl -s http://localhost:3000/health | jq '{status, version, uptime, services}'
echo ""

# Test enhanced hello endpoint
echo "👋 Enhanced Hello API:"
curl -s "http://localhost:3000/api/hello?name=VoiceDemo" | jq '{message, voiceEnabled}'
echo ""

# Test voice service availability
echo "🎤 Voice Service Status:"
curl -s http://localhost:3000/api/voice/voices 2>/dev/null | jq '.voices // {error: "Voice service not available"}'
echo ""

# Test voice synthesis (this will fail without API key, which is expected)
echo "🔊 Testing Voice Synthesis:"
curl -s -X POST http://localhost:3000/api/voice/synthesize \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello from Elysia with voice integration!"}' | \
  jq '{error, message}' 2>/dev/null || echo "Voice synthesis requires ElevenLabs API key"
echo ""

echo "📱 Android App Features:"
echo "   - Voice synthesis controls in chat interface"
echo "   - Real-time voice status indicator"
echo "   - Enhanced message display for voice responses"
echo "   - Voice button (🎤) for text-to-speech requests"
echo ""

echo "🚀 Production Features:"
echo "   ✅ Circuit breaker protection"
echo "   ✅ Retry logic with exponential backoff"
echo "   ✅ Comprehensive error handling"
echo "   ✅ Structured logging with request tracking"
echo "   ✅ Environment-based configuration"
echo "   ✅ Health monitoring and service status"
echo ""

echo "📖 Next Steps:"
echo "   1. Add ElevenLabs API key to server/.env"
echo "   2. Build Android app: cd android-simple && ./gradlew assembleDebug"
echo "   3. Install on emulator and test voice features"
echo "   4. Check interactive docs at http://localhost:3000/swagger"
echo ""

echo "🎉 Demo Complete! Your enhanced Elysia app is ready for voice-powered conversations!"