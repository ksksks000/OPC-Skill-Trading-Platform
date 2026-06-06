/**
 * OPC 技能交易平台 - 我的页面
 *
 * 展示用户信息、功能入口
 */
<template>
  <view class="mine-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-avatar">
        <text class="avatar-text">{{ userInfo.name ? userInfo.name[0] : '?' }}</text>
      </view>
      <view class="user-info" v-if="isLogin">
        <text class="user-name">{{ userInfo.name || '用户' }}</text>
        <text class="user-role">{{ userInfo.role === 1 ? '卖家' : '买家' }}</text>
      </view>
      <view class="user-info" v-else @click="goLogin">
        <text class="user-name">点击登录</text>
        <text class="user-role">登录后享受更多服务</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="func-section">
      <view class="func-item" @click="goPage('/pages/order/index')">
        <text class="func-icon">📋</text>
        <text class="func-text">我的订单</text>
        <text class="func-arrow">→</text>
      </view>
      <view class="func-item" @click="goPage('/pages/chat/index?sellerId=0')">
        <text class="func-icon">💬</text>
        <text class="func-text">消息中心</text>
        <view class="unread-badge" v-if="unreadCount > 0">
          <text class="badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view>
        <text class="func-arrow">→</text>
      </view>
      <view class="func-item" @click="goPage('/pages/ai-search/index')">
        <text class="func-icon">🤖</text>
        <text class="func-text">AI 智搜</text>
        <text class="func-arrow">→</text>
      </view>
    </view>

    <!-- 卖家功能 -->
    <view class="func-section" v-if="userInfo.role === 1">
      <view class="section-title">
        <text class="title-text">卖家工具</text>
      </view>
      <view class="func-item" @click="goPage('/pages/skill/manage')">
        <text class="func-icon">🎯</text>
        <text class="func-text">技能管理</text>
        <text class="func-arrow">→</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section" v-if="isLogin">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      userInfo: {},
      isLogin: false,
      unreadCount: 0
    }
  },

  onShow() {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.id) {
      this.userInfo = userInfo
      this.isLogin = true
      this.loadUnreadCount()
    } else {
      this.userInfo = {}
      this.isLogin = false
    }
  },

  methods: {
    async loadUnreadCount() {
      try {
        const res = await request('/user/chat/unread-count', {}, 'GET')
        if (res.code === 1) this.unreadCount = res.data || 0
      } catch (e) { /* 忽略 */ }
    },

    goLogin() {
      uni.navigateTo({ url: '/pages/login/index' })
    },

    goPage(url) {
      if (!this.isLogin) {
        this.goLogin()
        return
      }
      uni.navigateTo({ url })
    },

    handleLogout() {
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      this.userInfo = {}
      this.isLogin = false
      this.unreadCount = 0
      uni.showToast({ title: '已退出', icon: 'success' })
    }
  }
}
</script>

<style scoped>
.mine-page { min-height: 100vh; background: #f5f5f7; }

.user-card {
  display: flex; align-items: center;
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  padding: 60rpx 32rpx 40rpx; border-radius: 0 0 32rpx 32rpx;
}
.user-avatar {
  width: 100rpx; height: 100rpx; border-radius: 50%;
  background: linear-gradient(135deg, #e94560, #c23152);
  display: flex; align-items: center; justify-content: center;
}
.avatar-text { font-size: 40rpx; color: #fff; font-weight: 700; }
.user-info { margin-left: 24rpx; }
.user-name { display: block; font-size: 32rpx; font-weight: 600; color: #fff; }
.user-role { display: block; font-size: 24rpx; color: rgba(255,255,255,0.5); margin-top: 4rpx; }

.func-section { margin: 20rpx 32rpx; background: #fff; border-radius: 16rpx; overflow: hidden; }
.section-title { padding: 20rpx 24rpx; border-bottom: 1rpx solid #f5f5f5; }
.title-text { font-size: 26rpx; font-weight: 600; color: #1a1a2e; }

.func-item {
  display: flex; align-items: center; padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
}
.func-item:last-child { border-bottom: none; }
.func-icon { font-size: 32rpx; margin-right: 16rpx; }
.func-text { flex: 1; font-size: 28rpx; color: #333; }
.func-arrow { font-size: 28rpx; color: #ccc; }

.unread-badge {
  background: #e94560; border-radius: 20rpx;
  padding: 2rpx 12rpx; margin-right: 12rpx;
}
.badge-text { font-size: 20rpx; color: #fff; }

.logout-section { padding: 40rpx 32rpx; }
.logout-btn {
  display: flex; align-items: center; justify-content: center;
  height: 80rpx; background: #fff; border-radius: 40rpx;
  border: 1rpx solid #eee;
}
.logout-text { font-size: 28rpx; color: #e94560; }
</style>
