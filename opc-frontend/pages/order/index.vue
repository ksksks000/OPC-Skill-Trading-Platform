/**
 * OPC 技能交易平台 - 订单列表页
 *
 * 展示当前用户的订单列表，按状态分类
 */
<template>
  <view class="order-page">
    <!-- 状态标签切换 -->
    <view class="tab-bar">
      <view class="tab-item" :class="{ active: currentTab === null }" @click="switchTab(null)">
        <text class="tab-text">全部</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">
        <text class="tab-text">待付款</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 2 }" @click="switchTab(2)">
        <text class="tab-text">待接单</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 3 }" @click="switchTab(3)">
        <text class="tab-text">服务中</text>
      </view>
      <view class="tab-item" :class="{ active: currentTab === 5 }" @click="switchTab(5)">
        <text class="tab-text">已完成</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view class="order-list" scroll-y>
      <view class="order-card" v-for="order in orderList" :key="order.id" @click="goDetail(order.id)">
        <view class="order-header">
          <text class="order-number">订单号：{{ order.number }}</text>
          <text class="order-status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
        </view>
        <view class="order-body">
          <text class="order-amount">¥{{ order.amount }}</text>
          <text class="order-time">{{ order.orderTime }}</text>
        </view>
        <view class="order-remark" v-if="order.remark">
          <text class="remark-text">备注：{{ order.remark }}</text>
        </view>
      </view>

      <view class="empty-wrap" v-if="!isLoading && orderList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      orderList: [],
      currentTab: null,
      isLoading: false
    }
  },

  onShow() {
    this.loadOrders()
  },

  methods: {
    async loadOrders() {
      this.isLoading = true
      try {
        // TODO: 后端需补充用户端订单列表接口
        // 当前使用管理端接口模拟
        const res = await request('/user/order/list', { page: 1, pageSize: 20 }, 'GET')
        if (res.code === 1 && res.data) {
          this.orderList = res.data.records || []
        }
      } catch (e) {
        console.error('加载订单失败:', e)
      } finally {
        this.isLoading = false
      }
    },

    switchTab(status) {
      this.currentTab = status
      this.loadOrders()
    },

    getStatusText(status) {
      const map = { 1: '待付款', 2: '待接单', 3: '服务中', 5: '已完成', 6: '已取消' }
      return map[status] || '未知'
    },

    goDetail(id) {
      uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
    }
  }
}
</script>

<style scoped>
.order-page { min-height: 100vh; background: #f5f5f7; }

.tab-bar {
  display: flex; background: #fff; padding: 0 16rpx;
  border-bottom: 1rpx solid #eee;
}
.tab-item {
  flex: 1; display: flex; align-items: center; justify-content: center;
  height: 88rpx; position: relative;
}
.tab-item.active::after {
  content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 48rpx; height: 4rpx; background: #e94560; border-radius: 2rpx;
}
.tab-text { font-size: 26rpx; color: #666; }
.tab-item.active .tab-text { color: #e94560; font-weight: 600; }

.order-list { padding: 20rpx 32rpx; }
.order-card {
  background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.order-header { display: flex; justify-content: space-between; align-items: center; }
.order-number { font-size: 24rpx; color: #999; }
.order-status { font-size: 24rpx; font-weight: 600; }
.status-1 { color: #f59e0b; }
.status-2 { color: #3b82f6; }
.status-3 { color: #8b5cf6; }
.status-5 { color: #10b981; }
.status-6 { color: #999; }

.order-body { display: flex; justify-content: space-between; align-items: baseline; margin-top: 16rpx; }
.order-amount { font-size: 32rpx; font-weight: 700; color: #e94560; }
.order-time { font-size: 22rpx; color: #bbb; }

.order-remark { margin-top: 12rpx; padding-top: 12rpx; border-top: 1rpx solid #f5f5f5; }
.remark-text { font-size: 24rpx; color: #999; }

.empty-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
