import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  cacheHandler:
    process.env.profile === "redis"
      ? require.resolve("./app/lib/redis-cache-handler.js")
      : require.resolve("./app/lib/mongo-cache-handler.js"),
  //cacheMaxMemorySize: 0,
};

export default nextConfig;
