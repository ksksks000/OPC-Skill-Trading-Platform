import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

/**
 * Vite 配置
 * UniApp + Vue3 项目的基础构建配置
 *
 * 代理配置：同时在此处和 manifest.json 的 h5.devServer.proxy 中配置，
 * 确保无论 UniApp 使用哪种方式启动 dev server，代理都能生效。
 */
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    proxy: {
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 关键配置：确保自定义请求头（如 token）被正确转发
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // 转发 token 请求头（admin 和 user 统一使用 token 请求头）
            if (req.headers['token']) {
              proxyReq.setHeader('token', req.headers['token'])
            }
          })
        }
      },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            if (req.headers['token']) {
              proxyReq.setHeader('token', req.headers['token'])
            }
          })
        }
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
