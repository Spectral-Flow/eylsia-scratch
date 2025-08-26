# Production Deployment Guide

This guide covers deploying the enhanced Spectra web application with full ElevenLabs integration, performance optimizations, and production-ready features.

## Prerequisites

- **Bun** runtime: [Install from bun.sh](https://bun.sh/)
- **Java 17+**: For Android development
- **Android Studio**: For building Android app
- **Docker** (recommended): For containerized deployment
- **ElevenLabs API Key**: For voice synthesis features

## Server Deployment

### Development

```bash
cd server
bun install
cp .env.example .env
# Edit .env with your configuration
bun run dev
```

The development server includes:
- Debug logging enabled
- Higher rate limits (1000 req/min)
- Enhanced error reporting
- Hot reload support

### Production

```bash
cd server
bun install --production
bun run build
NODE_ENV=production bun run start
```

Production mode features:
- Optimized logging (info level)
- Production rate limiting (100 req/min)
- Enhanced security warnings
- Performance monitoring

### Docker Deployment (Recommended)

The Docker setup now uses multi-stage builds for optimization:

```bash
cd server
docker build -t spectra-server .
docker run -d \
  --name spectra-server \
  -p 3000:3000 \
  --env-file .env \
  spectra-server
```

### Environment Variables

**Required for production:**
- `NODE_ENV=production`
- `PORT=3000` (or your preferred port)

**Voice Integration:**
- `ELEVENLABS_API_KEY=your_api_key` (for voice features)
- `ELEVENLABS_VOICE_ID=voice_id` (default voice, optional)

**Security & Performance:**
- `ALLOWED_ORIGINS=https://yourdomain.com` (CORS configuration)
- `LOG_LEVEL=info` (production logging level)

**LLM Integration:**
- `LLM_ENDPOINT=http://localhost:11434` (Ollama endpoint)
- `LLM_MODEL=llama3.2` (model name)
- `LLM_ENABLED=true` (enable/disable LLM)

## Android App Deployment

### Debug Build

```bash
cd android-simple
./gradlew assembleDebug
```

APK will be in `build/outputs/apk/debug/`

### Release Build

1. Generate signing key:
```bash
keytool -genkey -v -keystore release-key.keystore \
  -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
```

2. Create `android-simple/keystore.properties`:
```
storeFile=../release-key.keystore
storePassword=your_password
keyAlias=alias_name
keyPassword=your_password
```

3. Build release:
```bash
./gradlew assembleRelease
```

### Server Configuration for Android

Update server URL in `ApiClient.kt` for production:
```kotlin
private const val BASE_URL = "https://your-production-server.com"
```

## Production Checklist

### Server
- [ ] Set `NODE_ENV=production`
- [ ] Configure proper CORS origins
- [ ] Set up HTTPS/TLS
- [ ] Configure logging and monitoring
- [ ] Set up database (if needed)
- [ ] Configure rate limiting
- [ ] Set up health checks
- [ ] Configure backup and recovery

### Android
- [ ] Update server URLs
- [ ] Add network security config
- [ ] Enable ProGuard/R8
- [ ] Test on real devices
- [ ] Set up crash reporting
- [ ] Configure app signing
- [ ] Test offline functionality

### Security
- [ ] Implement proper authentication
- [ ] Secure API keys and secrets
- [ ] Add input validation
- [ ] Configure HTTPS only
- [ ] Set up monitoring and alerts
- [ ] Regular security updates

## Monitoring & Health Checks

### Enhanced Health Endpoint

The `/health` endpoint now provides comprehensive monitoring:

```bash
curl http://localhost:3000/health
```

Response includes:
- **Service Status**: ElevenLabs and LLM availability
- **Circuit Breaker States**: Real-time failure detection
- **System Metrics**: Memory usage, uptime, Node.js version
- **Logging Metrics**: Error rates and log message counts
- **Performance Data**: Request tracking and response times

Example response:
```json
{
  "status": "ok",
  "services": {
    "elevenLabs": {
      "available": true,
      "circuitBreaker": { "state": "CLOSED", "failureCount": 0 }
    },
    "llm": {
      "available": true,
      "model": "llama3.2",
      "circuitBreaker": { "state": "CLOSED", "failureCount": 0 }
    }
  },
  "logging": {
    "metrics": { "debug": 0, "info": 15, "warn": 2, "error": 0 }
  },
  "system": {
    "nodeVersion": "v20.x.x",
    "platform": "linux",
    "memory": { "rss": 128000000, "heapUsed": 64000000 }
  }
}
```

### Performance Features

**Voice Response Caching:**
- 5-minute TTL for voice API responses
- Reduces ElevenLabs API calls and costs
- Automatic cache cleanup prevents memory leaks

**Rate Limiting:**
- Production: 100 requests/minute per IP
- Development: 1000 requests/minute per IP
- Automatic cleanup of old rate limit data
- Rate limit headers in responses

**Circuit Breakers:**
- ElevenLabs API: 5 failures trigger 60-second cooldown
- LLM API: 3 failures trigger 30-second cooldown
- Automatic recovery when services restore

### Logging

Enhanced structured logging with metrics:
```bash
# Follow logs in Docker
docker logs -f spectra-server

# Check error patterns
grep "ERROR" logs/app.log
grep "Circuit breaker" logs/app.log
grep "Rate limit" logs/app.log
```

### Metrics

Monitor:
- API response times
- Error rates
- Voice synthesis usage
- WebSocket connections
- Memory and CPU usage

## Scaling Considerations

### Horizontal Scaling
- Stateless server design
- WebSocket session management
- Load balancer configuration

### Performance Optimization
- Voice synthesis caching
- Connection pooling
- Response compression
- CDN for static assets

### Cost Optimization
- ElevenLabs API usage monitoring
- Efficient voice synthesis batching
- Connection keep-alive
- Resource cleanup