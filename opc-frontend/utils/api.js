/**
 * OPC 技能交易平台 - 全局配置与工具
 *
 * 统一管理 API 基础地址、请求工具、SSE 连接、通用工具函数
 */

// API 基础地址，根据实际部署修改
// 开发环境使用代理时设为空字符串，生产环境设为实际后端地址
const BASE_URL = import.meta.env.DEV ? '' : 'localhost:8080'

/**
 * 通用请求封装
 *
 * @param {string} url - 请求路径（不含基础地址）
 * @param {object} data - 请求体数据（GET 请求时作为 query 参数）
 * @param {string} method - 请求方法，默认 POST
 * @returns {Promise} 响应数据
 */
export const request = (url, data = {}, method = 'POST') => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        // 统一使用 token 请求头，admin 和 user 共用
        'token': uni.getStorageSync('token') || uni.getStorageSync('adminToken') || ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // 未登录，跳转到登录页
          uni.showToast({ title: '请先登录', icon: 'none' })
          setTimeout(() => uni.navigateTo({ url: '/pages/login/index' }), 1500)
          reject(new Error('未登录'))
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * SSE 流式请求封装
 *
 * H5 端使用 fetch API 实现真正的流式读取；
 * 小程序端降级为同步请求，一次性返回结果。
 *
 * @param {string} url - 请求路径
 * @param {object} data - 请求体数据
 * @param {function} onChunk - 每收到一个文本片段的回调
 * @param {function} onDone - 流结束回调
 * @param {function} onError - 错误回调
 */
export const fetchSSE = async (url, data, { onChunk, onDone, onError }) => {
  try {
    // #ifdef H5
    const response = await fetch(`${BASE_URL}${url}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': uni.getStorageSync('token') || ''
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 按 SSE 格式解析：每条消息以 "data: " 开头，以换行分隔
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const content = line.slice(6)
          if (content === '[DONE]') {
            onDone && onDone()
            return
          }
          if (content && content.trim()) {
            onChunk && onChunk(content)
          }
        }
      }
    }
    // #endif

    // #ifndef H5
    // 非 H5 端（小程序）降级为同步请求
    const result = await request(url, data)
    if (result) {
      onChunk && onChunk(typeof result === 'string' ? result : JSON.stringify(result))
    }
    onDone && onDone()
    // #endif

  } catch (error) {
    onError && onError(error)
  }
}

/**
 * 格式化日期
 * @param {string} dateStr - 日期字符串
 * @param {string} format - 格式类型：'date' | 'time' | 'full'
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (dateStr, format = 'date') => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')

  switch (format) {
    case 'time': return `${hour}:${minute}`
    case 'full': return `${month}月${day}日 ${hour}:${minute}`
    default: return `${month}月${day}日`
  }
}

/**
 * 解析技能标签 JSON 字符串
 * @param {string} tagsStr - JSON 格式的标签字符串
 * @param {number} limit - 最大返回数量
 * @returns {string[]} 标签数组
 */
export const parseTags = (tagsStr, limit = 3) => {
  if (!tagsStr) return []
  try {
    const tags = JSON.parse(tagsStr)
    return Array.isArray(tags) ? tags.slice(0, limit) : []
  } catch {
    return []
  }
}

/**
 * 检查用户是否已登录
 * @returns {boolean}
 */
export const isLoggedIn = () => {
  return !!uni.getStorageSync('token')
}

/**
 * 获取当前用户信息
 * @returns {object|null}
 */
export const getUserInfo = () => {
  return uni.getStorageSync('userInfo') || null
}

export default { BASE_URL, request, fetchSSE, formatDate, parseTags, isLoggedIn, getUserInfo }
