/**
 * OPC 技能交易平台 - 登录页面
 *
 * 支持买家和卖家两种角色登录
 * 登录成功后保存 token 和用户信息到本地存储
 */
<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="bg-decor">
      <view class="bg-circle c1"></view>
      <view class="bg-circle c2"></view>
      <view class="bg-circle c3"></view>
    </view>

    <!-- 品牌区域 -->
    <view class="brand-section">
      <text class="brand-icon">🎯</text>
      <text class="brand-name">OPC 技能交易平台</text>
      <text class="brand-slogan">AI 驱动 · 一人公司 · 技能变现</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-section">
      <!-- 角色切换 -->
      <view class="role-switch">
        <view class="role-item" :class="{ active: role === 0 }" @click="role = 0">
          <text class="role-text">买家登录</text>
        </view>
        <view class="role-item" :class="{ active: role === 1 }" @click="role = 1">
          <text class="role-text">卖家登录</text>
        </view>
      </view>

      <!-- 用户名输入 -->
      <view class="input-group">
        <text class="input-icon">👤</text>
        <input
          class="form-input"
          v-model="username"
          placeholder="请输入用户名"
          :maxlength="32"
        />
      </view>

      <!-- 密码输入 -->
      <view class="input-group">
        <text class="input-icon">🔒</text>
        <input
          class="form-input"
          v-model="password"
          placeholder="请输入密码"
          password
          :maxlength="32"
        />
      </view>

      <!-- 登录按钮 -->
      <view class="login-btn" :class="{ active: canLogin }" @click="handleLogin">
        <text class="login-text">{{ isLoading ? '登录中...' : '登 录' }}</text>
      </view>

      <!-- 提示 -->
      <view class="hint-section">
        <text class="hint-text">测试账号：buyer1 / seller1，密码：123456</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      username: '',
      password: '',
      role: 0,       // 0=买家 1=卖家
      isLoading: false
    }
  },
  computed: {
    canLogin() {
      return this.username.trim() && this.password.trim() && !this.isLoading
    }
  },
  methods: {
    async handleLogin() {
      if (!this.canLogin) return
      this.isLoading = true

      try {
        const res = await request('/user/user/login', {
          username: this.username,
          password: this.password
        })

        if (res.code === 1 && res.data) {
          // 保存 token 和用户信息
          uni.setStorageSync('token', res.data.token)
          uni.setStorageSync('userInfo', {
            id: res.data.id,
            username: res.data.userName,
            name: res.data.name,
            role: this.role
          })

          uni.showToast({ title: '登录成功', icon: 'success' })

          // 跳转到首页
          setTimeout(() => {
            uni.switchTab({ url: '/pages/index/index' })
          }, 1000)
        } else {
          uni.showToast({ title: res.msg || '登录失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  padding: 0 48rpx;
  position: relative;
  overflow: hidden;
}

/* 背景装饰圆 */
.bg-decor { position: absolute; top: 0; left: 0; right: 0; bottom: 0; pointer-events: none; }
.bg-circle { position: absolute; border-radius: 50%; opacity: 0.06; background: #e94560; }
.c1 { width: 400rpx; height: 400rpx; top: -100rpx; right: -100rpx; }
.c2 { width: 300rpx; height: 300rpx; bottom: 200rpx; left: -80rpx; }
.c3 { width: 200rpx; height: 200rpx; bottom: -50rpx; right: 60rpx; }

/* 品牌区域 */
.brand-section {
  padding-top: 180rpx;
  text-align: center;
  margin-bottom: 80rpx;
}
.brand-icon { font-size: 80rpx; display: block; margin-bottom: 24rpx; }
.brand-name { display: block; font-size: 44rpx; font-weight: 700; color: #fff; letter-spacing: 4rpx; }
.brand-slogan { display: block; font-size: 24rpx; color: rgba(255,255,255,0.5); margin-top: 12rpx; }

/* 表单 */
.form-section { position: relative; z-index: 1; }

.role-switch {
  display: flex;
  background: rgba(255,255,255,0.08);
  border-radius: 40rpx;
  padding: 6rpx;
  margin-bottom: 40rpx;
}
.role-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  border-radius: 36rpx;
  transition: all 0.3s;
}
.role-item.active { background: #e94560; }
.role-text { font-size: 26rpx; color: rgba(255,255,255,0.6); }
.role-item.active .role-text { color: #fff; font-weight: 600; }

.input-group {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.08);
  border-radius: 40rpx;
  padding: 0 28rpx;
  margin-bottom: 24rpx;
  border: 1rpx solid rgba(255,255,255,0.1);
}
.input-icon { font-size: 32rpx; margin-right: 16rpx; }
.form-input {
  flex: 1;
  height: 88rpx;
  font-size: 28rpx;
  color: #fff;
}
.form-input::placeholder { color: rgba(255,255,255,0.35); }

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  background: rgba(255,255,255,0.12);
  border-radius: 44rpx;
  margin-top: 40rpx;
  transition: all 0.3s;
}
.login-btn.active {
  background: linear-gradient(135deg, #e94560, #c23152);
  box-shadow: 0 8rpx 32rpx rgba(233,69,96,0.35);
}
.login-text { font-size: 30rpx; color: rgba(255,255,255,0.5); font-weight: 600; }
.login-btn.active .login-text { color: #fff; }

.hint-section { text-align: center; margin-top: 32rpx; }
.hint-text { font-size: 22rpx; color: rgba(255,255,255,0.35); }
</style>
