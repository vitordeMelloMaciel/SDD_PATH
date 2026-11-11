import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  agentRules: false,
  poweredByHeader: false,
  reactStrictMode: true,
  async rewrites() {
    const backend = process.env.INTERNAL_API_BASE_URL
      ?? (process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : undefined);
    return backend ? [{ source: '/api/:path*', destination: `${backend.replace(/\/$/, '')}/api/:path*` }] : [];
  },
};

export default nextConfig;
