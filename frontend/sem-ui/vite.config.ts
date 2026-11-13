import path from "node:path";
import { fileURLToPath } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";
import { API_PROXY_PATHS } from "./src/config/proxy-paths";

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const srcDir = path.resolve(rootDir, "src");

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyApi = env.VITE_PROXY_API;
  const contextPath = env.VITE_CONTEXT_PATH;
  const globalConfigUrl = env.VITE_GLOBAL_CONFIG_URL?.trim();
  const base = `/${contextPath}/`;

  const proxyConfig = proxyApi
    ? Object.fromEntries(
        API_PROXY_PATHS.map((proxyPath) => [
          proxyPath,
          { target: proxyApi, changeOrigin: true, secure: false },
        ]),
      )
    : undefined;

  return {
    base,
    plugins: [
      react(),
      tailwindcss(),
      {
        name: "inject-global-config",
        transformIndexHtml(html) {
          if (!globalConfigUrl) return html;
          const script = `<script src="${globalConfigUrl}"></script>`;
          return html.replace("<!-- GLOBAL_CONFIG_SCRIPT -->", script);
        },
      },
      {
        name: "root-favicon",
        configureServer(server) {
          server.middlewares.use((req, _res, next) => {
            if (req.url === "/favicon.ico") {
              req.url = `${base}favicon.svg`;
            }
            next();
          });
        },
      },
    ],
    resolve: {
      alias: {
        "@": srcDir,
        "@/ui": path.resolve(srcDir, "ui"),
        "@/shared": path.resolve(srcDir, "shared"),
        "@/modules": path.resolve(srcDir, "modules"),
      },
    },
    server: {
      port: 5174,
      proxy: proxyConfig,
    },
    build: {
      outDir: "dist",
      sourcemap: true,
    },
  };
});
