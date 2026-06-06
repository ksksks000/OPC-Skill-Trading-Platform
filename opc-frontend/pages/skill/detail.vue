/**
 * OPC 技能交易平台 - 技能详情页
 *
 * 功能：
 * 1. 展示技能的详细信息（名称、价格、描述、标签、卖家信息）
 * 2. 顶部图片展示
 * 3. 富文本描述解析
 * 4. 底部悬浮操作栏：聊一聊 + 立即购买
 */
<template>
  <view class="detail-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :style="{ opacity: scrollOpacity }">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">←</text>
      </view>
      <text class="nav-title">技能详情</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-wrap" v-if="isLoading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 内容区域 -->
    <view class="detail-content" v-if="!isLoading && skill">
      <!-- 顶部图片区域 -->
      <view class="image-section">
        <image
          class="cover-image"
          :src="skill.image || defaultImage"
          mode="aspectFill"
        />
        <!-- 状态标签 -->
        <view class="status-badge" :class="skill.status === 1 ? 'online' : 'offline'">
          <text class="status-text">{{ skill.status === 1 ? '在售' : '已下架' }}</text>
        </view>
      </view>

      <!-- 价格与基本信息 -->
      <view class="info-section">
        <view class="price-row">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ skill.price }}</text>
          <text class="price-unit">/次</text>
        </view>
        <text class="skill-title">{{ skill.name }}</text>
        <view class="tag-row">
          <text class="tag" v-for="(tag, idx) in parseTags(skill.tags)" :key="idx">{{ tag }}</text>
        </view>
      </view>

      <!-- 卖家信息 -->
      <view class="seller-section" @click="goToChat">
        <view class="seller-avatar">
          <text class="avatar-text">{{ (skill.sellerName || '卖')[0] }}</text>
        </view>
        <view class="seller-info">
          <text class="seller-name">{{ skill.sellerName || '专业卖家' }}</text>
          <text class="seller-desc">点击与TA聊聊</text>
        </view>
        <text class="seller-arrow">→</text>
      </view>

      <!-- 技能描述 -->
      <view class="desc-section">
        <view class="section-title-row">
          <view class="section-title-bar"></view>
          <text class="section-title">服务详情</text>
        </view>
        <view class="desc-content">
          <!-- 富文本解析：支持简单的 HTML 标签 -->
          <rich-text :nodes="formatDescription(skill.description)" />
        </view>
      </view>

      <!-- 温馨提示 -->
      <view class="tips-section">
        <view class="section-title-row">
          <view class="section-title-bar tips-bar"></view>
          <text class="section-title">温馨提示</text>
        </view>
        <view class="tips-content">
          <text class="tip-item">· 下单前建议先与卖家沟通确认服务内容</text>
          <text class="tip-item">· 服务过程中如有问题可申请平台介入</text>
          <text class="tip-item">· 完成服务后请及时确认，保障双方权益</text>
        </view>
      </view>

      <!-- 底部占位，防止悬浮栏遮挡内容 -->
      <view class="bottom-placeholder"></view>
    </view>

    <!-- 底部悬浮操作栏 -->
    <view class="bottom-bar" v-if="!isLoading && skill">
      <view class="bar-left">
        <!-- 聊一聊按钮 -->
        <view class="chat-btn" @click="goToChat">
          <text class="chat-icon">💬</text>
          <text class="chat-text">聊一聊</text>
        </view>
      </view>
      <view class="bar-right">
        <!-- 立即购买按钮 -->
        <view class="buy-btn" :class="{ disabled: skill.status !== 1 }" @click="handleBuy">
          <text class="buy-text">{{ skill.status === 1 ? '立即购买' : '已下架' }}</text>
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
      // 技能ID
      skillId: null,
      // 技能详情
      skill: null,
      // 是否加载中
      isLoading: true,
      // 滚动透明度（导航栏）
      scrollOpacity: 0,
      // 默认图片
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20detail%20banner%20minimal&image_size=landscape_16_9'
    }
  },

  onLoad(options) {
    // 从路由参数获取技能ID
    if (options.id) {
      this.skillId = options.id
      this.loadSkillDetail()
    }
  },

  // 监听页面滚动，动态调整导航栏透明度
  onPageScroll(e) {
    this.scrollOpacity = Math.min(e.scrollTop / 200, 1)
  },

  methods: {
    /**
     * 加载技能详情
     */
    async loadSkillDetail() {
      this.isLoading = true
      try {
        const res = await request(`/admin/skill/${this.skillId}`, {}, 'GET')
        if (res.code === 1 && res.data) {
          this.skill = res.data
        } else {
          uni.showToast({ title: '技能不存在', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 解析标签 JSON
     */
    parseTags(tagsStr) {
      if (!tagsStr) return []
      try {
        const tags = JSON.parse(tagsStr)
        return Array.isArray(tags) ? tags : []
      } catch {
        return []
      }
    },

    /**
     * 格式化描述文本
     * 将纯文本转为适合 rich-text 渲染的 HTML
     */
    formatDescription(desc) {
      if (!desc) return '<p style="color:#999;">暂无详细描述</p>'
      // 如果已经是 HTML，直接返回
      if (desc.includes('<')) return desc
      // 纯文本：将换行转为 <br>
      return desc.split('\n').map(p => `<p style="margin-bottom:16rpx;">${p}</p>`).join('')
    },

    /**
     * 跳转到聊天页面
     */
    goToChat() {
      if (!this.skill) return
      uni.navigateTo({
        url: `/pages/chat/index?sellerId=${this.skill.sellerId}&skillId=${this.skill.id}`
      })
    },

    /**
     * 立即购买
     */
    async handleBuy() {
      if (!this.skill || this.skill.status !== 1) return

      // 检查是否登录
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }

      try {
        const res = await request('/user/order/submit', {
          skillId: this.skill.id,
          payMethod: 1,
          remark: '',
          amount: this.skill.price
        })

        if (res.code === 1 && res.data) {
          uni.showToast({ title: '下单成功', icon: 'success' })
          // 跳转到订单详情或支付页
          setTimeout(() => {
            uni.navigateTo({
              url: `/pages/order/detail?id=${res.data.id}`
            })
          }, 1500)
        } else {
          uni.showToast({ title: res.msg || '下单失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '下单失败', icon: 'none' })
      }
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
.detail-page {
  min-height: 100vh;
  background: #f5f5f7;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  padding-top: var(--status-bar-height, 44rpx);
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1rpx solid rgba(0,0,0,0.05);
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-back-icon {
  font-size: 36rpx;
  color: #1a1a2e;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.nav-placeholder {
  width: 64rpx;
}

/* 加载状态 */
.loading-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #e0e0e0;
  border-top-color: #e94560;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 24rpx;
  font-size: 26rpx;
  color: #999;
}

/* 封面图 */
.image-section {
  position: relative;
  width: 100%;
  height: 480rpx;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.status-badge.online {
  background: rgba(16,185,129,0.9);
}

.status-badge.offline {
  background: rgba(156,163,175,0.9);
}

.status-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 信息区域 */
.info-section {
  background: #ffffff;
  padding: 32rpx;
  border-radius: 0 0 32rpx 32rpx;
  margin-top: -24rpx;
  position: relative;
  z-index: 1;
}

.price-row {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 28rpx;
  font-weight: 700;
  color: #e94560;
}

.price-value {
  font-size: 52rpx;
  font-weight: 800;
  color: #e94560;
  margin-left: 4rpx;
}

.price-unit {
  font-size: 24rpx;
  color: #999;
  margin-left: 8rpx;
}

.skill-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1a2e;
  margin-top: 16rpx;
  line-height: 1.5;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
}

.tag {
  font-size: 22rpx;
  color: #e94560;
  background: rgba(233,69,96,0.08);
  padding: 6rpx 18rpx;
  border-radius: 8rpx;
}

/* 卖家信息 */
.seller-section {
  display: flex;
  align-items: center;
  background: #ffffff;
  margin-top: 20rpx;
  padding: 28rpx 32rpx;
}

.seller-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: 700;
}

.seller-info {
  flex: 1;
  margin-left: 20rpx;
}

.seller-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.seller-desc {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.seller-arrow {
  font-size: 32rpx;
  color: #ccc;
}

/* 描述区域 */
.desc-section {
  background: #ffffff;
  margin-top: 20rpx;
  padding: 32rpx;
}

.section-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title-bar {
  width: 6rpx;
  height: 32rpx;
  background: #e94560;
  border-radius: 3rpx;
  margin-right: 16rpx;
}

.tips-bar {
  background: #f59e0b;
}

.section-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1a2e;
}

.desc-content {
  font-size: 28rpx;
  color: #444;
  line-height: 1.8;
}

/* 温馨提示 */
.tips-section {
  background: #ffffff;
  margin-top: 20rpx;
  padding: 32rpx;
}

.tips-content {
  padding-left: 8rpx;
}

.tip-item {
  display: block;
  font-size: 26rpx;
  color: #888;
  line-height: 2.2;
}

/* 底部占位 */
.bottom-placeholder {
  height: 140rpx;
}

/* 底部悬浮操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 16rpx 32rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-top: 1rpx solid rgba(0,0,0,0.05);
  z-index: 100;
}

.bar-left {
  flex: 1;
}

.chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200rpx;
  height: 80rpx;
  border: 2rpx solid #1a1a2e;
  border-radius: 40rpx;
}

.chat-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.chat-text {
  font-size: 26rpx;
  color: #1a1a2e;
  font-weight: 600;
}

.bar-right {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.buy-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 320rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #e94560, #c23152);
  border-radius: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(233,69,96,0.3);
}

.buy-btn.disabled {
  background: #ccc;
  box-shadow: none;
}

.buy-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 700;
}
</style>
