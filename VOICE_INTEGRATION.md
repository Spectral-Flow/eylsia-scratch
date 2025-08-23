# ElevenLabs Voice Integration Setup

## Overview

This application includes ElevenLabs voice synthesis integration for real-time voice generation from text.

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

### 6. Error Handling

The application includes robust error handling for voice synthesis:
- Circuit breaker prevents cascading failures
- Retry logic with exponential backoff
- Graceful degradation when voice service is unavailable
- Detailed error logging and user feedback

### 7. Testing Without API Key

The application works without an ElevenLabs API key:
- Voice features will be disabled
- Mock voice synthesis responses for development
- All other features remain fully functional

## Production Considerations

- Set `NODE_ENV=production` in production
- Monitor API usage and rate limits
- Consider caching frequently used voice syntheses
- Implement user authentication for voice features
- Set up proper logging and monitoring