/**
 * OPC 技能交易平台 - 卖家技能管理页
 *
 * 功能：
 * 1. 展示当前卖家已发布的所有技能
 * 2. 支持新增、编辑、上下架、删除技能
 * 3. 点击技能卡片进入编辑模式
 */
<template>
  <view class="manage-page">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <text class="page-title">技能管理</text>
      <view class="add-btn" @click="goAdd">
        <text class="add-text">+ 新增技能</text>
      </view>
    </view>

    <!-- 统计信息 -->
    <view class="stats-row">
      <view class="stat-item">
        <text class="stat-num">{{ totalCount }}</text>
        <text class="stat-label">全部</text>
      </view>
      <view class="stat-item">
        <text class="stat-num online-num">{{ onlineCount }}</text>
        <text class="stat-label">在售</text>
      </view>
      <view class="stat-item">
        <text class="stat-num offline-num">{{ offlineCount }}</text>
        <text class="stat-label">已下架</text>
      </view>
    </view>

    <!-- 技能列表 -->
    <scroll-view class="skill-list" scroll-y @scrolltolower="loadMore">
      <view class="skill-item" v-for="skill in skillList" :key="skill.id">
        <!-- 左侧封面 -->
        <image class="skill-img" :src="skill.image || defaultImage" mode="aspectFill" @click="goEdit(skill.id)" />

        <!-- 中间信息 -->
        <view class="skill-info" @click="goEdit(skill.id)">
          <text class="skill-name">{{ skill.name }}</text>
          <text class="skill-price">¥{{ skill.price }}/次</text>
          <view class="skill-tags">
            <text class="skill-tag" v-for="(tag, i) in parseTags(skill.tags)" :key="i">{{ tag }}</text>
          </view>
        </view>

        <!-- 右侧操作 -->
        <view class="skill-actions">
          <!-- 上下架切换 -->
          <view class="action-btn toggle-btn" :class="skill.status === 1 ? 'offline' : 'online'" @click.stop="toggleStatus(skill)">
            <text class="action-text">{{ skill.status === 1 ? '下架' : '上架' }}</text>
          </view>
          <!-- 删除 -->
          <view class="action-btn delete-btn" @click.stop="deleteSkill(skill)">
            <text class="action-text">删除</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="skillList.length > 0">
        <text class="load-text">{{ noMore ? '没有更多了' : '加载中...' }}</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-wrap" v-if="!isLoading && skillList.length === 0">
        <text class="empty-icon">🎯</text>
        <text class="empty-text">还没有发布技能，点击右上角新增</text>
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
      page: 1,
      pageSize: 20,
      noMore: false,
      isLoading: false,
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20card%20minimal&image_size=square_hd'
    }
  },

  computed: {
    totalCount() { return this.skillList.length },
    onlineCount() { return this.skillList.filter(s => s.status === 1).length },
    offlineCount() { return this.skillList.filter(s => s.status !== 1).length }
  },

  onShow() {
    this.page = 1
    this.skillList = []
    this.noMore = false
    this.loadSkills()
  },

  methods: {
    /**
     * 加载技能列表（管理端接口，需管理员 token）
     */
    async loadSkills() {
      if (this.isLoading || this.noMore) return
      this.isLoading = true

      try {
        const res = await request('/admin/skill/page', { page: this.page, pageSize: this.pageSize }, 'GET')
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

    /**
     * 切换技能上下架状态
     */
    async toggleStatus(skill) {
      const newStatus = skill.status === 1 ? 0 : 1
      try {
        const res = await request(`/admin/skill/status/${newStatus}?id=${skill.id}`, {}, 'POST')
        if (res.code === 1) {
          skill.status = newStatus
          uni.showToast({ title: newStatus === 1 ? '已上架' : '已下架', icon: 'success' })
        }
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },

    /**
     * 删除技能
     */
    deleteSkill(skill) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除技能"${skill.name}"吗？删除后不可恢复。`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await request(`/admin/skill?id=${skill.id}`, {}, 'DELETE')
              if (result.code === 1) {
                this.skillList = this.skillList.filter(s => s.id !== skill.id)
                uni.showToast({ title: '已删除', icon: 'success' })
              }
            } catch (e) {
              uni.showToast({ title: '删除失败', icon: 'none' })
            }
          }
        }
      })
    },

    parseTags(tagsStr) {
      if (!tagsStr) return []
      try {
        const tags = JSON.parse(tagsStr)
        return Array.isArray(tags) ? tags.slice(0, 2) : []
      } catch { return [] }
    },

    goAdd() {
      uni.navigateTo({ url: '/pages/skill/edit' })
    },

    goEdit(id) {
      uni.navigateTo({ url: `/pages/skill/edit?id=${id}` })
    }
  }
}
</script>

<style scoped>
.manage-page { min-height: 100vh; background: #f5f5f7; }

.top-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 24rpx 32rpx; background: #fff;
}
.page-title { font-size: 34rpx; font-weight: 700; color: #1a1a2e; }
.add-btn {
  padding: 12rpx 28rpx; background: linear-gradient(135deg, #e94560, #c23152);
  border-radius: 28rpx;
}
.add-text { font-size: 24rpx; color: #fff; font-weight: 600; }

.stats-row {
  display: flex; background: #fff; padding: 24rpx 32rpx; margin-bottom: 16rpx;
  border-top: 1rpx solid #f5f5f5;
}
.stat-item { flex: 1; text-align: center; }
.stat-num { display: block; font-size: 36rpx; font-weight: 700; color: #1a1a2e; }
.online-num { color: #10b981; }
.offline-num { color: #999; }
.stat-label { display: block; font-size: 22rpx; color: #999; margin-top: 4rpx; }

.skill-list { padding: 0 32rpx; }

.skill-item {
  display: flex; align-items: center; background: #fff;
  border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.skill-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; flex-shrink: 0; }

.skill-info { flex: 1; margin-left: 16rpx; }
.skill-name { display: block; font-size: 28rpx; font-weight: 600; color: #1a1a2e; }
.skill-price { display: block; font-size: 26rpx; color: #e94560; margin-top: 4rpx; }
.skill-tags { display: flex; gap: 6rpx; margin-top: 6rpx; }
.skill-tag { font-size: 18rpx; color: #e94560; background: rgba(233,69,96,0.08); padding: 2rpx 10rpx; border-radius: 4rpx; }

.skill-actions { display: flex; flex-direction: column; gap: 10rpx; margin-left: 12rpx; }
.action-btn { padding: 8rpx 20rpx; border-radius: 16rpx; }
.action-text { font-size: 22rpx; font-weight: 600; }

.toggle-btn.online { background: rgba(16,185,129,0.1); }
.toggle-btn.online .action-text { color: #10b981; }
.toggle-btn.offline { background: rgba(156,163,175,0.1); }
.toggle-btn.offline .action-text { color: #9ca3af; }
.delete-btn { background: rgba(239,68,68,0.1); }
.delete-btn .action-text { color: #ef4444; }

.load-status { text-align: center; padding: 24rpx; }
.load-text { font-size: 24rpx; color: #999; }

.empty-wrap { display: flex; flex-direction: column; align-items: center; padding-top: 160rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
