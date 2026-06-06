/**
 * OPC 技能交易平台 - 应用入口
 *
 * UniApp + Vue3 项目入口文件
 * 负责创建 Vue 应用实例和注册全局配置
 */
import { createSSRApp } from 'vue'
import App from './App.vue'

/**
 * 创建应用实例
 * UniApp 要求使用 createSSRApp 以支持 SSR 场景
 */
export function createApp() {
  const app = createSSRApp(App)
  return { app }
}
