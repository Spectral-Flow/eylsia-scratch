# ElevenLabs Voice Integration Setup

## Overview

This application features **complete ElevenLabs voice synthesis integration** with real API support, intelligent caching, and graceful fallback modes for production-ready voice features.

## Setup Instructions

### 1. Get ElevenLabs API Key

1. Sign up at [ElevenLabs](https://elevenlabs.io/)
2. Go to your [API Keys](https://elevenlabs.io/app/settings/api-keys) page
3. Create or copy your API key

### 2. Configure Environment

Copy the environment template:
```bash
cd server
cp .env.example .env
```

Edit `.env` and add your API key:
```bash
ELEVENLABS_API_KEY=your_actual_api_key_here
```

### 3. Available Voice Features

#### Server Endpoints

- `GET /api/voice/voices` - Get available voices
- `POST /api/voice/synthesize` - Convert text to speech
- `GET /health` - Check voice service status

#### WebSocket Voice Commands

Send JSON messages to `/ws`:
```json
{
  "type": "voice-request",
  "content": "Text to synthesize",
  "voiceId": "optional_voice_id"
}
```

#### Android Voice Features

- Voice synthesis button (🎤) in chat input
- Voice status indicator in top bar
- Enhanced message display for voice responses

### 4. Voice Settings

You can customize voice synthesis with these parameters:

```json
{
  "text": "Your text here",
  "voiceId": "voice_id_optional",
  "voiceSettings": {
    "stability": 0.5,
    "similarityBoost": 0.75,
    "style": 0.0,
    "useSpeakerBoost": true
  }
}
```

### 5. Default Voice

The default voice is Rachel (`21m00Tcm4TlvDq8ikWAM`). You can change this in the `.env` file:
```bash
ELEVENLABS_VOICE_ID=your_preferred_voice_id
```

### 6. Performance Features

The voice integration includes several performance optimizations:

- **Intelligent Caching**: Voice API responses are cached for 5 minutes to reduce API calls
- **Rate Limiting**: Built-in rate limiting prevents API quota exhaustion
- **Circuit Breakers**: Automatic failover when API is unavailable
- **Graceful Degradation**: Falls back to mock mode when real API fails
- **Memory Management**: Automatic cleanup of old cache entries and rate limit data

### 7. Error Handling

The application includes robust error handling for voice synthesis:
- **API Authentication**: Automatic detection and reporting of API key issues
- **Rate Limit Detection**: Intelligent handling of API quota and rate limits
- **Network Resilience**: Retry logic with exponential backoff for network errors
- **Circuit breaker prevents cascading failures**
- **Detailed error logging and user feedback**
- **Graceful degradation when voice service is unavailable**

### 8. Testing Without API Key

The application works without an ElevenLabs API key:
- Voice features will be disabled
- Mock voice synthesis responses for development
- All other features remain fully functional

## Production Considerations

### Environment Configuration
- Set `NODE_ENV=production` for production deployments
- Configure `LOG_LEVEL=info` or `warn` for production (avoid `debug`)
- Use proper CORS origins (avoid wildcard `*` in production)
- Set appropriate rate limiting thresholds

### Monitoring & Performance
- Monitor API usage and rate limits through the `/health` endpoint
- Check logging metrics for error rates and performance issues
- Set up alerts for circuit breaker state changes
- Monitor memory usage for voice caching

### Security Best Practices
- Keep ElevenLabs API keys secure and rotate regularly
- Use environment variables for all sensitive configuration
- Implement proper authentication for voice features in production
- Configure firewalls and reverse proxies appropriately

### Scaling Considerations
- Voice caching reduces API load but increases memory usage
- Rate limiting is per-instance; consider distributed rate limiting for multiple instances
- Circuit breakers help prevent cascade failures during API outages
- Consider caching frequently used voice syntheses externally