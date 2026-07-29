import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      // 联调时代理到后端（关闭 Mock 时生效）；所有接口统一以 /api 开头
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
