FROM oven/bun:latest

WORKDIR /app

# Copy package files
COPY package.json bun.lockb* ./

# Install dependencies (disable certificate checking for build)
ENV NODE_TLS_REJECT_UNAUTHORIZED=0
RUN bun install --frozen-lockfile
ENV NODE_TLS_REJECT_UNAUTHORIZED=1

# Copy source code
COPY . .

# Build the application
RUN bun run build

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:3000/health || exit 1

# Start the application
CMD ["bun", "run", "start"]