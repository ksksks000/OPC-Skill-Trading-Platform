/**
 * OPC 技能交易平台 - 聊天页面
 *
 * 功能：
 * 1. 买卖双方点对点聊天
 * 2. 进入页面自动连接 WebSocket
 * 3. 自动拉取历史消息
 * 4. 发送消息（文本/图片）
 * 5. 区分"我发的"和"对方发的"气泡样式
 * 6. 实时接收对方消息
 */
<template>
  <view class="chat-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">←</text>
      </view>
      <text class="nav-title">{{ targetName || '聊天' }}</text>
      <view class="nav-online" v-if="isTargetOnline">
        <text class="online-dot"></text>
        <text class="online-text">在线</text>
      </view>
    </view>

    <!-- 消息列表区域 -->
    <scroll-view
      class="message-list"
      scroll-y
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <!-- 时间分割线 -->
      <view class="time-divider" v-if="messages.length > 0">
        <text class="time-text">{{ formatDate(messages[0].createTime) }}</text>
      </view>

      <!-- 消息项 -->
      <view
        class="message-item"
        v-for="(msg, index) in messages"
        :key="msg.id || index"
        :class="{ 'is-mine': msg.senderId === currentUserId }"
      >
        <!-- 时间分割线（间隔超过5分钟显示） -->
        <view class="time-divider" v-if="shouldShowTime(index)">
          <text class="time-text">{{ formatDate(msg.createTime) }}</text>
        </view>

        <!-- 对方消息（左侧气泡） -->
        <view class="msg-row other-msg" v-if="msg.senderId !== currentUserId">
          <view class="avatar-wrap">
            <text class="avatar-text">{{ targetName[0] || '?' }}</text>
          </view>
          <view class="bubble-wrap">
            <!-- 文本消息 -->
            <view class="bubble other-bubble" v-if="msg.type === 'text'">
              <text class="bubble-text">{{ msg.content }}</text>
            </view>
            <!-- 图片消息 -->
            <view class="bubble image-bubble" v-if="msg.type === 'image'" @click="previewImage(msg.content)">
              <image class="msg-image" :src="msg.content" mode="widthFix" />
            </view>
            <text class="msg-time">{{ formatTime(msg.createTime) }}</text>
          </view>
        </view>

        <!-- 我的消息（右侧气泡） -->
        <view class="msg-row my-msg" v-if="msg.senderId === currentUserId">
          <view class="bubble-wrap my-bubble-wrap">
            <!-- 文本消息 -->
            <view class="bubble my-bubble" v-if="msg.type === 'text'">
              <text class="bubble-text my-text">{{ msg.content }}</text>
            </view>
            <!-- 图片消息 -->
            <view class="bubble image-bubble" v-if="msg.type === 'image'" @click="previewImage(msg.content)">
              <image class="msg-image" :src="msg.content" mode="widthFix" />
            </view>
            <text class="msg-time my-time">{{ formatTime(msg.createTime) }}</text>
          </view>
          <view class="avatar-wrap my-avatar">
            <text class="avatar-text">我</text>
          </view>
        </view>
      </view>

      <!-- 底部占位，防止最后一条消息被输入框遮挡 -->
      <view class="list-bottom"></view>
    </scroll-view>

    <!-- 底部输入区域 -->
    <view class="input-bar">
      <view class="input-wrap">
        <!-- 图片按钮 -->
        <view class="img-btn" @click="chooseImage">
          <text class="img-icon">📷</text>
        </view>
        <!-- 文本输入框 -->
        <input
          class="msg-input"
          v-model="inputText"
          placeholder="输入消息..."
          confirm-type="send"
          @confirm="sendTextMessage"
          :adjust-position="true"
        />
        <!-- 发送按钮 -->
        <view
          class="send-btn"
          :class="{ active: inputText.trim() }"
          @click="sendTextMessage"
        >
          <text class="send-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      // 当前登录用户ID
      currentUserId: null,
      // 对方用户ID
      targetId: null,
      // 对方用户名称
      targetName: '卖家',
      // 关联的技能ID（可选，用于上下文）
      skillId: null,
      // 消息列表
      messages: [],
      // 输入框文本
      inputText: '',
      // 滚动位置（控制自动滚动到底部）
      scrollTop: 0,
      // 对方是否在线
      isTargetOnline: false,
      // WebSocket 连接实例
      socketTask: null
    }
  },

  onLoad(options) {
    // 从路由参数获取对方用户ID和技能ID
    this.targetId = options.sellerId ? Number(options.sellerId) : null
    this.skillId = options.skillId ? Number(options.skillId) : null

    // 从本地存储获取当前用户ID
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo) {
      this.currentUserId = userInfo.id
    }

    // 加载历史消息
    this.loadChatHistory()

    // 连接 WebSocket
    this.connectWebSocket()
  },

  onUnload() {
    // 页面卸载时关闭 WebSocket 连接
    this.closeWebSocket()
  },

  methods: {
    /**
     * 加载聊天历史记录
     * 从后端 HTTP 接口获取，而非 WebSocket
     */
    async loadChatHistory() {
      if (!this.targetId) return
      try {
        const res = await request('/user/chat/history', { targetId: this.targetId }, 'GET')
        if (res.code === 1 && res.data) {
          this.messages = res.data
          this.scrollToBottom()
        }
      } catch (e) {
        console.error('加载历史消息失败:', e)
      }
    },

    /**
     * 连接 WebSocket
     * 使用 uni.connectSocket API，兼容 H5 和小程序
     */
    connectWebSocket() {
      if (!this.currentUserId) return

      const wsUrl = `ws://localhost:8080/ws/chat/${this.currentUserId}`
      console.log('连接 WebSocket:', wsUrl)

      this.socketTask = uni.connectSocket({
        url: wsUrl,
        complete: () => {}
      })

      // 连接成功
      uni.onSocketOpen(() => {
        console.log('WebSocket 连接成功')
      })

      // 收到消息
      uni.onSocketMessage((res) => {
        try {
          const msg = JSON.parse(res.data)
          // 如果是对方发来的消息，添加到消息列表
          if (msg.senderId === this.targetId) {
            this.messages.push({
              id: msg.id,
              senderId: msg.senderId,
              receiverId: msg.receiverId,
              content: msg.content,
              type: msg.type,
              createTime: msg.createTime
            })
            this.scrollToBottom()
          }
        } catch (e) {
          console.warn('WebSocket 消息解析失败:', e)
        }
      })

      // 连接关闭
      uni.onSocketClose(() => {
        console.log('WebSocket 连接关闭')
      })

      // 连接错误
      uni.onSocketError((err) => {
        console.error('WebSocket 连接错误:', err)
      })
    },

    /**
     * 关闭 WebSocket 连接
     */
    closeWebSocket() {
      if (this.socketTask) {
        uni.closeSocket()
        this.socketTask = null
      }
    },

    /**
     * 发送文本消息
     * 通过 HTTP 接口发送，WebSocket 用于接收
     */
    async sendTextMessage() {
      if (!this.inputText.trim() || !this.targetId) return

      const content = this.inputText.trim()
      this.inputText = ''

      // 先在本地添加消息（乐观更新）
      const tempMsg = {
        senderId: this.currentUserId,
        receiverId: this.targetId,
        content: content,
        type: 'text',
        createTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
      }
      this.messages.push(tempMsg)
      this.scrollToBottom()

      // 发送到后端
      try {
        await request('/user/chat/send', {
          receiverId: this.targetId,
          content: content,
          type: 'text'
        })
      } catch (e) {
        console.error('发送消息失败:', e)
        uni.showToast({ title: '发送失败', icon: 'none' })
      }
    },

    /**
     * 选择图片并发送
     */
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        success: async (res) => {
          const tempPath = res.tempFilePaths[0]
          // TODO: 上传图片到 OSS，获取 URL
          // 目前先用临时路径展示
          this.messages.push({
            senderId: this.currentUserId,
            receiverId: this.targetId,
            content: tempPath,
            type: 'image',
            createTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
          })
          this.scrollToBottom()

          // 发送图片消息
          try {
            await request('/user/chat/send', {
              receiverId: this.targetId,
              content: tempPath,
              type: 'image'
            })
          } catch (e) {
            console.error('发送图片失败:', e)
          }
        }
      })
    },

    /**
     * 预览图片
     */
    previewImage(url) {
      uni.previewImage({
        current: url,
        urls: [url]
      })
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = this.scrollTop + 9999
      })
    },

    /**
     * 判断是否应该显示时间分割线
     * 两条消息间隔超过5分钟时显示
     */
    shouldShowTime(index) {
      if (index === 0) return true
      const prev = new Date(this.messages[index - 1].createTime).getTime()
      const curr = new Date(this.messages[index].createTime).getTime()
      return (curr - prev) > 5 * 60 * 1000 // 5分钟
    },

    /**
     * 格式化日期（显示月-日）
     */
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return `${d.getMonth() + 1}月${d.getDate()}日`
    },

    /**
     * 格式化时间（显示时:分）
     */
    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      const h = d.getHours().toString().padStart(2, '0')
      const m = d.getMinutes().toString().padStart(2, '0')
      return `${h}:${m}`
    },

    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f3;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 32rpx;
  padding-top: var(--status-bar-height, 44rpx);
  background: #ffffff;
  border-bottom: 1rpx solid #eee;
  position: relative;
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
}

.nav-back-icon {
  font-size: 36rpx;
  color: #1a1a2e;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.nav-online {
  display: flex;
  align-items: center;
  position: absolute;
  right: 32rpx;
  top: 50%;
  transform: translateY(25%);
}

.online-dot {
  width: 12rpx;
  height: 12rpx;
  background: #10b981;
  border-radius: 50%;
  margin-right: 8rpx;
}

.online-text {
  font-size: 22rpx;
  color: #10b981;
}

/* 消息列表 */
.message-list {
  flex: 1;
  padding: 20rpx 24rpx;
}

.list-bottom {
  height: 20rpx;
}

/* 时间分割线 */
.time-divider {
  display: flex;
  justify-content: center;
  margin: 24rpx 0;
}

.time-text {
  font-size: 22rpx;
  color: #aaa;
  background: rgba(0,0,0,0.04);
  padding: 4rpx 20rpx;
  border-radius: 12rpx;
}

/* 消息行 */
.msg-row {
  display: flex;
  margin-bottom: 24rpx;
  align-items: flex-start;
}

/* 头像 */
.avatar-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.my-avatar {
  background: linear-gradient(135deg, #e94560, #c23152);
}

.avatar-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 700;
}

/* 气泡 */
.bubble-wrap {
  margin-left: 16rpx;
  max-width: 520rpx;
}

.my-bubble-wrap {
  margin-left: 0;
  margin-right: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.bubble {
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  word-break: break-all;
}

.other-bubble {
  background: #ffffff;
  border-top-left-radius: 4rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

.my-bubble {
  background: linear-gradient(135deg, #e94560, #c23152);
  border-top-right-radius: 4rpx;
  box-shadow: 0 4rpx 16rpx rgba(233,69,96,0.2);
}

.bubble-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.my-text {
  color: #ffffff;
}

/* 图片消息 */
.image-bubble {
  padding: 8rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.msg-image {
  width: 300rpx;
  border-radius: 12rpx;
}

/* 消息时间 */
.msg-time {
  font-size: 20rpx;
  color: #bbb;
  margin-top: 6rpx;
  margin-left: 8rpx;
}

.my-time {
  margin-left: 0;
  margin-right: 8rpx;
}

/* 底部输入栏 */
.input-bar {
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1rpx solid #eee;
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.img-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.img-icon {
  font-size: 40rpx;
}

.msg-input {
  flex: 1;
  height: 72rpx;
  background: #f5f5f7;
  border-radius: 36rpx;
  padding: 0 28rpx;
  font-size: 28rpx;
  color: #333;
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 72rpx;
  background: #e0e0e0;
  border-radius: 36rpx;
  transition: all 0.3s;
  flex-shrink: 0;
}

.send-btn.active {
  background: linear-gradient(135deg, #e94560, #c23152);
  box-shadow: 0 4rpx 12rpx rgba(233,69,96,0.3);
}

.send-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 600;
}
</style>
