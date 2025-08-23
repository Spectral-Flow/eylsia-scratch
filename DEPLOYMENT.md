# Deployment Guide

## Prerequisites

- **Bun** runtime: [Install from bun.sh](https://bun.sh/)
- **Java 17+**: For Android development
- **Android Studio**: For building Android app
- **Docker** (optional): For containerized deployment

## Server Deployment

### Development

```bash
cd server
bun install
cp .env.example .env
# Edit .env with your configuration
bun run dev
```

### Production

```bash
cd server
bun install --production
bun run build
NODE_ENV=production bun run start
```

### Docker Deployment

```bash
cd server
docker build -t elysia-voice-app .
docker run -p 3000:3000 \
  -e ELEVENLABS_API_KEY=your_key \
  -e NODE_ENV=production \
  elysia-voice-app
```

### Environment Variables

Required for production:
- `NODE_ENV=production`
- `PORT=3000` (or your preferred port)
- `ELEVENLABS_API_KEY=your_api_key` (for voice features)

Optional:
- `ELEVENLABS_VOICE_ID=voice_id` (default voice)
- `ALLOWED_ORIGINS=https://yourdomain.com` (CORS)
- `LOG_LEVEL=info` (logging level)

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

## Monitoring

### Health Endpoints

- `GET /health` - Basic health check
- Monitor response time and availability
- Check service dependencies status

### Logging

Server includes structured logging with request IDs:
- Error tracking
- Performance monitoring
- User behavior analytics

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