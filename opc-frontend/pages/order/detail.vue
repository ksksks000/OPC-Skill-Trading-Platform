/**
 * OPC 技能交易平台 - 订单详情页
 *
 * 功能：
 * 1. 展示订单基本信息（订单号、状态、金额、时间）
 * 2. 展示订单明细（技能名称、图片、价格）
 * 3. 展示服务交付状态进度条
 * 4. 操作按钮：支付 / 联系卖家 / 确认完成
 */
<template>
  <view class="detail-page">
    <!-- 加载状态 -->
    <view class="loading-wrap" v-if="isLoading">
      <view class="loading-spinner"></view>
    </view>

    <view class="detail-content" v-if="!isLoading && order">
      <!-- 订单状态卡片 -->
      <view class="status-card" :class="'status-bg-' + order.status">
        <text class="status-icon">{{ getStatusIcon(order.status) }}</text>
        <text class="status-text">{{ getStatusText(order.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>

      <!-- 服务交付进度条 -->
      <view class="progress-section" v-if="order.payStatus === 1">
        <view class="progress-bar">
          <view class="progress-step" :class="{ active: order.deliveryStatus >= 0 }">
            <view class="step-dot"></view>
            <text class="step-label">待服务</text>
          </view>
          <view class="progress-line" :class="{ active: order.deliveryStatus >= 1 }"></view>
          <view class="progress-step" :class="{ active: order.deliveryStatus >= 1 }">
            <view class="step-dot"></view>
            <text class="step-label">服务中</text>
          </view>
          <view class="progress-line" :class="{ active: order.deliveryStatus >= 2 }"></view>
          <view class="progress-step" :class="{ active: order.deliveryStatus >= 2 }">
            <view class="step-dot"></view>
            <text class="step-label">待确认</text>
          </view>
          <view class="progress-line" :class="{ active: order.deliveryStatus >= 3 }"></view>
          <view class="progress-step" :class="{ active: order.deliveryStatus >= 3 }">
            <view class="step-dot"></view>
            <text class="step-label">已完成</text>
          </view>
        </view>
      </view>

      <!-- 订单明细 -->
      <view class="detail-section">
        <view class="section-title">
          <text class="title-text">服务详情</text>
        </view>
        <view class="detail-item" v-for="item in order.orderDetailList" :key="item.id">
          <image class="item-image" :src="item.image || defaultImage" mode="aspectFill" />
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-amount">¥{{ item.amount }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="info-section">
        <view class="section-title">
          <text class="title-text">订单信息</text>
        </view>
        <view class="info-row">
          <text class="info-label">订单号</text>
          <text class="info-value">{{ order.number }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ formatTime(order.orderTime) }}</text>
        </view>
        <view class="info-row" v-if="order.checkoutTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ formatTime(order.checkoutTime) }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">支付状态</text>
          <text class="info-value" :class="order.payStatus === 1 ? 'paid' : 'unpaid'">
            {{ order.payStatus === 1 ? '已支付' : '未支付' }}
          </text>
        </view>
        <view class="info-row" v-if="order.remark">
          <text class="info-label">备注</text>
          <text class="info-value">{{ order.remark }}</text>
        </view>
        <view class="info-row total-row">
          <text class="info-label">实付金额</text>
          <text class="total-amount">¥{{ order.amount }}</text>
        </view>
      </view>

      <!-- 底部操作栏 -->
      <view class="bottom-bar">
        <!-- 待付款状态：显示支付按钮 -->
        <view class="pay-btn" v-if="order.status === 1" @click="handlePay">
          <text class="pay-text">立即支付 ¥{{ order.amount }}</text>
        </view>
        <!-- 服务中/待确认：显示确认完成按钮 -->
        <view class="confirm-btn" v-if="order.deliveryStatus >= 1 && order.deliveryStatus < 3" @click="handleConfirm">
          <text class="confirm-text">确认完成</text>
        </view>
        <!-- 联系卖家 -->
        <view class="chat-btn" v-if="order.sellerId" @click="goChat">
          <text class="chat-text">联系卖家</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      orderId: null,
      order: null,
      isLoading: true,
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20card%20minimal&image_size=square_hd'
    }
  },

  onLoad(options) {
    if (options.id) {
      this.orderId = options.id
      this.loadOrderDetail()
    }
  },

  methods: {
    /**
     * 加载订单详情
     */
    async loadOrderDetail() {
      this.isLoading = true
      try {
        const res = await request(`/user/order/detail/${this.orderId}`, {}, 'GET')
        if (res.code === 1 && res.data) {
          this.order = res.data
        } else {
          uni.showToast({ title: '订单不存在', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 模拟支付
     */
    async handlePay() {
      try {
        const res = await request('/user/order/payment', {
          orderNumber: this.order.number,
          payMethod: 1
        })
        if (res.code === 1) {
          uni.showToast({ title: '支付成功', icon: 'success' })
          this.loadOrderDetail()
        }
      } catch (e) {
        uni.showToast({ title: '支付失败', icon: 'none' })
      }
    },

    /**
     * 确认完成
     */
    handleConfirm() {
      uni.showModal({
        title: '确认完成',
        content: '确认服务已完成？确认后款项将打给卖家。',
        success: async (res) => {
          if (res.confirm) {
            // TODO: 后端补充确认完成接口
            uni.showToast({ title: '已确认', icon: 'success' })
            this.loadOrderDetail()
          }
        }
      })
    },

    /**
     * 联系卖家
     */
    goChat() {
      uni.navigateTo({
        url: `/pages/chat/index?sellerId=${this.order.sellerId}`
      })
    },

    getStatusIcon(status) {
      const map = { 1: '💳', 2: '⏳', 3: '🔧', 5: '✅', 6: '❌' }
      return map[status] || '📋'
    },

    getStatusText(status) {
      const map = { 1: '待付款', 2: '待接单', 3: '服务中', 5: '已完成', 6: '已取消' }
      return map[status] || '未知'
    },

    getStatusDesc(status) {
      const map = {
        1: '请尽快完成支付，超时订单将自动取消',
        2: '卖家正在确认订单，请耐心等待',
        3: '卖家正在为您提供服务',
        5: '服务已完成，感谢您的信任',
        6: '订单已取消'
      }
      return map[status] || ''
    },

    formatTime(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
    }
  }
}
</script>

<style scoped>
.detail-page { min-height: 100vh; background: #f5f5f7; }

.loading-wrap { display: flex; justify-content: center; padding-top: 200rpx; }
.loading-spinner { width: 48rpx; height: 48rpx; border: 4rpx solid #e0e0e0; border-top-color: #e94560; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 状态卡片 */
.status-card {
  padding: 40rpx 32rpx; margin: 20rpx 32rpx;
  border-radius: 20rpx; color: #fff;
}
.status-bg-1 { background: linear-gradient(135deg, #f59e0b, #d97706); }
.status-bg-2 { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.status-bg-3 { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.status-bg-5 { background: linear-gradient(135deg, #10b981, #059669); }
.status-bg-6 { background: linear-gradient(135deg, #9ca3af, #6b7280); }

.status-icon { font-size: 48rpx; display: block; margin-bottom: 8rpx; }
.status-text { display: block; font-size: 34rpx; font-weight: 700; }
.status-desc { display: block; font-size: 24rpx; opacity: 0.8; margin-top: 8rpx; }

/* 进度条 */
.progress-section { background: #fff; margin: 16rpx 32rpx; border-radius: 16rpx; padding: 28rpx 20rpx; }
.progress-bar { display: flex; align-items: center; }
.progress-step { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; }
.step-dot { width: 20rpx; height: 20rpx; border-radius: 50%; background: #ddd; margin-bottom: 8rpx; }
.progress-step.active .step-dot { background: #e94560; }
.step-label { font-size: 20rpx; color: #999; }
.progress-step.active .step-label { color: #e94560; font-weight: 600; }
.progress-line { flex: 1; height: 4rpx; background: #eee; margin: 0 4rpx; margin-bottom: 28rpx; }
.progress-line.active { background: #e94560; }

/* 明细 */
.detail-section { background: #fff; margin: 16rpx 32rpx; border-radius: 16rpx; padding: 24rpx; }
.section-title { margin-bottom: 16rpx; }
.title-text { font-size: 28rpx; font-weight: 600; color: #1a1a2e; }

.detail-item { display: flex; align-items: center; padding: 12rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.detail-item:last-child { border-bottom: none; }
.item-image { width: 100rpx; height: 100rpx; border-radius: 12rpx; margin-right: 16rpx; }
.item-info { flex: 1; }
.item-name { display: block; font-size: 26rpx; color: #333; }
.item-amount { display: block; font-size: 26rpx; color: #e94560; font-weight: 600; margin-top: 4rpx; }

/* 订单信息 */
.info-section { background: #fff; margin: 16rpx 32rpx; border-radius: 16rpx; padding: 24rpx; }
.info-row { display: flex; justify-content: space-between; padding: 10rpx 0; }
.info-label { font-size: 26rpx; color: #999; }
.info-value { font-size: 26rpx; color: #333; }
.paid { color: #10b981; }
.unpaid { color: #f59e0b; }
.total-row { border-top: 1rpx solid #f5f5f5; margin-top: 8rpx; padding-top: 16rpx; }
.total-amount { font-size: 32rpx; font-weight: 700; color: #e94560; }

/* 底部操作栏 */
.bottom-placeholder { height: 120rpx; }
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; gap: 16rpx; padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #fff; border-top: 1rpx solid #eee;
}
.pay-btn {
  flex: 1; display: flex; align-items: center; justify-content: center;
  height: 80rpx; background: linear-gradient(135deg, #e94560, #c23152);
  border-radius: 40rpx;
}
.pay-text { font-size: 28rpx; color: #fff; font-weight: 700; }

.confirm-btn {
  flex: 1; display: flex; align-items: center; justify-content: center;
  height: 80rpx; background: linear-gradient(135deg, #10b981, #059669);
  border-radius: 40rpx;
}
.confirm-text { font-size: 28rpx; color: #fff; font-weight: 700; }

.chat-btn {
  display: flex; align-items: center; justify-content: center;
  width: 200rpx; height: 80rpx;
  border: 2rpx solid #1a1a2e; border-radius: 40rpx;
}
.chat-text { font-size: 26rpx; color: #1a1a2e; font-weight: 600; }
</style>
