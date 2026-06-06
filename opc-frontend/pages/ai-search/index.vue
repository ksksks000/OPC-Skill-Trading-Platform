/**
 * OPC 技能交易平台 - AI 智搜页面
 *
 * 核心功能：
 * 1. 用户输入自然语言查询
 * 2. 通过 SSE 接收 AI 流式响应
 * 3. 逐字渲染 AI 推荐语（打字机效果）
 * 4. 解析 AI 返回的 JSON，展示匹配的技能卡片列表
 * 5. 点击卡片跳转到技能详情页
 */
<template>
  <view class="ai-search-page">
    <!-- 顶部搜索区域 -->
    <view class="search-header">
      <view class="header-bg"></view>
      <view class="header-content">
        <text class="page-title">AI 智搜</text>
        <text class="page-subtitle">说出你的需求，AI 为你精准匹配</text>

        <!-- 搜索输入框 -->
        <view class="search-box">
          <view class="search-input-wrap">
            <text class="search-icon">🔍</text>
            <input
              class="search-input"
              v-model="query"
              placeholder="试试：我想找一位心理咨询师..."
              :disabled="isLoading"
              confirm-type="search"
              @confirm="handleSearch"
            />
            <view
              class="search-btn"
              :class="{ active: query.trim() && !isLoading }"
              @click="handleSearch"
            >
              <text class="search-btn-text">{{ isLoading ? '搜索中' : '搜索' }}</text>
            </view>
          </view>
        </view>

        <!-- 快捷标签 -->
        <view class="quick-tags">
          <view
            class="tag-item"
            v-for="tag in quickTags"
            :key="tag"
            @click="quickSearch(tag)"
          >
            <text class="tag-text">{{ tag }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载动画 -->
    <view class="loading-section" v-if="isLoading && !recommendationText">
      <view class="loading-animation">
        <view class="loading-dot" v-for="i in 3" :key="i" :style="{ animationDelay: (i * 0.2) + 's' }"></view>
      </view>
      <text class="loading-text">🏃 别急，AI 在穿拖鞋狂奔！正在为你分析需求...</text>
    </view>

    <!-- AI 推荐语区域（打字机效果） -->
    <view class="recommendation-section" v-if="recommendationText">
      <view class="section-header">
        <text class="section-icon">✨</text>
        <text class="section-title">AI 为你推荐</text>
      </view>
      <view class="recommendation-card">
        <text class="recommendation-text">{{ recommendationText }}<text class="cursor" v-if="isTyping">|</text></text>
      </view>
    </view>

    <!-- 技能卡片列表 -->
    <view class="skill-list-section" v-if="skillList.length > 0">
      <view class="section-header">
        <text class="section-icon">🎯</text>
        <text class="section-title">匹配的技能</text>
        <text class="section-count">共 {{ skillList.length }} 个</text>
      </view>

      <view class="skill-card" v-for="skill in skillList" :key="skill.id" @click="goToDetail(skill.id)">
        <!-- 左侧封面图 -->
        <view class="skill-image-wrap">
          <image
            class="skill-image"
            :src="skill.image || defaultImage"
            mode="aspectFill"
          />
        </view>
        <!-- 右侧信息 -->
        <view class="skill-info">
          <text class="skill-name">{{ skill.name }}</text>
          <view class="skill-tags">
            <text class="skill-tag" v-for="(tag, idx) in parseTags(skill.tags)" :key="idx">{{ tag }}</text>
          </view>
          <view class="skill-bottom">
            <text class="skill-price">¥{{ skill.price }}</text>
            <text class="skill-seller">{{ skill.sellerName || '专业卖家' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-section" v-if="hasSearched && !isLoading && !recommendationText">
      <text class="empty-icon">🤔</text>
      <text class="empty-text">没有找到匹配的技能，换个说法试试？</text>
    </view>

    <!-- 错误提示 -->
    <view class="error-section" v-if="errorMsg">
      <text class="error-icon">⚠️</text>
      <text class="error-text">{{ errorMsg }}</text>
      <view class="retry-btn" @click="handleSearch">
        <text class="retry-text">重试</text>
      </view>
    </view>
  </view>
</template>

<script>
import { fetchSSE, request } from '../../utils/api.js'

export default {
  data() {
    return {
      // 用户输入的查询内容
      query: '',
      // 是否正在加载
      isLoading: false,
      // 是否正在打字
      isTyping: false,
      // AI 推荐语（逐字渲染）
      recommendationText: '',
      // 匹配的技能列表
      skillList: [],
      // 是否已搜索过
      hasSearched: false,
      // 错误信息
      errorMsg: '',
      // AI 返回的完整文本（用于 JSON 解析）
      fullText: '',
      // 默认图片
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20placeholder%20icon%20minimal&image_size=square_hd',
      // 快捷搜索标签
      quickTags: ['心理咨询', '简历优化', '塔罗牌占卜', 'Python编程', '法律咨询']
    }
  },

  methods: {
    /**
     * 发起 AI 搜索
     * 通过 SSE 流式接口获取 AI 响应
     */
    async handleSearch() {
      if (!this.query.trim() || this.isLoading) return

      // 重置状态
      this.isLoading = true
      this.isTyping = true
      this.recommendationText = ''
      this.skillList = []
      this.hasSearched = true
      this.errorMsg = ''
      this.fullText = ''

      try {
        // 调用 SSE 流式接口
        await fetchSSE('/api/ai/search-stream', { query: this.query }, {
          // 每收到一个文本片段，追加到推荐语（打字机效果）
          onChunk: (chunk) => {
            this.fullText += chunk
            // 逐字追加到推荐语，实现打字机效果
            this.recommendationText += chunk
          },
          // 流结束，解析 JSON 提取技能列表
          onDone: () => {
            this.isTyping = false
            this.isLoading = false
            this.parseResult()
          },
          // 错误处理
          onError: (err) => {
            console.error('SSE 错误:', err)
            this.isLoading = false
            this.isTyping = false
            this.errorMsg = 'AI 搜索出了点问题，请稍后再试'
          }
        })
      } catch (e) {
        this.isLoading = false
        this.isTyping = false
        this.errorMsg = '网络异常，请检查连接'
      }
    },

    /**
     * 解析 AI 返回的 JSON 结果
     * 提取 recommendationText 和 skillIds，查询技能详情
     */
    async parseResult() {
      try {
        // 清理可能的 markdown 代码块标记
        let jsonStr = this.fullText.trim()
        jsonStr = jsonStr.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim()

        const result = JSON.parse(jsonStr)

        // 更新推荐语（用解析后的完整文本替换逐字累积的文本）
        if (result.recommendationText) {
          this.recommendationText = result.recommendationText
        }

        // 根据 skillIds 查询技能详情
        if (result.skillIds && result.skillIds.length > 0) {
          await this.fetchSkillDetails(result.skillIds)
        }
      } catch (e) {
        // JSON 解析失败，保留已逐字渲染的文本
        console.warn('AI 返回内容非标准 JSON，保留原始文本', e)
      }
    },

    /**
     * 根据 ID 列表查询技能详情
     */
    async fetchSkillDetails(ids) {
      try {
        // 逐个查询技能详情（也可以改为批量接口）
        const promises = ids.map(id =>
          request(`/admin/skill/${id}`, {}, 'GET').catch(() => null)
        )
        const results = await Promise.all(promises)
        this.skillList = results
          .filter(r => r && r.code === 1 && r.data)
          .map(r => r.data)
      } catch (e) {
        console.error('查询技能详情失败:', e)
      }
    },

    /**
     * 解析技能标签（JSON 字符串 -> 数组）
     */
    parseTags(tagsStr) {
      if (!tagsStr) return []
      try {
        const tags = JSON.parse(tagsStr)
        return Array.isArray(tags) ? tags.slice(0, 3) : []
      } catch {
        return []
      }
    },

    /**
     * 快捷搜索 - 点击标签直接搜索
     */
    quickSearch(tag) {
      this.query = tag
      this.handleSearch()
    },

    /**
     * 跳转到技能详情页
     */
    goToDetail(id) {
      uni.navigateTo({
        url: `/pages/skill/detail?id=${id}`
      })
    }
  }
}
</script>

<style scoped>
/* 页面容器 */
.ai-search-page {
  min-height: 100vh;
  background: #f5f5f7;
}

/* 顶部搜索区域 */
.search-header {
  position: relative;
  padding: 0 32rpx 40rpx;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 420rpx;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 0 0 48rpx 48rpx;
}

.header-content {
  position: relative;
  z-index: 1;
  padding-top: 80rpx;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2rpx;
}

.page-subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255,255,255,0.6);
  margin-top: 8rpx;
}

/* 搜索框 */
.search-box {
  margin-top: 36rpx;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.12);
  border-radius: 48rpx;
  padding: 0 12rpx 0 28rpx;
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(255,255,255,0.15);
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  height: 88rpx;
  font-size: 28rpx;
  color: #ffffff;
}

.search-input::placeholder {
  color: rgba(255,255,255,0.4);
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 120rpx;
  height: 64rpx;
  background: rgba(255,255,255,0.15);
  border-radius: 32rpx;
  transition: all 0.3s;
}

.search-btn.active {
  background: #e94560;
}

.search-btn-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 600;
}

/* 快捷标签 */
.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 24rpx;
}

.tag-item {
  padding: 10rpx 24rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 24rpx;
  border: 1rpx solid rgba(255,255,255,0.12);
}

.tag-text {
  font-size: 24rpx;
  color: rgba(255,255,255,0.75);
}

/* 加载动画 */
.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 60rpx;
}

.loading-animation {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.loading-dot {
  width: 16rpx;
  height: 16rpx;
  background: #e94560;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.loading-text {
  font-size: 26rpx;
  color: #888;
  text-align: center;
}

/* AI 推荐语 */
.recommendation-section {
  padding: 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
}

.recommendation-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.06);
  border-left: 6rpx solid #e94560;
}

.recommendation-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
}

/* 打字机光标 */
.cursor {
  animation: blink 1s infinite;
  color: #e94560;
  font-weight: 300;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* 技能卡片列表 */
.skill-list-section {
  padding: 0 32rpx 32rpx;
}

.section-count {
  font-size: 24rpx;
  color: #999;
  margin-left: auto;
}

.skill-card {
  display: flex;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 16rpx rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.skill-card:active {
  transform: scale(0.98);
}

.skill-image-wrap {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.skill-image {
  width: 100%;
  height: 100%;
}

.skill-info {
  flex: 1;
  margin-left: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.skill-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a2e;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
}

.skill-tag {
  font-size: 20rpx;
  color: #e94560;
  background: rgba(233,69,96,0.08);
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}

.skill-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
}

.skill-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #e94560;
}

.skill-seller {
  font-size: 22rpx;
  color: #999;
}

/* 空状态 */
.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 错误提示 */
.error-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 60rpx;
}

.error-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.error-text {
  font-size: 26rpx;
  color: #e94560;
  text-align: center;
  margin-bottom: 32rpx;
}

.retry-btn {
  padding: 16rpx 48rpx;
  background: #e94560;
  border-radius: 32rpx;
}

.retry-text {
  font-size: 26rpx;
  color: #ffffff;
}
</style>
