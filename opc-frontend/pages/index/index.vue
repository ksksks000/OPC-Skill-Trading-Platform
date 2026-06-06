/**
 * OPC 技能交易平台 - 首页/技能列表页
 *
 * 展示所有上架技能的列表，支持分类筛选和搜索
 * 作为 TabBar 的首页
 */
<template>
  <view class="home-page">
    <!-- 顶部搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap" @click="goAiSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">AI 智搜：说出你的需求...</text>
      </view>
    </view>

    <!-- 分类标签 -->
    <scroll-view class="category-scroll" scroll-x>
      <view class="category-list">
        <view
          class="cat-item"
          :class="{ active: currentCategory === null }"
          @click="currentCategory = null"
        >
          <text class="cat-text">全部</text>
        </view>
        <view
          class="cat-item"
          :class="{ active: currentCategory === cat.id }"
          v-for="cat in categories"
          :key="cat.id"
          @click="currentCategory = cat.id"
        >
          <text class="cat-text">{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 技能列表 -->
    <scroll-view class="skill-scroll" scroll-y @scrolltolower="loadMore">
      <view class="skill-grid">
        <view
          class="skill-card"
          v-for="skill in skillList"
          :key="skill.id"
          @click="goDetail(skill.id)"
        >
          <image class="card-image" :src="skill.image || defaultImage" mode="aspectFill" />
          <view class="card-info">
            <text class="card-name">{{ skill.name }}</text>
            <view class="card-tags">
              <text class="card-tag" v-for="(tag, i) in parseTags(skill.tags)" :key="i">{{ tag }}</text>
            </view>
            <view class="card-bottom">
              <text class="card-price">¥{{ skill.price }}</text>
              <text class="card-unit">/次</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="skillList.length > 0">
        <text class="load-text">{{ noMore ? '没有更多了' : '加载中...' }}</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-wrap" v-if="!isLoading && skillList.length === 0">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无技能，去 AI 智搜试试？</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { request } from '../../utils/api.js'

export default {
  data() {
    return {
      skillList: [],
      currentCategory: null,
      categories: [
        { id: 1, name: '心理咨询' },
        { id: 2, name: '职业发展' },
        { id: 3, name: '占卜命理' },
        { id: 4, name: '商业服务' },
        { id: 5, name: '设计创意' },
        { id: 6, name: '法律咨询' }
      ],
      page: 1,
      pageSize: 10,
      noMore: false,
      isLoading: false,
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20card%20minimal&image_size=square_hd'
    }
  },

  onShow() {
    this.page = 1
    this.skillList = []
    this.noMore = false
    this.loadSkills()
  },

  watch: {
    currentCategory() {
      this.page = 1
      this.skillList = []
      this.noMore = false
      this.loadSkills()
    }
  },

  methods: {
    async loadSkills() {
      if (this.isLoading || this.noMore) return
      this.isLoading = true

      try {
        const params = { page: this.page, pageSize: this.pageSize }
        if (this.currentCategory) params.categoryId = this.currentCategory

        const res = await request('/admin/skill/page', params, 'GET')
        if (res.code === 1 && res.data) {
          const records = res.data.records || []
          if (this.page === 1) {
            this.skillList = records
          } else {
            this.skillList = [...this.skillList, ...records]
          }
          if (records.length < this.pageSize) this.noMore = true
        }
      } catch (e) {
        console.error('加载技能列表失败:', e)
      } finally {
        this.isLoading = false
      }
    },

    loadMore() {
      if (!this.noMore) {
        this.page++
        this.loadSkills()
      }
    },

    parseTags(tagsStr) {
      if (!tagsStr) return []
      try {
        const tags = JSON.parse(tagsStr)
        return Array.isArray(tags) ? tags.slice(0, 2) : []
      } catch { return [] }
    },

    goDetail(id) {
      uni.navigateTo({ url: `/pages/skill/detail?id=${id}` })
    },

    goAiSearch() {
      uni.navigateTo({ url: '/pages/ai-search/index' })
    }
  }
}
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f5f7; }

.search-bar { padding: 24rpx 32rpx 16rpx; background: #fff; }
.search-input-wrap {
  display: flex; align-items: center;
  background: #f5f5f7; border-radius: 36rpx;
  padding: 0 24rpx; height: 72rpx;
}
.search-icon { font-size: 28rpx; margin-right: 12rpx; }
.search-placeholder { font-size: 26rpx; color: #999; }

.category-scroll { background: #fff; padding-bottom: 16rpx; }
.category-list { display: flex; padding: 0 32rpx; white-space: nowrap; }
.cat-item {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 10rpx 28rpx; margin-right: 16rpx;
  background: #f5f5f7; border-radius: 24rpx;
}
.cat-item.active { background: #e94560; }
.cat-text { font-size: 24rpx; color: #666; }
.cat-item.active .cat-text { color: #fff; font-weight: 600; }

.skill-scroll { height: calc(100vh - 200rpx); }
.skill-grid { padding: 20rpx 32rpx; }

.skill-card {
  display: flex; background: #fff; border-radius: 20rpx;
  padding: 20rpx; margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.skill-card:active { transform: scale(0.98); }

.card-image { width: 160rpx; height: 160rpx; border-radius: 12rpx; flex-shrink: 0; }
.card-info { flex: 1; margin-left: 20rpx; display: flex; flex-direction: column; justify-content: space-between; }
.card-name { font-size: 28rpx; font-weight: 600; color: #1a1a2e; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-tags { display: flex; gap: 8rpx; margin-top: 8rpx; }
.card-tag { font-size: 20rpx; color: #e94560; background: rgba(233,69,96,0.08); padding: 2rpx 12rpx; border-radius: 6rpx; }
.card-bottom { display: flex; align-items: baseline; margin-top: 8rpx; }
.card-price { font-size: 30rpx; font-weight: 700; color: #e94560; }
.card-unit { font-size: 20rpx; color: #999; margin-left: 4rpx; }

.load-status { text-align: center; padding: 24rpx; }
.load-text { font-size: 24rpx; color: #999; }

.empty-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
