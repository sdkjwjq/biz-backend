import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(() => ({
  plugins: [
    vue(),
  ].filter(Boolean),
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 统一代理所有 /api 开头的请求到后端
      '/api': {
        target: 'http://localhost:8080',
        // target: 'http://172.19.2.81:8080',

        changeOrigin: true,
        secure: false,
        // 如果需要重写路径（去掉 /api 前缀），取消下面注释
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
}))
