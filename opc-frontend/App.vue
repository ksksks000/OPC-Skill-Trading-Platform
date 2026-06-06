<!--
  OPC 技能交易平台 - 根组件

  管理全局生命周期和全局样式
  - onLaunch: 应用启动时初始化
  - onShow: 应用从后台切到前台
  - onHide: 应用从前台切到后台
-->
<template>
  <view>
    <router-view />
  </view>
</template>

<script>
export default {
  onLaunch() {
    console.log('OPC 技能交易平台启动')

    // 检查更新（小程序环境）
    // #ifdef MP-WEIXIN
    const updateManager = uni.getUpdateManager()
    updateManager.onUpdateReady(() => {
      uni.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: (res) => {
          if (res.confirm) updateManager.applyUpdate()
        }
      })
    })
    // #endif
  },

  onShow() {
    console.log('App Show')
  },

  onHide() {
    console.log('App Hide')
  }
}
</script>

<style>
/* ==================== 全局样式重置 ==================== */
page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', sans-serif;
  background: #f5f5f7;
  color: #1a1a2e;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 去除小程序默认按钮样式 */
button::after {
  border: none;
}

/* 全局点击态 */
.hover-active {
  opacity: 0.8;
  transform: scale(0.98);
  transition: all 0.15s ease;
}

/* 文本溢出省略 */
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 安全区域 */
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
