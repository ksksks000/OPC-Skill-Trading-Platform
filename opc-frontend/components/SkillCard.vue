/**
 * OPC 技能交易平台 - 技能卡片组件
 *
 * 可复用的技能展示卡片，用于首页列表和搜索结果
 * 支持横向和纵向两种布局模式
 *
 * Props:
 * - skill: 技能对象
 * - mode: 'horizontal'(默认) | 'vertical'
 *
 * Events:
 * - click: 点击卡片时触发，传递技能ID
 */
<template>
  <view class="skill-card" :class="mode" @click="$emit('click', skill.id)">
    <!-- 封面图 -->
    <view class="card-image-wrap">
      <image class="card-image" :src="skill.image || defaultImage" mode="aspectFill" />
      <!-- 状态角标 -->
      <view class="status-tag" v-if="skill.status !== undefined && skill.status !== 1">
        <text class="status-tag-text">已下架</text>
      </view>
    </view>
    <!-- 信息区 -->
    <view class="card-body">
      <text class="card-name">{{ skill.name }}</text>
      <!-- 标签 -->
      <view class="card-tags" v-if="parseTags(skill.tags).length">
        <text class="card-tag" v-for="(tag, i) in parseTags(skill.tags)" :key="i">{{ tag }}</text>
      </view>
      <!-- 价格 -->
      <view class="card-price-row">
        <text class="card-price">¥{{ skill.price }}</text>
        <text class="card-unit">/次</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SkillCard',
  props: {
    skill: { type: Object, required: true },
    mode: { type: String, default: 'horizontal' }
  },
  data() {
    return {
      defaultImage: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20service%20card%20minimal&image_size=square_hd'
    }
  },
  methods: {
    parseTags(tagsStr) {
      if (!tagsStr) return []
      try {
        const tags = JSON.parse(tagsStr)
        return Array.isArray(tags) ? tags.slice(0, 3) : []
      } catch { return [] }
    }
  }
}
</script>

<style scoped>
.skill-card {
  display: flex; background: #fff; border-radius: 20rpx;
  overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  transition: transform 0.2s;
}
.skill-card:active { transform: scale(0.98); }

/* 横向布局 */
.horizontal { flex-direction: row; padding: 20rpx; }
.horizontal .card-image-wrap { width: 160rpx; height: 160rpx; flex-shrink: 0; }
.horizontal .card-body { flex: 1; margin-left: 20rpx; }

/* 纵向布局 */
.vertical { flex-direction: column; }
.vertical .card-image-wrap { width: 100%; height: 240rpx; }
.vertical .card-body { padding: 16rpx 20rpx 20rpx; }

.card-image-wrap { position: relative; border-radius: 12rpx; overflow: hidden; }
.card-image { width: 100%; height: 100%; }

.status-tag {
  position: absolute; top: 8rpx; right: 8rpx;
  background: rgba(0,0,0,0.5); border-radius: 8rpx; padding: 2rpx 10rpx;
}
.status-tag-text { font-size: 18rpx; color: #fff; }

.card-body { display: flex; flex-direction: column; justify-content: space-between; }
.card-name { font-size: 28rpx; font-weight: 600; color: #1a1a2e; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-tags { display: flex; gap: 8rpx; margin-top: 8rpx; flex-wrap: wrap; }
.card-tag { font-size: 20rpx; color: #e94560; background: rgba(233,69,96,0.08); padding: 2rpx 12rpx; border-radius: 6rpx; }
.card-price-row { display: flex; align-items: baseline; margin-top: 8rpx; }
.card-price { font-size: 30rpx; font-weight: 700; color: #e94560; }
.card-unit { font-size: 20rpx; color: #999; margin-left: 4rpx; }
</style>
