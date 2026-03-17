import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: "http",
                hostname: "localhost",
                port: "8080",
            },
            {
                protocol: "http",
                hostname: "backend",
                port: "8080",
            }
        ],
        unoptimized: true
    },
    output: "standalone",
};

export default nextConfig;
