/**
 * OPC 技能交易平台 - 空状态组件
 *
 * 当列表为空时展示的占位提示
 *
 * Props:
 * - icon: 图标（emoji）
 * - text: 提示文字
 * - actionText: 操作按钮文字（可选）
 *
 * Events:
 * - action: 点击操作按钮时触发
 */
<template>
  <view class="empty-state">
    <text class="empty-icon">{{ icon }}</text>
    <text class="empty-text">{{ text }}</text>
    <view class="empty-action" v-if="actionText" @click="$emit('action')">
      <text class="action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    icon: { type: String, default: '📭' },
    text: { type: String, default: '暂无数据' },
    actionText: { type: String, default: '' }
  }
}
</script>

<style scoped>
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 120rpx 60rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 28rpx; color: #999; text-align: center; }
.empty-action {
  margin-top: 32rpx; padding: 16rpx 48rpx;
  background: #e94560; border-radius: 32rpx;
}
.action-text { font-size: 26rpx; color: #fff; font-weight: 600; }
</style>
