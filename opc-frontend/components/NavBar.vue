/**
 * OPC 技能交易平台 - 自定义导航栏组件
 *
 * 用于需要自定义导航栏的页面（如详情页）
 * 支持滚动渐变效果
 *
 * Props:
 * - title: 标题文字
 * - opacity: 背景透明度（0-1）
 * - bgColor: 背景色（默认白色）
 *
 * Events:
 * - back: 点击返回按钮
 */
<template>
  <view class="custom-navbar" :style="{ background: `rgba(255,255,255,${opacity})` }">
    <view class="navbar-content" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-left" @click="$emit('back')">
        <text class="back-icon" :style="{ color: opacity > 0.5 ? '#1a1a2e' : '#fff' }">←</text>
      </view>
      <text class="navbar-title" :style="{ opacity }">{{ title }}</text>
      <view class="navbar-right"></view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'NavBar',
  props: {
    title: { type: String, default: '' },
    opacity: { type: Number, default: 1 },
    bgColor: { type: String, default: '#ffffff' }
  },
  data() {
    return {
      statusBarHeight: 44
    }
  },
  mounted() {
    // 获取状态栏高度
    const sysInfo = uni.getSystemInfoSync()
    this.statusBarHeight = sysInfo.statusBarHeight || 44
  }
}
</script>

<style scoped>
.custom-navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(0,0,0,0.05);
}
.navbar-content {
  display: flex; align-items: center; justify-content: space-between;
  height: 88rpx; padding: 0 32rpx;
}
.navbar-left { width: 64rpx; height: 64rpx; display: flex; align-items: center; }
.back-icon { font-size: 36rpx; }
.navbar-title { font-size: 32rpx; font-weight: 600; color: #1a1a2e; }
.navbar-right { width: 64rpx; }
</style>
