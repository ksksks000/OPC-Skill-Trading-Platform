import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

/**
 * Vite 配置
 * UniApp + Vue3 项目的基础构建配置
 */
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    proxy: {
      // 开发环境代理，解决跨域问题
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
