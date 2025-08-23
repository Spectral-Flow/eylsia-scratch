# API Documentation

## Base URL
- Development: `http://localhost:3000`
- Android Emulator: `http://10.0.2.2:3000`

## Authentication
- Simple bearer token for development: `Bearer dev`
- Production: Implement proper authentication

## Core Endpoints

### Health Check
```
GET /health
```

Response includes:
- Server status and uptime
- Service availability (ElevenLabs, etc.)
- System information
- Circuit breaker states

### Hello API
```
GET /api/hello?name=YourName
```

Returns greeting with voice capability status.

## Voice Synthesis API

### Get Available Voices
```
GET /api/voice/voices
```

Returns list of available ElevenLabs voices.

### Synthesize Speech
```
POST /api/voice/synthesize
Content-Type: application/json

{
  "text": "Text to convert to speech",
  "voiceId": "optional_voice_id",
  "voiceSettings": {
    "stability": 0.5,
    "similarityBoost": 0.75,
    "style": 0.0,
    "useSpeakerBoost": true
  }
}
```

Returns audio data (MP3 format).

## WebSocket API

### Connection
```
ws://localhost:3000/ws
```

### Message Types

#### Text Message
```json
{
  "type": "text",
  "content": "Your message"
}
```

#### Voice Request
```json
{
  "type": "voice-request",
  "content": "Text to synthesize",
  "voiceId": "optional_voice_id"
}
```

#### System Messages
- `system` - Server notifications
- `history` - Chat history on connect
- `voice-response` - Voice synthesis result
- `voice-error` - Voice synthesis error

## Error Handling

All endpoints return structured error responses:
```json
{
  "error": true,
  "message": "Error description",
  "requestId": "unique_request_id",
  "timestamp": "ISO_8601_timestamp"
}
```

## Rate Limiting

- Built-in circuit breaker for external services
- Configurable retry logic
- Graceful degradation on service failures

## Swagger Documentation

Interactive API documentation available at:
```
http://localhost:3000/swagger
```