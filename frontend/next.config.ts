import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    images: {
        remotePatterns: [
            {
                protocol: "http",
                hostname: "localhost",
                port: "8080",
                // pathname: "/api/v1/product/**"
            }
        ],
        unoptimized: process.env.NODE_ENV === "development"
    }
  /* config options here */
};

export default nextConfig;
