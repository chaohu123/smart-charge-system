<template>
  <div class="profile-page">
    <!-- 顶部导航栏 -->
    <header class="profile-header-nav">
      <div class="nav-inner">
        <div class="nav-left">
          <div class="logo-circle">⚡</div>
          <div class="nav-brand">
            <div class="brand-title">智能充电桩</div>
            <div class="brand-subtitle">让充电更简单，让出行更绿色</div>
          </div>
        </div>
        <nav class="nav-center">
          <div class="nav-menu-items">
            <div 
              class="nav-item" 
              :class="{ active: activeNav === '/home' }"
              @click="goHome"
            >
              首页
            </div>
            <div 
              class="nav-item" 
              :class="{ active: activeNav === '/map' }"
              @click="goMap"
            >
              地图
            </div>
            <div 
              class="nav-item" 
              :class="{ active: activeNav === '/orders' }"
              @click="goOrders"
            >
              订单
            </div>
            <div 
              class="nav-item" 
              :class="{ active: activeNav === '/notifications' }"
              @click="goMessages"
            >
              消息
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="nav-badge" />
            </div>
          </div>
        </nav>
        <div class="nav-right" v-if="userStore.token">
          <el-dropdown>
            <div class="user-entry">
              <el-avatar :src="userStore.userInfo?.avatar" :size="36" />
              <div class="user-text">
                <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.phone }}</span>
                <span class="user-credit">信用分 {{ userStore.userInfo?.creditScore || 100 }}</span>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.native="goProfileBasic">账户设置</el-dropdown-item>
                <el-dropdown-item divided @click.native="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="nav-right" v-else>
          <el-button type="primary" @click="showLoginDialog = true">登录</el-button>
          <el-button type="primary" link @click="router.push('/register')">注册</el-button>
        </div>
      </div>
    </header>

    <!-- 主体内容：左 8 / 右 4 栅格 -->
    <main class="profile-main">
      <div class="content-grid">
        <!-- 左侧主内容（8列） -->
        <section class="main-left">
          <!-- 用户状态与欢迎区域 -->
          <el-card class="card welcome-card" shadow="never">
            <div class="welcome-top">
              <div class="welcome-left">
                <div class="welcome-text">
                  <h2 class="welcome-title">
                    欢迎回来，<span class="welcome-name">{{ displayUserName }}</span>！
                  </h2>
                  <p class="welcome-sub">
                    <el-icon><Clock /></el-icon>
                    <span>上次充电：{{ lastChargeText }}</span>
                  </p>
                </div>
                <!-- 信用分可视化 -->
                <div class="credit-score-section">
                  <div class="credit-score-header">
                    <span class="credit-label">信用分</span>
                    <span class="credit-value">{{ creditScore }}</span>
                    <el-tag :type="creditTagType" size="small" effect="plain">{{ creditLevelText }}</el-tag>
                  </div>
                  <el-progress
                    :percentage="creditPercentage"
                    :color="creditProgressColor"
                    :stroke-width="8"
                    :show-text="false"
                    class="credit-progress"
                  />
                  <p class="credit-hint">{{ creditHintText }}</p>
                </div>
              </div>
              <div class="welcome-actions">
                <el-button type="primary" size="large" @click="goToFastCharge" class="primary-action-btn">
                  <el-icon><Lightning /></el-icon>
                  <span>前往充电</span>
                </el-button>
                <el-button size="large" @click="goOrders" plain class="secondary-action-btn">
                  <el-icon><Document /></el-icon>
                  <span>查看订单</span>
                </el-button>
              </div>
            </div>
            <!-- 车辆状态卡片 -->
            <div class="vehicle-status-card">
              <div class="vehicle-status-header">
                <div class="section-title">
                  <el-icon class="section-icon"><Van /></el-icon>
                  <span>您的车辆状态</span>
                  <span v-if="vehicles.length > 1" class="vehicle-count">({{ currentVehicleIndex + 1 }}/{{ vehicles.length }})</span>
                </div>
                <el-button text size="small" @click="showVehicleDialog = true">车辆管理</el-button>
              </div>
              <div 
                class="vehicle-status-content-wrapper"
                @touchstart="handleTouchStart"
                @touchmove="handleTouchMove"
                @touchend="handleTouchEnd"
                @mousedown="handleMouseDown"
                @mousemove="handleMouseMove"
                @mouseup="handleMouseUp"
                @mouseleave="handleMouseUp"
              >
                <div 
                  class="vehicle-status-content"
                  :style="{ transform: `translateX(-${currentVehicleIndex * 100}%)` }"
                >
                  <div 
                    v-for="(vehicle, index) in vehicles" 
                    :key="vehicle.id || index"
                    class="vehicle-status-item"
                  >
                    <!-- 电量环形进度条 -->
                    <div class="battery-progress-wrapper">
                      <el-progress
                        type="circle"
                        :percentage="getVehicleBatteryPercentage(vehicle)"
                        :width="120"
                        :stroke-width="10"
                        :color="getBatteryProgressColor(getVehicleBatteryPercentage(vehicle))"
                        class="battery-progress"
                      >
                        <template #default="{ percentage }">
                          <div class="battery-progress-inner">
                            <span class="battery-percentage">{{ percentage }}%</span>
                            <span class="battery-label">剩余电量</span>
                          </div>
                        </template>
                      </el-progress>
                      <div class="battery-info">
                        <div class="battery-range">
                          <span class="battery-range-value">{{ getVehicleRangeText(vehicle) }}</span>
                          <span class="battery-range-label">当前续航</span>
                        </div>
                        <div class="battery-vehicle-name">
                          {{ vehicle.brand }} {{ vehicle.model }}
                        </div>
                      </div>
                    </div>
                    <!-- 智能充电建议 -->
                    <div class="smart-charge-suggestion">
                      <div class="suggestion-header">
                        <el-icon class="suggestion-icon"><Lightning /></el-icon>
                        <span>智能充电建议</span>
                      </div>
                      <div class="suggestion-content">
                        <div class="suggestion-item">
                          <span class="suggestion-label">推荐充电时间</span>
                          <span class="suggestion-value highlight">{{ smartChargeTime }}</span>
                        </div>
                        <div class="suggestion-item">
                          <span class="suggestion-label">预计节省</span>
                          <span class="suggestion-value save-amount">¥{{ estimatedSaveAmount }}</span>
                        </div>
                        <div class="suggestion-item">
                          <span class="suggestion-label">附近最优站</span>
                          <span class="suggestion-value">{{ nearestOptimalStation }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 滑动指示器 -->
              <div v-if="vehicles.length > 1" class="vehicle-indicators">
                <div 
                  v-for="(vehicle, index) in vehicles" 
                  :key="vehicle.id || index"
                  class="indicator"
                  :class="{ active: currentVehicleIndex === index }"
                  @click="switchVehicle(index)"
                ></div>
              </div>
            </div>
          </el-card>

          <!-- 智能搜索区（增强版） -->
          <el-card class="card search-card" shadow="never">
            <div class="card-header-row">
              <h3>搜索充电站、地址或服务</h3>
            </div>
            <!-- 放大搜索框 -->
            <div class="search-bar-enhanced">
              <el-input
                v-model="searchKeyword"
                size="large"
                placeholder="例如：北京中关村软件园、附近充电站、快充桩"
                @keyup.enter="handleSearch"
                class="search-input-large"
              >
                <template #prefix>
                  <el-icon class="search-icon"><Search /></el-icon>
                </template>
                <template #append>
                  <el-button type="primary" size="large" @click="handleSearch" class="search-btn-large">
                    <el-icon><Search /></el-icon>
                    <span>搜索</span>
                  </el-button>
                </template>
              </el-input>
            </div>
            <!-- 筛选条件：常用与高级 -->
            <div class="search-filters-enhanced">
              <div class="filters-common">
                <span class="filters-label">常用筛选：</span>
                <el-checkbox v-model="filters.fast" class="filter-item">
                  <el-icon><Lightning /></el-icon>
                  <span>快充(直流)</span>
                </el-checkbox>
                <el-checkbox v-model="filters.slow" class="filter-item">
                  <el-icon><CircleCheck /></el-icon>
                  <span>慢充(交流)</span>
                </el-checkbox>
                <el-checkbox v-model="filters.onlyIdle" class="filter-item">
                  <el-icon><Select /></el-icon>
                  <span>仅显示空闲</span>
                </el-checkbox>
                <el-checkbox v-model="filters.freeParking" class="filter-item">
                  <el-icon><Location /></el-icon>
                  <span>免费停车</span>
                </el-checkbox>
              </div>
              <el-collapse v-model="advancedFiltersCollapse" class="filters-advanced-collapse">
                <el-collapse-item name="advanced" :title="null">
                  <template #title>
                    <el-button text size="small" class="advanced-toggle-btn">
                      <el-icon><ArrowDown v-if="!advancedFiltersCollapse.includes('advanced')" /><ArrowUp v-else /></el-icon>
                      <span>{{ advancedFiltersCollapse.includes('advanced') ? '收起' : '展开' }}高级筛选</span>
                    </el-button>
                  </template>
                  <div class="filters-advanced">
                    <el-checkbox v-model="filters.cheaper" class="filter-item">价格 &lt; 1.5元</el-checkbox>
                    <el-checkbox v-model="filters.lounge" class="filter-item">带休息室</el-checkbox>
                    <el-checkbox v-model="filters.fullTime" class="filter-item">24小时营业</el-checkbox>
                    <el-checkbox v-model="filters.reservable" class="filter-item">可预约</el-checkbox>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
            <!-- 最近搜索快捷入口 -->
            <div class="search-extra">
              <div class="recent-search">
                <span class="label">
                  <el-icon><Clock /></el-icon>
                  <span>最近搜索：</span>
                </span>
                <div class="recent-tags">
                  <el-tag
                    v-for="item in recentSearches"
                    :key="item"
                    size="small"
                    effect="plain"
                    class="recent-tag"
                    @click="quickSearch(item)"
                  >
                    {{ item }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 附近充电站分布模块 -->
          <el-card class="card map-preview-card" shadow="never">
            <div class="card-header-row">
              <h3>附近充电站分布</h3>
              <div class="map-actions">
                <el-button text size="small" @click="refreshLocation">
                  <el-icon><Refresh /></el-icon>
                  <span>刷新位置</span>
                </el-button>
                <el-button type="primary" size="small" @click="goMap" class="map-entry-btn">
                  <el-icon><MapLocation /></el-icon>
                  <span>进入完整地图</span>
                </el-button>
              </div>
            </div>
            <!-- 加载状态 -->
            <div v-if="loadingRecommend" class="nearby-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <!-- 最近充电站列表 -->
            <div v-else-if="nearbyStationsList.length > 0" class="nearby-stations-list">
              <div
                v-for="station in nearbyStationsList"
                :key="station.id"
                class="nearby-station-item"
                @click="goStationDetail(station)"
              >
                <div class="station-item-left">
                  <div class="station-name">{{ station.name }}</div>
                  <div class="station-info">
                    <span class="station-distance">
                      <el-icon><Location /></el-icon>
                      <span>{{ formatDistance(station.distance) }}</span>
                    </span>
                    <span class="station-price">
                      <el-icon><Money /></el-icon>
                      <span>¥{{ station.serviceFee || '--' }}/kWh</span>
                    </span>
                  </div>
                </div>
                <div class="station-item-right">
                  <div class="station-availability">
                    <span class="availability-label">空闲</span>
                    <span class="availability-value">{{ station.availablePiles || 0 }}/{{ station.totalPiles || 0 }}</span>
                  </div>
                  <el-button size="small" type="primary" plain @click.stop="goNavigateStation(station)">
                    导航
                  </el-button>
                </div>
              </div>
            </div>
            <!-- 空状态提示 -->
            <div v-else-if="!loadingRecommend" class="nearby-empty">
              <el-empty description="附近暂无充电站" :image-size="80">
                <el-button type="primary" size="small" @click="refreshLocation">刷新位置</el-button>
              </el-empty>
            </div>
            <!-- 地图预览（当没有列表数据时显示，已废弃，保留作为备用） -->
            <div v-if="false" class="map-preview-body">
              <div class="map-preview-placeholder">
                <span class="map-user">您在这里 📍</span>
                <div class="map-dots">
                  <span class="dot fast"></span>
                  <span class="dot fast"></span>
                  <span class="dot slow"></span>
                  <span class="dot slow"></span>
                  <span class="dot low"></span>
                </div>
              </div>
              <div class="map-legend">
                <span>图例：</span>
                <span>🔴 快充站 (空闲 &gt; 3)</span>
                <span>🔵 慢充站 (空闲 &gt; 3)</span>
                <span>⚫ 充电站较少或无空闲</span>
              </div>
            </div>
          </el-card>

          <!-- 智能推荐区 -->
          <el-card class="card recommend-card" shadow="never">
            <div class="card-header-row">
              <h3>智能推荐</h3>
              <el-button text size="small" @click="goToStationList">查看更多</el-button>
            </div>
            <div class="recommend-tabs">
              <el-tag
                v-for="tab in recommendTabs"
                :key="tab.value"
                :effect="tab.value === activeRecommendTab ? 'dark' : 'plain'"
                round
                size="small"
                @click="activeRecommendTab = tab.value"
              >
                {{ tab.label }}
              </el-tag>
            </div>

            <div v-if="loadingRecommend" class="recommend-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="!recommendStations.length" class="recommend-empty">
              <p class="empty-main">附近暂无可推荐的充电站</p>
              <p class="empty-sub">
                您可以调整位置、扩大搜索范围，或使用「沿途搜索」规划充电行程。
              </p>
              <div class="empty-actions">
                <el-button type="primary" plain @click="goMap">使用地图查找</el-button>
                <el-button @click="goRouteSearch">沿途搜索</el-button>
              </div>
            </div>
            <div v-else class="recommend-grid">
              <div
                v-for="station in sortedRecommendStations"
                :key="station.id"
                class="recommend-item"
                @click="goStationDetail(station)"
              >
                <div class="rec-header">
                  <div class="rec-title">{{ station.name }}</div>
                  <div class="rec-distance">{{ formatDistance(station.distance) }}</div>
                </div>
                <div class="rec-info-row">
                  <span>空闲：{{ station.availablePiles }}/{{ station.totalPiles }}</span>
                  <span>服务费：{{ station.serviceFee || '—' }} 元/度</span>
                </div>
                <div class="rec-info-row">
                  <span>营业时间：{{ station.businessHours || '00:00-24:00' }}</span>
                </div>
                <div class="rec-actions">
                  <el-button size="small" type="primary" plain @click.stop="goNavigateStation(station)">导航</el-button>
                  <el-button size="small" type="primary" @click.stop="goReserveStation(station)">详情</el-button>
                </div>
              </div>
            </div>

            <div class="recommend-tips">
              <p>⭐ 根据您的历史记录，您常在工作日18-20点充电</p>
              <p>⭐ 推荐您尝试附近新开的优质充电站，部分站点享受限时优惠</p>
              <p>⭐ 今晚22:00后为谷电时段，充电费用可节省约30%</p>
            </div>
          </el-card>
        </section>

        <!-- 右侧边栏（4列） -->
        <aside class="main-right">
          <!-- 快捷工具 -->
          <el-card class="card side-card" shadow="never">
            <div class="side-card-header">
              <span>快捷工具</span>
            </div>
            <!-- 高频功能 -->
            <div class="quick-tools">
              <div class="tool-section">
                <div class="section-title-small">高频功能</div>
                <div class="tool-item tool-item-high" @click="goToFastCharge">
                  <div class="tool-icon-wrapper high-frequency">
                    <el-icon class="tool-icon"><Lightning /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">一键找桩</span>
                    <span class="tool-desc">最近空闲充电站</span>
                  </div>
                </div>
                <div class="tool-item tool-item-high" @click="goRouteSearch">
                  <div class="tool-icon-wrapper high-frequency">
                    <el-icon class="tool-icon"><MapLocation /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">沿途搜索</span>
                    <span class="tool-desc">预约充电行程</span>
                  </div>
                </div>
                <div class="tool-item tool-item-high" @click="goWallet">
                  <div class="tool-icon-wrapper high-frequency">
                    <el-icon class="tool-icon"><Wallet /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">钱包充值</span>
                    <span class="tool-desc">快速充值余额</span>
                  </div>
                </div>
              </div>
              <!-- 低频功能 -->
              <div class="tool-section">
                <div class="section-title-small">其他功能</div>
                <div class="tool-item tool-item-low" @click="openCouponsDialog">
                  <div class="tool-icon-wrapper low-frequency">
                    <el-icon class="tool-icon"><Ticket /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">优惠券</span>
                    <span class="tool-desc">{{ couponCount }}张可用</span>
                  </div>
                </div>
                <div class="tool-item tool-item-low" @click="openInvoiceDialog">
                  <div class="tool-icon-wrapper low-frequency">
                    <el-icon class="tool-icon"><Document /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">开发票</span>
                    <span class="tool-desc">申请电子发票</span>
                  </div>
                </div>
                <div class="tool-item tool-item-low" @click="showVehicleDialog = true">
                  <div class="tool-icon-wrapper low-frequency">
                    <el-icon class="tool-icon"><Setting /></el-icon>
                  </div>
                  <div class="tool-content">
                    <span class="tool-name">车辆管理</span>
                    <span class="tool-desc">管理我的车辆</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 我的车辆信息 -->
          <el-card class="card side-card vehicle-card-enhanced" shadow="never">
            <div class="side-card-header">
              <span>我的车辆</span>
            </div>
            <div v-if="primaryVehicle" class="vehicle-brief-enhanced">
              <!-- 车辆资产视觉表达 -->
              <div class="vehicle-asset-header">
                <div class="vehicle-icon-large">
                  <el-icon><Van /></el-icon>
                </div>
                <div class="vehicle-info-main">
                  <p class="vehicle-name-enhanced">{{ primaryVehicle.brand }} {{ primaryVehicle.model }}</p>
                  <p class="vehicle-plate">{{ primaryVehicle.plateNumber || '未绑定车牌' }}</p>
                </div>
              </div>
              <!-- 月度统计 -->
              <div class="vehicle-stats">
                <div class="stat-item-month">
                  <span class="stat-label">本月充电</span>
                  <span class="stat-value">{{ monthlyChargeCount }}次</span>
                </div>
                <div class="stat-item-month">
                  <span class="stat-label">节省成本</span>
                  <span class="stat-value save-cost">¥{{ monthlySaveCost }}</span>
                </div>
              </div>
              <!-- 车辆详情 -->
              <div class="vehicle-details">
                <div class="detail-item">
                  <el-icon class="detail-icon"><Lightning /></el-icon>
                  <span class="detail-label">电池容量</span>
                  <span class="detail-value">{{ primaryVehicle.batteryCapacity || '--' }} kWh</span>
                </div>
                <div class="detail-item">
                  <el-icon class="detail-icon"><Clock /></el-icon>
                  <span class="detail-label">最近充电</span>
                  <span class="detail-value">{{ lastChargeShortText }}</span>
                </div>
              </div>
              <!-- 操作按钮 -->
              <div class="vehicle-actions-enhanced">
                <el-button size="small" @click="goChargeHistory" class="action-btn">
                  <el-icon><Document /></el-icon>
                  <span>充电记录</span>
                </el-button>
                <el-button size="small" type="primary" plain @click="showVehicleDialog = true" class="action-btn">
                  <el-icon><Setting /></el-icon>
                  <span>车辆设置</span>
                </el-button>
              </div>
            </div>
            <div v-else class="vehicle-empty-enhanced">
              <div class="empty-icon">
                <el-icon><Van /></el-icon>
              </div>
              <p class="empty-text">还没有绑定车辆</p>
              <el-button type="primary" size="small" @click="showVehicleDialog = true" class="empty-action-btn">
                <el-icon><Plus /></el-icon>
                <span>去添加车辆</span>
              </el-button>
            </div>
          </el-card>

          <!-- 近期活动/优惠 -->
          <el-card class="card side-card" shadow="never">
            <div class="side-card-header">
              <span>限时优惠</span>
            </div>
            <ul class="promo-list" @click="openCouponsDialog('available')" style="cursor: pointer;">
              <li>🎁 新用户首充立减20元</li>
              <li>🌙 谷电时段充电7折</li>
              <li>🎯 累计充电5次送1次</li>
              <li>📢 部分区域新增超级充电站</li>
            </ul>
            <div class="promo-actions">
              <el-button text size="small" type="primary" @click.stop="openCouponsDialog('available')">领取优惠券</el-button>
              <el-button text size="small" @click.stop="goActivities">查看全部活动</el-button>
            </div>
          </el-card>

          <!-- 收藏充电站摘要 -->
          <el-card class="card side-card" shadow="never">
            <div class="side-card-header">
              <span>收藏站点</span>
            </div>
            <div v-if="favoriteStations.length" class="favorite-list">
              <div
                v-for="fav in favoriteStations"
                :key="fav.id"
                class="favorite-item"
                @click="goStationDetailById(fav.stationId)"
              >
                <div class="fav-title">⭐ 充电站 #{{ fav.stationId }}</div>
              </div>
              <div class="favorite-actions">
                <el-button size="small" @click="goFavorites">管理收藏</el-button>
                <el-button size="small" type="primary" plain @click="goFavorites">查看全部</el-button>
              </div>
            </div>
            <div v-else class="favorite-empty">
              <p>您还没有收藏充电站。</p>
              <el-button size="small" type="primary" plain @click="goMap">去收藏常用站点</el-button>
            </div>
          </el-card>
        </aside>
      </div>

      <!-- 底部功能区 -->
      <section class="bottom-section">
        <div class="bottom-column">
          <h4>热门充电站推荐</h4>
          <ul>
            <li>· 国贸三期超级充电站</li>
            <li>· 首都机场T3充电站</li>
            <li>· 北京南站充电中心</li>
            <li>· 鸟巢奥运充电站</li>
          </ul>
        </div>
        <div class="bottom-column">
          <h4>附近服务设施</h4>
          <ul>
            <li>· 休息室 / 咖啡厅</li>
            <li>· 卫生间 / 便利店</li>
            <li>· 周边餐厅</li>
            <li>· 停车场</li>
          </ul>
        </div>
        <div class="bottom-column">
          <h4>使用指南</h4>
          <ul>
            <li>· 如何快速找到充电站</li>
            <li>· 预约充电流程</li>
            <li>· 常见问题解答</li>
            <li>· 客服联系方式</li>
          </ul>
        </div>
      </section>
    </main>

    <!-- 优惠券弹窗 -->
    <el-dialog
      v-model="showCouponsDialog"
      title="我的优惠券"
      width="880px"
      @close="selectedCoupon = null"
    >
      <div class="coupon-dialog-body">
        <div class="coupon-list-pane">
          <el-tabs v-model="couponTab">
            <el-tab-pane label="可领取" name="available">
              <div v-if="availableLoading" class="dialog-loading">
                <el-skeleton :rows="4" animated />
              </div>
              <template v-else>
                <el-empty v-if="!availableCoupons.length" description="暂无可领取优惠券" />
                <div v-else class="coupon-grid">
                  <el-card
                    v-for="coupon in availableCoupons"
                    :key="coupon.id"
                    class="coupon-card"
                    shadow="hover"
                    @click="handleSelectCoupon(coupon)"
                  >
                    <div class="coupon-card-main">
                      <div class="coupon-amount">
                        <span class="amount-symbol">¥</span>
                        <span class="amount-value">{{ coupon.amount || coupon.discount || 0 }}</span>
                      </div>
                      <div class="coupon-meta">
                        <div class="coupon-title">{{ coupon.name || '优惠券' }}</div>
                        <div class="coupon-limit">满 {{ coupon.minAmount || 0 }} 元可用</div>
                        <div class="coupon-expire">有效期至 {{ coupon.endTime || '--' }}</div>
                      </div>
                      <el-tag type="warning" size="small">可领取</el-tag>
                    </div>
                    <div class="coupon-card-actions">
                      <el-button
                        type="primary"
                        size="small"
                        :loading="receivingCouponId === coupon.id"
                        @click.stop="handleReceiveCoupon(coupon)"
                      >
                        立即领取
                      </el-button>
                    </div>
                  </el-card>
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane label="未使用" name="unused">
              <div v-if="couponsLoading" class="dialog-loading">
                <el-skeleton :rows="4" animated />
              </div>
              <template v-else>
                <el-empty v-if="!unusedCoupons.length" description="暂无未使用优惠券" />
                <div v-else class="coupon-grid">
                  <el-card
                    v-for="coupon in unusedCoupons"
                    :key="coupon.id"
                    class="coupon-card"
                    shadow="hover"
                    @click="handleSelectCoupon(coupon)"
                  >
                    <div class="coupon-card-main">
                      <div class="coupon-amount">
                        <span class="amount-symbol">¥</span>
                        <span class="amount-value">{{ coupon.amount || coupon.discount || 0 }}</span>
                      </div>
                      <div class="coupon-meta">
                        <div class="coupon-title">{{ coupon.name || '优惠券' }}</div>
                        <div class="coupon-limit">满 {{ coupon.minAmount || 0 }} 元可用</div>
                        <div class="coupon-expire">有效期至 {{ coupon.endTime || '--' }}</div>
                      </div>
                      <el-tag type="success" size="small">未使用</el-tag>
                    </div>
                  </el-card>
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane label="已使用" name="used">
              <div v-if="couponsLoading" class="dialog-loading">
                <el-skeleton :rows="4" animated />
              </div>
              <template v-else>
                <el-empty v-if="!usedCoupons.length" description="暂无已使用优惠券" />
                <div v-else class="coupon-grid">
                  <el-card
                    v-for="coupon in usedCoupons"
                    :key="coupon.id"
                    class="coupon-card used"
                    shadow="hover"
                    @click="handleSelectCoupon(coupon)"
                  >
                    <div class="coupon-card-main">
                      <div class="coupon-amount">
                        <span class="amount-symbol">¥</span>
                        <span class="amount-value">{{ coupon.amount || coupon.discount || 0 }}</span>
                      </div>
                      <div class="coupon-meta">
                        <div class="coupon-title">{{ coupon.name || '优惠券' }}</div>
                        <div class="coupon-limit">满 {{ coupon.minAmount || 0 }} 元可用</div>
                        <div class="coupon-expire">有效期至 {{ coupon.endTime || '--' }}</div>
                      </div>
                      <el-tag type="info" size="small">已使用</el-tag>
                    </div>
                  </el-card>
                </div>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="coupon-detail-pane">
          <div class="coupon-detail-title">
            <el-icon><Ticket /></el-icon>
            <span>优惠券详情</span>
          </div>
          <div v-if="selectedCoupon" class="coupon-detail-content">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="名称">{{ selectedCoupon.name || '优惠券' }}</el-descriptions-item>
              <el-descriptions-item label="面额">¥{{ selectedCoupon.amount || selectedCoupon.discount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="使用门槛">满 {{ selectedCoupon.minAmount || 0 }} 元可用</el-descriptions-item>
              <el-descriptions-item label="状态">{{ formatCouponStatus(selectedCoupon) }}</el-descriptions-item>
              <el-descriptions-item label="有效期">{{ selectedCoupon.startTime || '--' }} 至 {{ selectedCoupon.endTime || '--' }}</el-descriptions-item>
              <el-descriptions-item label="说明">{{ selectedCoupon.description || '暂无描述' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="点击左侧优惠券查看详情" :image-size="120" />
        </div>
      </div>
    </el-dialog>

    <!-- 发票列表弹窗 -->
    <el-dialog
      v-model="showInvoiceDialog"
      title="我的发票"
      width="900px"
    >
      <div v-if="invoiceLoading" class="dialog-loading">
        <el-skeleton :rows="5" animated />
      </div>
      <template v-else>
        <el-empty v-if="!invoices.length" description="暂无发票记录" />
        <el-table v-else :data="invoices" stripe style="width: 100%">
          <el-table-column prop="invoiceNo" label="发票号" min-width="140" />
          <el-table-column prop="orderNo" label="关联订单" min-width="140" />
          <el-table-column prop="title" label="发票抬头" min-width="120" />
          <el-table-column prop="amount" label="金额" min-width="100" />
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getInvoiceStatusType(scope.row.status)" size="small">
                {{ scope.row.status || '待开票' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" min-width="160" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === '已开票'"
                size="small"
                type="primary"
                @click="handleDownloadInvoice(scope.row)"
              >
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <!-- 登录弹窗 -->
    <el-dialog
      v-model="showLoginDialog"
      title="登录"
      width="380px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
        <el-form-item prop="phone">
          <el-input
            v-model="loginForm.phone"
            placeholder="请输入手机号"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleDialogLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loginLoading"
            @click="handleDialogLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="login-dialog-footer">
          <span>还没有账号？</span>
          <el-button type="primary" link @click="goRegisterFromDialog">立即注册</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 车辆管理对话框 -->
    <el-dialog v-model="showVehicleDialog" title="我的车辆">
      <el-button type="primary" @click="showAddVehicleDialog = true">添加车辆</el-button>
      <el-table :data="vehicles" style="width: 100%; margin-top: 20px">
        <el-table-column prop="plateNumber" label="车牌号" />
        <el-table-column prop="brand" label="品牌" />
        <el-table-column prop="model" label="型号" />
        <el-table-column prop="batteryCapacity" label="电池容量(kWh)" />
      </el-table>
    </el-dialog>

    <!-- 添加车辆对话框 -->
    <el-dialog v-model="showAddVehicleDialog" title="添加车辆">
      <el-form :model="vehicleForm" label-width="100px">
        <el-form-item label="车牌号">
          <el-input v-model="vehicleForm.plateNumber" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="vehicleForm.brand" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="vehicleForm.model" />
        </el-form-item>
        <el-form-item label="电池容量">
          <el-input-number v-model="vehicleForm.batteryCapacity" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddVehicleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddVehicle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getVehicleList, addVehicle, uploadAvatar } from '@/api/user'
import { getUnreadCount } from '@/api/notification'
import { getRecommendedStations, searchStations, getNearbyStations } from '@/api/station'
import { getFavoriteList } from '@/api/favorite'
import { getMyCoupons, getAvailableCoupons, receiveCoupon } from '@/api/coupon'
import { getInvoiceList, downloadInvoice } from '@/api/invoice'
import { login } from '@/api/user'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  Document,
  Van,
  UserFilled,
  Lock,
  Star,
  Ticket,
  Bell,
  SwitchButton,
  Search,
  Clock,
  Lightning,
  CircleCheck,
  Select,
  Location,
  ArrowDown,
  ArrowUp,
  Refresh,
  MapLocation,
  Money,
  Setting,
  Plus
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const showVehicleDialog = ref(false)
const showAddVehicleDialog = ref(false)
const showRealNameDialog = ref(false)
const vehicles = ref([])
const unreadCount = ref(0)
const currentVehicleIndex = ref(0)

// 触摸滑动相关
const touchStartX = ref(0)
const touchStartY = ref(0)
const touchEndX = ref(0)
const touchEndY = ref(0)
const isDragging = ref(false)
const dragStartX = ref(0)

const recommendStations = ref([])
const sortedRecommendStations = computed(() => {
  const list = [...recommendStations.value]
  switch (activeRecommendTab.value) {
    case 'distance':
      return list.sort((a, b) => (a.distance || Infinity) - (b.distance || Infinity))
    case 'price':
      return list.sort((a, b) => {
        const pa = a.serviceFee != null ? Number(a.serviceFee) : Infinity
        const pb = b.serviceFee != null ? Number(b.serviceFee) : Infinity
        return pa - pb
      })
    case 'idle':
      return list.sort((a, b) => {
        const ra = a.totalPiles ? a.availablePiles / a.totalPiles : 0
        const rb = b.totalPiles ? b.availablePiles / b.totalPiles : 0
        return rb - ra
      })
    case 'score':
      return list.sort((a, b) => {
        const sa = typeof a.recommendScore === 'number' ? a.recommendScore : 0
        const sb = typeof b.recommendScore === 'number' ? b.recommendScore : 0
        return sb - sa
      })
    default:
      return list
  }
})
const loadingRecommend = ref(false)
const favoriteStations = ref([])
const couponCount = ref(0)
const showCouponsDialog = ref(false)
const showInvoiceDialog = ref(false)
const couponTab = ref('unused')
const couponsLoading = ref(false)
const availableLoading = ref(false)
const unusedCoupons = ref([])
const usedCoupons = ref([])
const availableCoupons = ref([])
const selectedCoupon = ref(null)
const couponsLoaded = ref(false)
const receivingCouponId = ref(null)
const invoiceLoading = ref(false)
const invoices = ref([])

const showLoginDialog = ref(false)
const loginLoading = ref(false)
const loginFormRef = ref(null)
const loginForm = ref({
  phone: '',
  password: ''
})
const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const searchKeyword = ref('')
const filters = ref({
  fast: false,
  slow: false,
  onlyIdle: false,
  freeParking: false,
  cheaper: false,
  lounge: false,
  fullTime: false,
  reservable: false
})

const recentSearches = ref([])

const recommendTabs = [
  { label: '综合推荐', value: 'default' },
  { label: '距离最近', value: 'distance' },
  { label: '价格最优', value: 'price' },
  { label: '评分最高', value: 'score' },
  { label: '空闲最多', value: 'idle' }
]
const activeRecommendTab = ref('default')

// 高级筛选折叠状态
const advancedFiltersCollapse = ref([])

// 信用分相关计算
const creditScore = computed(() => {
  return userStore.userInfo?.creditScore || 100
})

const creditPercentage = computed(() => {
  return Math.min(100, (creditScore.value / 100) * 100)
})

const creditLevelText = computed(() => {
  if (creditScore.value >= 90) return '优秀'
  if (creditScore.value >= 70) return '良好'
  if (creditScore.value >= 50) return '一般'
  return '待提升'
})

const creditTagType = computed(() => {
  if (creditScore.value >= 90) return 'success'
  if (creditScore.value >= 70) return 'warning'
  return 'info'
})

const creditProgressColor = computed(() => {
  if (creditScore.value >= 90) return '#00B578'
  if (creditScore.value >= 70) return '#1677FF'
  return '#FF9800'
})

const creditHintText = computed(() => {
  if (creditScore.value >= 90) return '信用优秀，享受更多权益'
  if (creditScore.value >= 70) return '保持良好信用，继续加油'
  return '完善信息可提升信用分'
})

// 电池电量相关计算
const batteryPercentage = computed(() => {
  // 这里应该从车辆数据或订单数据中获取，暂时使用示例值
  return 65
})

const batteryProgressColor = computed(() => {
  const percentage = batteryPercentage.value
  if (percentage >= 50) return '#00B578'
  if (percentage >= 20) return '#1677FF'
  return '#FF4D4F'
})

// 智能充电建议
const smartChargeTime = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 22 || hour < 6) {
    return '当前时段（谷电优惠）'
  }
  return '今晚22:00后（谷电优惠）'
})

const estimatedSaveAmount = computed(() => {
  // 根据谷电优惠计算，示例值
  return '15.8'
})

const nearestOptimalStation = computed(() => {
  if (recommendStations.value.length > 0) {
    return recommendStations.value[0]?.name || '暂无推荐'
  }
  return '暂无推荐'
})

// 附近充电站列表
const nearbyStationsList = computed(() => {
  // 取前3个最近的充电站，确保有数据且是数组
  if (!recommendStations.value || !Array.isArray(recommendStations.value)) {
    return []
  }
  return recommendStations.value.slice(0, 3)
})

// 月度统计（示例数据，实际应从API获取）
const monthlyChargeCount = computed(() => {
  return 12 // 示例值
})

const monthlySaveCost = computed(() => {
  return '186.5' // 示例值
})

const vehicleForm = ref({
  plateNumber: '',
  brand: '',
  model: '',
  batteryCapacity: 0
})

// 监听筛选条件变化，自动搜索（在组件顶层定义）
watch(
  () => [filters.value.fast, filters.value.slow, filters.value.onlyIdle, 
         filters.value.freeParking, filters.value.cheaper, filters.value.lounge,
         filters.value.fullTime, filters.value.reservable],
  () => {
    // 防抖处理，避免频繁请求
    if (searchDebounceTimer.value) {
      clearTimeout(searchDebounceTimer.value)
    }
    searchDebounceTimer.value = setTimeout(() => {
      if (searchKeyword.value || hasActiveFilters.value) {
        performSearch()
      }
    }, 500)
  },
  { deep: true }
)

onMounted(async () => {
  await loadVehicles()
  await loadUnreadCount()
  await loadRecommend()
  await loadFavoriteStations()
  await loadCouponCount()
  loadSearchHistory()
})

// 搜索防抖定时器
const searchDebounceTimer = ref(null)

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const loadVehicles = async () => {
  try {
    const res = await getVehicleList()
    if (res.code === 200) {
      vehicles.value = res.data || []
      // 重置当前车辆索引，确保不超出范围
      if (currentVehicleIndex.value >= vehicles.value.length) {
        currentVehicleIndex.value = 0
      }
    }
  } catch (error) {
    console.error('加载车辆列表失败:', error)
  }
}

const handleAddVehicle = async () => {
  try {
    const res = await addVehicle(vehicleForm.value)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      showAddVehicleDialog.value = false
      await loadVehicles()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const uploadUrl = computed(() => {
  return '/api/user/avatar'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${userStore.token}`
  }
})

const displayUserName = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.phone || '智能车主'
})

const primaryVehicle = computed(() => {
  return vehicles.value && vehicles.value.length > 0 ? vehicles.value[currentVehicleIndex.value] : null
})

// 切换车辆
const switchVehicle = (index) => {
  if (index >= 0 && index < vehicles.value.length) {
    currentVehicleIndex.value = index
  }
}

// 获取车辆电量百分比
const getVehicleBatteryPercentage = (vehicle) => {
  // 这里应该从车辆数据或订单数据中获取，暂时使用示例值
  // 可以根据车辆ID从订单中获取最新的电量信息
  return vehicle.batteryPercentage || 65
}

// 根据电量百分比返回进度条颜色
const getBatteryProgressColor = (percentage) => {
  if (percentage >= 50) return '#00B578'
  if (percentage >= 20) return '#1677FF'
  return '#FF4D4F'
}

// 获取车辆续航文本
const getVehicleRangeText = (vehicle) => {
  const percentage = getVehicleBatteryPercentage(vehicle)
  const batteryCapacity = vehicle.batteryCapacity || 60
  // 假设每kWh可以行驶约4.5km
  const estimatedRange = Math.round((batteryCapacity * percentage / 100) * 4.5)
  return `约 ${estimatedRange}km (剩余电量 ${percentage}%)`
}

// 触摸事件处理
const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
  isDragging.value = true
}

const handleTouchMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()
}

const handleTouchEnd = (e) => {
  if (!isDragging.value) return
  touchEndX.value = e.changedTouches[0].clientX
  touchEndY.value = e.changedTouches[0].clientY
  handleSwipe()
  isDragging.value = false
}

// 鼠标事件处理（桌面端）
const handleMouseDown = (e) => {
  dragStartX.value = e.clientX
  isDragging.value = true
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  e.preventDefault()
}

const handleMouseUp = (e) => {
  if (!isDragging.value) return
  const dragEndX = e.clientX || dragStartX.value
  touchStartX.value = dragStartX.value
  touchEndX.value = dragEndX
  handleSwipe()
  isDragging.value = false
}

// 处理滑动
const handleSwipe = () => {
  if (vehicles.value.length <= 1) return
  
  const deltaX = touchEndX.value - touchStartX.value
  const deltaY = Math.abs(touchEndY.value - touchStartY.value)
  
  // 判断是否为水平滑动（水平滑动距离大于垂直滑动距离）
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 50) {
    if (deltaX > 0 && currentVehicleIndex.value > 0) {
      // 向右滑动，显示上一辆车
      currentVehicleIndex.value--
    } else if (deltaX < 0 && currentVehicleIndex.value < vehicles.value.length - 1) {
      // 向左滑动，显示下一辆车
      currentVehicleIndex.value++
    }
  }
}

const vehicleRangeText = computed(() => {
  // 这里可以根据车辆电池容量、最近订单等数据计算，暂用示例文案
  return '约 280km (剩余电量 65%)'
})

const lastChargeText = computed(() => {
  // 可结合订单数据优化，这里为示例
  return '2天前 国贸CBD充电站'
})

const lastChargeShortText = computed(() => {
  return '2天前'
})

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    ElMessage.success('头像上传成功')
    await userStore.getUserInfoAction()
  } else {
    ElMessage.error('头像上传失败')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 顶部导航跳转
const activeNav = computed(() => {
  const path = router.currentRoute.value.path
  if (path === '/home' || path === '/') return '/home'
  if (path.startsWith('/map')) return '/map'
  if (path.startsWith('/orders') || path.startsWith('/order/')) return '/orders'
  if (path.startsWith('/notifications')) return '/notifications'
  return path
})

const goHome = () => router.push('/home')
const goMap = () => router.push('/map')
const goOrders = () => router.push('/orders')
const goMessages = () => router.push('/notifications')
const goProfileBasic = () => router.push('/profile')

// 搜索相关
const handleSearch = async () => {
  if (!searchKeyword.value && !hasActiveFilters.value) {
    ElMessage.warning('请输入搜索内容或选择筛选条件')
    return
  }
  
  // 保存搜索记录
  if (searchKeyword.value) {
    saveSearchHistory(searchKeyword.value)
  }
  
  // 执行搜索
  await performSearch()
}

// 执行搜索
const performSearch = async () => {
  loadingRecommend.value = true
  try {
    let res
    
    // 获取用户位置
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(async (position) => {
        const { longitude, latitude } = position.coords
        currentLocation.value = { longitude, latitude }
        
        // 构建搜索参数
        const params = {
          longitude,
          latitude,
          radius: 10000,
          current: 1,
          size: 20
        }
        
        // 处理筛选条件
        if (filters.value.fast && !filters.value.slow) {
          params.type = 1 // 快充
        } else if (filters.value.slow && !filters.value.fast) {
          params.type = 0 // 慢充
        }
        
        if (filters.value.onlyIdle) {
          params.status = 0 // 仅空闲
        }
        
        // 如果有搜索关键词，使用搜索接口
        if (searchKeyword.value) {
          const searchParams = {
            keyword: searchKeyword.value,
            type: params.type,
            current: 1,
            size: 20
          }
          
          // 价格筛选
          if (filters.value.cheaper) {
            searchParams.minPrice = 0
            searchParams.maxPrice = 1.5
          }
          
          // 其他筛选条件
          if (filters.value.freeParking) {
            searchParams.freeParking = 1
          }
          if (filters.value.lounge) {
            searchParams.hasLounge = 1
          }
          if (filters.value.fullTime) {
            searchParams.is24Hours = 1
          }
          if (filters.value.reservable) {
            searchParams.reservable = 1
          }
          
          res = await searchStations(searchParams)
        } else {
          // 使用附近充电站接口
          res = await getNearbyStations(params)
        }
        
        if (res.code === 200) {
          // 处理不同的数据结构
          let stations = []
          if (Array.isArray(res.data)) {
            stations = res.data
          } else if (res.data && Array.isArray(res.data.records)) {
            stations = res.data.records
          } else if (res.data && res.data.data && Array.isArray(res.data.data)) {
            stations = res.data.data
          }
          
          // 前端筛选处理（后端不支持的条件）
          stations = applyFrontendFilters(stations)
          
          // 计算距离（如果接口没有返回）
          stations.forEach(station => {
            if (!station.distance && currentLocation.value) {
              station.distance = calculateDistance(
                currentLocation.value.latitude,
                currentLocation.value.longitude,
                station.latitude,
                station.longitude
              )
            }
          })
          
          // 排序
          stations.sort((a, b) => (a.distance || Infinity) - (b.distance || Infinity))
          
          recommendStations.value = stations
          updateNearbyStationsForMap(stations)
        }
        loadingRecommend.value = false
      }, (error) => {
        console.error('获取位置失败:', error)
        ElMessage.warning('无法获取位置信息，请检查位置权限')
        loadingRecommend.value = false
      })
    } else {
      // 不支持地理位置，使用搜索接口
      const searchParams = {
        keyword: searchKeyword.value || undefined,
        current: 1,
        size: 20
      }
      
      if (filters.value.fast && !filters.value.slow) {
        searchParams.type = 1
      } else if (filters.value.slow && !filters.value.fast) {
        searchParams.type = 0
      }
      
      if (filters.value.cheaper) {
        searchParams.minPrice = 0
        searchParams.maxPrice = 1.5
      }
      
      // 其他筛选条件
      if (filters.value.freeParking) {
        searchParams.freeParking = 1
      }
      if (filters.value.lounge) {
        searchParams.hasLounge = 1
      }
      if (filters.value.fullTime) {
        searchParams.is24Hours = 1
      }
      if (filters.value.reservable) {
        searchParams.reservable = 1
      }
      
      res = await searchStations(searchParams)
      if (res.code === 200) {
        // 处理不同的数据结构
        let stations = []
        if (Array.isArray(res.data)) {
          stations = res.data
        } else if (res.data && Array.isArray(res.data.records)) {
          stations = res.data.records
        } else if (res.data && res.data.data && Array.isArray(res.data.data)) {
          stations = res.data.data
        }
        
        stations = applyFrontendFilters(stations)
        recommendStations.value = stations
        updateNearbyStationsForMap(stations)
      }
      loadingRecommend.value = false
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
    loadingRecommend.value = false
  }
}

// 前端筛选处理（后端不支持的条件）
const applyFrontendFilters = (stations) => {
  let filtered = [...stations]
  
  // 这些筛选条件现在后端已支持，但为了兼容性，前端也做一次过滤
  if (filters.value.freeParking) {
    filtered = filtered.filter(s => s.freeParking === 1 || s.freeParking === true)
  }
  
  if (filters.value.lounge) {
    filtered = filtered.filter(s => s.hasLounge === 1 || s.hasLounge === true)
  }
  
  if (filters.value.fullTime) {
    filtered = filtered.filter(s => s.is24Hours === 1 || s.is24Hours === true)
  }
  
  if (filters.value.reservable) {
    filtered = filtered.filter(s => s.reservable === 1 || s.reservable === true)
  }
  
  return filtered
}

// 计算距离（Haversine公式）
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371000 // 地球半径（米）
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return Math.round(R * c)
}

// 更新地图预览的充电站数据
const updateNearbyStationsForMap = (stations) => {
  // 这里可以更新地图预览区域的数据
  // 目前地图预览是静态的，后续可以集成真实地图组件
}

// 检查是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return filters.value.fast || filters.value.slow || 
         filters.value.onlyIdle || filters.value.freeParking ||
         filters.value.cheaper || filters.value.lounge ||
         filters.value.fullTime || filters.value.reservable
})

// 保存搜索历史
const saveSearchHistory = (keyword) => {
  if (!keyword || keyword.trim() === '') return
  
  const history = getSearchHistory()
  // 移除重复项
  const filtered = history.filter(item => item !== keyword)
  // 添加到开头
  filtered.unshift(keyword)
  // 最多保存10条
  const limited = filtered.slice(0, 10)
  // 保存到localStorage
  localStorage.setItem('searchHistory', JSON.stringify(limited))
  // 更新显示
  recentSearches.value = limited
}

// 获取搜索历史
const getSearchHistory = () => {
  try {
    const history = localStorage.getItem('searchHistory')
    return history ? JSON.parse(history) : []
  } catch (error) {
    console.error('读取搜索历史失败:', error)
    return []
  }
}

// 加载搜索历史
const loadSearchHistory = () => {
  recentSearches.value = getSearchHistory()
}

const quickSearch = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 当前用户位置
const currentLocation = ref(null)

// 推荐列表加载
const loadRecommend = async () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持地理位置功能')
    return
  }
  loadingRecommend.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const { longitude, latitude } = position.coords
        currentLocation.value = { longitude, latitude }
        
        // 优先使用推荐接口
        try {
          const res = await getRecommendedStations({
            longitude,
            latitude,
            radius: 8000
          })
          if (res.code === 200) {
            // 处理返回的数据结构
            let stations = []
            if (Array.isArray(res.data)) {
              stations = res.data
            } else if (res.data && Array.isArray(res.data.records)) {
              stations = res.data.records
            } else if (res.data && res.data.data && Array.isArray(res.data.data)) {
              stations = res.data.data
            }
            
            if (stations.length > 0) {
              recommendStations.value = stations
              loadingRecommend.value = false
              return
            }
          }
        } catch (e) {
          console.warn('推荐接口失败，使用降级方案:', e)
        }
        
        // 降级方案：使用附近充电站接口
        const nearbyRes = await getNearbyStations({
          longitude,
          latitude,
          radius: 8000,
          current: 1,
          size: 20
        })
        
        if (nearbyRes.code === 200) {
          let stations = []
          if (Array.isArray(nearbyRes.data)) {
            stations = nearbyRes.data
          } else if (nearbyRes.data && Array.isArray(nearbyRes.data.records)) {
            stations = nearbyRes.data.records
          }
          
          // 计算距离（如果接口没有返回）
          stations.forEach(station => {
            if (!station.distance) {
              station.distance = calculateDistance(
                latitude,
                longitude,
                station.latitude,
                station.longitude
              )
            }
          })
          
          // 按距离排序
          stations.sort((a, b) => (a.distance || Infinity) - (b.distance || Infinity))
          
          recommendStations.value = stations
        }
      } catch (e) {
        console.error('加载推荐充电站失败', e)
        ElMessage.error('加载充电站数据失败，请稍后重试')
      } finally {
        loadingRecommend.value = false
      }
    },
    (error) => {
      console.error('获取位置失败:', error)
      ElMessage.warning('无法获取您的位置，请检查位置权限设置')
      loadingRecommend.value = false
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 60000
    }
  )
}

const formatDistance = (distance) => {
  if (!distance && distance !== 0) return '--'
  if (distance >= 1000) {
    return (distance / 1000).toFixed(1) + 'km'
  }
  return distance + 'm'
}

const goToStationList = () => {
  router.push('/search')
}

const goStationDetail = (station) => {
  router.push(`/station/${station.id}`)
}

const goStationDetailById = (id) => {
  router.push(`/station/${id}`)
}

const goNavigateStation = (station) => {
  router.push({
    path: '/map',
    query: {
      stationId: station.id
    }
  })
}

const goReserveStation = (station) => {
  router.push({
    path: '/station/' + station.id
  })
}

const refreshLocation = () => {
  ElMessage.info('正在刷新位置...')
  currentLocation.value = null
  loadRecommend()
  // 如果有关键词或筛选条件，重新搜索
  if (searchKeyword.value || hasActiveFilters.value) {
    performSearch()
  }
}

// 收藏站点摘要加载（从收藏列表 + 站点详情推导）
const loadFavoriteStations = async () => {
  try {
    const res = await getFavoriteList()
    if (res.code === 200 && Array.isArray(res.data)) {
      // 这里只展示收藏记录本身，详细信息在收藏页中按需加载
      favoriteStations.value = res.data || []
    }
  } catch (e) {
    console.error('加载收藏站点失败', e)
  }
}

// 快捷工具跳转
const goToFastCharge = () => {
  router.push('/map')
}

const goWallet = () => router.push('/wallet')
const openCouponsDialog = async (tab = 'unused') => {
  couponTab.value = tab
  showCouponsDialog.value = true
  console.log('[coupon] open dialog, tab=', tab)
  await loadCouponList()
}
const openInvoiceDialog = async () => {
  showInvoiceDialog.value = true
  await loadInvoicesForDialog()
}
const goActivities = () => router.push('/notifications')
const goFavorites = () => router.push('/favorites')
const goChargeHistory = () => router.push('/orders')
const goRouteSearch = () => router.push('/route-search')

// 优惠券数量：从我的未使用优惠券中统计
const loadCouponCount = async () => {
  try {
    const res = await getMyCoupons(0)
    if (res.code === 200 && Array.isArray(res.data)) {
      couponCount.value = res.data.length
    }
  } catch (e) {
    console.error('加载优惠券数量失败', e)
  }
}

// 优惠券弹窗数据加载
const loadCouponList = async (force = false) => {
  if (couponsLoaded.value && !force) return
  couponsLoading.value = true
  availableLoading.value = true
  try {
    console.log('[coupon] loading coupons...')
    const [unusedRes, usedRes, availableRes] = await Promise.all([
      getMyCoupons(0),
      getMyCoupons(1),
      getAvailableCoupons()
    ])
    console.log('[coupon] api result unusedRes=', unusedRes, 'usedRes=', usedRes, 'availableRes=', availableRes)
    if (unusedRes.code === 200 && Array.isArray(unusedRes.data)) {
      unusedCoupons.value = unusedRes.data
    }
    if (usedRes.code === 200 && Array.isArray(usedRes.data)) {
      usedCoupons.value = usedRes.data
    }
    if (availableRes.code === 200 && Array.isArray(availableRes.data)) {
      availableCoupons.value = availableRes.data
    }
    console.log('[coupon] state available=', availableCoupons.value, 'unused=', unusedCoupons.value, 'used=', usedCoupons.value)
    couponsLoaded.value = true
  } catch (error) {
    ElMessage.error('加载优惠券失败')
    console.error('[coupon] loadCouponList error', error)
  } finally {
    couponsLoading.value = false
    availableLoading.value = false
  }
}

const handleSelectCoupon = (coupon) => {
  selectedCoupon.value = coupon
}

const formatCouponStatus = (coupon) => {
  if (couponTab.value === 'available') {
    return '可领取'
  }
  if (coupon.status === 1 || coupon.used === true || couponTab.value === 'used') {
    return '已使用'
  }
  if (coupon.status === 0 || coupon.used === false || couponTab.value === 'unused') {
    return '未使用'
  }
  return coupon.status || '未知'
}

watch(showCouponsDialog, (val) => {
  if (!val) {
    selectedCoupon.value = null
  }
})

const handleReceiveCoupon = async (coupon) => {
  if (!coupon?.id) return
  receivingCouponId.value = coupon.id
  try {
    const res = await receiveCoupon(coupon.id)
    if (res.code === 200) {
      ElMessage.success('领取成功，已加入我的优惠券')
      couponsLoaded.value = false
      couponTab.value = 'unused'
      await Promise.all([loadCouponList(true), loadCouponCount()])
    } else {
      ElMessage.error(res.message || '领取失败')
    }
  } catch (error) {
    ElMessage.error('领取失败')
  } finally {
    receivingCouponId.value = null
  }
}

// 发票弹窗数据
const loadInvoicesForDialog = async () => {
  invoiceLoading.value = true
  try {
    const res = await getInvoiceList({ current: 1, size: 20 })
    if (res.code === 200) {
      invoices.value = Array.isArray(res.data?.records) ? res.data.records : (res.data || [])
    }
  } catch (error) {
    ElMessage.error('加载发票列表失败')
  } finally {
    invoiceLoading.value = false
  }
}

const getInvoiceStatusType = (status) => {
  const types = {
    '待开票': 'warning',
    '已开票': 'success',
    '已作废': 'info'
  }
  return types[status] || 'info'
}

const handleDownloadInvoice = async (invoice) => {
  try {
    const res = await downloadInvoice(invoice.id)
    if (res.code === 200) {
      window.open(res.data, '_blank')
    }
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 登录弹窗逻辑
const handleDialogLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loginLoading.value = true
    try {
      const res = await login(loginForm.value.phone, loginForm.value.password)
      if (res.code === 200) {
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data.user)
        ElMessage.success('登录成功')
        showLoginDialog.value = false
        // 登录后刷新与用户相关的数据
        await Promise.all([loadVehicles(), loadFavoriteStations(), loadCouponCount(), loadUnreadCount()])
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    } catch (e) {
      ElMessage.error('登录失败')
    } finally {
      loginLoading.value = false
    }
  })
}

const goRegisterFromDialog = () => {
  showLoginDialog.value = false
  router.push('/register')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f6fa;
  padding-bottom: 24px;
}

.profile-header-nav {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  padding: 12px 24px;
  color: white;
  position: sticky;
  top: 0;
  z-index: 10;
}

.nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
  gap: 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.logo-circle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.nav-brand {
  display: flex;
  flex-direction: column;
}

.brand-title {
  font-size: 18px;
  font-weight: 600;
}

.brand-subtitle {
  font-size: 12px;
  opacity: 0.9;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
  overflow: visible;
}

.nav-menu-items {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  width: 100%;
}

.nav-item {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  white-space: nowrap;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  position: relative;
}

.nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.active {
  color: #fff;
  border-bottom-color: #fff;
  background: transparent;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-size: 14px;
}

.user-credit {
  font-size: 12px;
  opacity: 0.8;
}

.nav-badge :deep(.el-badge__content.is-fixed) {
  top: 12px;
}

.profile-main {
  max-width: 1200px;
  margin: 16px auto 0;
  padding: 0 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
}

.main-left {
  grid-column: span 8;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-right {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  border-radius: 16px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 欢迎卡片优化 */
.welcome-card .welcome-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 24px;
}

.welcome-left {
  flex: 1;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #212121;
}

.welcome-name {
  color: #1677FF;
}

.welcome-sub {
  font-size: 14px;
  color: #636e72;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 信用分可视化 */
.credit-score-section {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
  border-radius: 12px;
}

.credit-score-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.credit-label {
  font-size: 14px;
  color: #636e72;
}

.credit-value {
  font-size: 20px;
  font-weight: 600;
  color: #1677FF;
}

.credit-progress {
  margin: 8px 0;
}

.credit-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin: 4px 0 0;
}

/* 操作按钮 */
.welcome-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 140px;
}

.primary-action-btn {
  background: linear-gradient(135deg, #1677FF 0%, #0958d9 100%);
  border: none;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.primary-action-btn:hover {
  background: linear-gradient(135deg, #0958d9 0%, #1677FF 100%);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.4);
  transform: translateY(-2px);
}

.secondary-action-btn {
  height: 48px;
  font-size: 16px;
  border-color: #d9d9d9;
}

.secondary-action-btn:hover {
  border-color: #1677FF;
  color: #1677FF;
}

/* 车辆状态卡片 */
.vehicle-status-card {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;
}

.vehicle-status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #212121;
}

.vehicle-count {
  font-size: 14px;
  font-weight: 400;
  color: #8c8c8c;
  margin-left: 4px;
}

.section-icon {
  font-size: 20px;
  color: #1677FF;
}

.vehicle-status-content-wrapper {
  overflow: hidden;
  position: relative;
  width: 100%;
  cursor: grab;
  user-select: none;
}

.vehicle-status-content-wrapper:active {
  cursor: grabbing;
}

.vehicle-status-content {
  display: flex;
  transition: transform 0.3s ease;
  width: 100%;
}

.vehicle-status-item {
  min-width: 100%;
  flex-shrink: 0;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 电量环形进度条 */
.battery-progress-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.battery-progress {
  position: relative;
}

.battery-progress-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.battery-percentage {
  font-size: 24px;
  font-weight: 700;
  color: #212121;
  line-height: 1;
}

.battery-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.battery-info {
  text-align: center;
}

.battery-vehicle-name {
  margin-top: 4px;
  font-size: 14px;
  color: #636e72;
}

.battery-range-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #212121;
  margin-bottom: 4px;
}

.battery-range-label {
  font-size: 12px;
  color: #8c8c8c;
}

/* 智能充电建议 */
.smart-charge-suggestion {
  flex: 1;
  padding: 16px;
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-radius: 12px;
  border: 1px solid #ffe7ba;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #212121;
  margin-bottom: 12px;
}

.suggestion-icon {
  color: #1677FF;
  font-size: 18px;
}

.suggestion-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-label {
  font-size: 13px;
  color: #636e72;
}

.suggestion-value {
  font-size: 14px;
  font-weight: 500;
  color: #212121;
}

.suggestion-value.highlight {
  color: #1677FF;
  font-weight: 600;
}

.suggestion-value.save-amount {
  color: #00B578;
  font-weight: 600;
  font-size: 16px;
}

/* 车辆滑动指示器 */
.vehicle-indicators {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d9d9d9;
  cursor: pointer;
  transition: all 0.3s;
}

.indicator:hover {
  background: #1677FF;
  transform: scale(1.2);
}

.indicator.active {
  background: #1677FF;
  width: 24px;
  border-radius: 4px;
}

.search-card {
  margin-top: 4px;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-header-row h3 {
  margin: 0;
  font-size: 16px;
}

/* 搜索框放大 */
.search-bar-enhanced {
  margin-bottom: 16px;
}

.search-input-large :deep(.el-input__wrapper) {
  padding: 12px 16px;
  font-size: 16px;
  border-radius: 12px;
}

.search-icon {
  font-size: 20px;
  color: #8c8c8c;
}

.search-btn-large {
  height: 100%;
  padding: 0 24px;
  font-size: 16px;
  font-weight: 500;
}

/* 筛选条件优化 */
.search-filters-enhanced {
  margin-bottom: 12px;
}

.filters-common {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
  margin-bottom: 12px;
}

.filters-label {
  font-size: 14px;
  font-weight: 500;
  color: #212121;
  margin-right: 4px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.filter-item :deep(.el-checkbox__label) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.filters-advanced-collapse {
  border: none;
  background: transparent;
}

.filters-advanced-collapse :deep(.el-collapse-item__header) {
  border: none;
  padding: 0;
  background: transparent;
}

.filters-advanced-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  background: transparent;
}

.filters-advanced-collapse :deep(.el-collapse-item__content) {
  padding: 12px 0 0;
}

.advanced-toggle-btn {
  color: #1677FF;
  font-size: 13px;
}

.filters-advanced {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 20px;
}

.search-extra {
  font-size: 13px;
  color: #636e72;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.recent-search {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.recent-search .label {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8c8c8c;
  font-size: 13px;
}

.recent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.recent-tag {
  cursor: pointer;
  transition: all 0.2s ease;
}

.recent-tag:hover {
  background: #1677FF;
  color: #fff;
  border-color: #1677FF;
}

.map-preview-card .map-preview-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.map-preview-placeholder {
  height: 180px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e8f0ff, #e6fff7);
  position: relative;
  overflow: hidden;
  padding: 12px;
}

.map-user {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 8px;
  border-radius: 999px;
}

.map-dots {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.map-dots .dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.map-dots .dot.fast {
  background: #ff6b6b;
}

.map-dots .dot.slow {
  background: #1a6dff;
}

.map-dots .dot.low {
  background: #636e72;
}

.map-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  font-size: 12px;
  color: #636e72;
}

.map-actions {
  display: flex;
  gap: 8px;
}

.map-entry-btn {
  background: linear-gradient(135deg, #1677FF 0%, #0958d9 100%);
  border: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.map-entry-btn:hover {
  background: linear-gradient(135deg, #0958d9 0%, #1677FF 100%);
}

/* 附近充电站列表 */
.nearby-stations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.nearby-station-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nearby-station-item:hover {
  background: #f0f5ff;
  border-color: #1677FF;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
}

.station-item-left {
  flex: 1;
}

.station-name {
  font-size: 15px;
  font-weight: 600;
  color: #212121;
  margin-bottom: 8px;
}

.station-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
}

.station-distance,
.station-price {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #8c8c8c;
}

.station-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.station-availability {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.availability-label {
  font-size: 12px;
  color: #8c8c8c;
}

.availability-value {
  font-size: 16px;
  font-weight: 600;
  color: #00B578;
}

/* 附近充电站加载状态 */
.nearby-loading {
  padding: 20px 0;
}

/* 附近充电站空状态 */
.nearby-empty {
  padding: 40px 20px;
  text-align: center;
}

.recommend-card .recommend-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.recommend-loading {
  padding: 8px 0;
}

.recommend-empty {
  text-align: left;
  padding: 8px 0 4px;
}

.empty-main {
  font-size: 14px;
  margin: 0 0 4px;
}

.empty-sub {
  font-size: 12px;
  color: #636e72;
  margin: 0 0 8px;
}

.empty-actions {
  display: flex;
  gap: 8px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 4px;
}

.recommend-item {
  border-radius: 10px;
  padding: 10px;
  background: #ffffff;
  border: 1px solid #ecf0f1;
  cursor: pointer;
  transition: all 0.2s ease;
}

.recommend-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.rec-title {
  font-size: 14px;
  font-weight: 600;
}

.rec-distance {
  font-size: 12px;
  color: #636e72;
}

.rec-info-row {
  font-size: 12px;
  color: #636e72;
  display: flex;
  justify-content: space-between;
  margin-bottom: 2px;
}

.rec-actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 6px;
}

.recommend-tips {
  margin-top: 10px;
  font-size: 12px;
  color: #636e72;
}

.side-card-header {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

/* 快捷工具优化 */
.quick-tools {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tool-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title-small {
  font-size: 13px;
  font-weight: 500;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.tool-item:hover {
  background: #f8f9ff;
  border-color: #e6f0ff;
  transform: translateX(4px);
}

.tool-item-high:hover {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  border-color: #1677FF;
}

.tool-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.tool-icon-wrapper.high-frequency {
  background: linear-gradient(135deg, #1677FF 0%, #0958d9 100%);
  color: #fff;
}

.tool-icon-wrapper.low-frequency {
  background: #f5f5f5;
  color: #8c8c8c;
}

.tool-item:hover .tool-icon-wrapper.high-frequency {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
}

.tool-item:hover .tool-icon-wrapper.low-frequency {
  background: #e6f0ff;
  color: #1677FF;
}

.tool-icon {
  font-size: 20px;
}

.tool-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.tool-name {
  font-size: 14px;
  font-weight: 500;
  color: #212121;
}

.tool-desc {
  font-size: 12px;
  color: #8c8c8c;
}

/* 我的车辆模块优化 */
.vehicle-card-enhanced {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
}

.vehicle-brief-enhanced {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.vehicle-asset-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.vehicle-icon-large {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1677FF 0%, #0958d9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}

.vehicle-info-main {
  flex: 1;
}

.vehicle-name-enhanced {
  font-size: 18px;
  font-weight: 600;
  color: #212121;
  margin: 0 0 4px;
}

.vehicle-plate {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

/* 月度统计 */
.vehicle-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  border-radius: 12px;
}

.stat-item-month {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #212121;
}

.stat-value.save-cost {
  color: #00B578;
}

/* 车辆详情 */
.vehicle-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.detail-icon {
  color: #1677FF;
  font-size: 16px;
}

.detail-label {
  color: #8c8c8c;
  flex: 1;
}

.detail-value {
  color: #212121;
  font-weight: 500;
}

/* 操作按钮 */
.vehicle-actions-enhanced {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* 空状态 */
.vehicle-empty-enhanced {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  font-size: 32px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 16px;
}

.empty-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.promo-list {
  list-style: none;
  padding: 0;
  margin: 0 0 6px;
  font-size: 13px;
  color: #2d3436;
}

.promo-list li + li {
  margin-top: 4px;
}

.promo-actions {
  display: flex;
  gap: 8px;
}

.favorite-list {
  font-size: 13px;
}

.favorite-item {
  padding: 6px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
}

.favorite-item:last-child {
  border-bottom: none;
}

.fav-title {
  font-weight: 500;
  margin-bottom: 2px;
}

.fav-meta {
  font-size: 12px;
  color: #636e72;
  display: flex;
  justify-content: space-between;
}

.favorite-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.favorite-empty {
  font-size: 13px;
  color: #636e72;
}

.bottom-section {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
  padding: 12px 0 0;
  border-top: 1px solid #ecf0f1;
  font-size: 13px;
}

.bottom-column h4 {
  margin: 0 0 6px;
  font-size: 14px;
}

.bottom-column ul {
  padding-left: 16px;
  margin: 0;
}

.bottom-column li + li {
  margin-top: 4px;
}

/* 优惠券 & 发票弹窗样式 */
.coupon-dialog-body {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.coupon-list-pane {
  min-height: 380px;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
  margin-top: 8px;
}

.coupon-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.coupon-card.used {
  opacity: 0.85;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.06);
}

.coupon-card-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.coupon-card-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: #f56c6c;
}

.amount-symbol {
  font-size: 16px;
}

.amount-value {
  font-size: 32px;
  font-weight: 700;
}

.coupon-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.coupon-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.coupon-detail-pane {
  border-left: 1px solid #f0f0f0;
  padding-left: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-detail-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #303133;
}

.coupon-detail-content {
  flex: 1;
}

.dialog-loading {
  padding: 16px 8px;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .main-left,
  .main-right {
    grid-column: span 12;
  }

  .recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .nav-inner {
    max-width: 100%;
    padding: 0 16px;
  }
  
  .brand-subtitle {
    display: none;
  }
  
  .nav-menu :deep(.el-menu-item) {
    padding: 0 12px;
    font-size: 14px;
  }
}

@media (max-width: 768px) {
  .nav-inner {
    flex-wrap: wrap;
    padding: 8px 16px;
  }
  
  .nav-left {
    order: 1;
  }
  
  .nav-right {
    order: 2;
  }
  
  .nav-center {
    display: flex;
    order: 3;
    width: 100%;
    margin-top: 8px;
  }
  
  .nav-menu-items {
    display: flex;
    justify-content: space-around;
    width: 100%;
  }
  
  .nav-item {
    padding: 0 8px;
    font-size: 13px;
    height: 48px;
    line-height: 48px;
    flex: 1;
    text-align: center;
  }
  
  .brand-subtitle {
    display: none;
  }

  .vehicle-status-body {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .recommend-grid {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }

  .bottom-section {
    grid-template-columns: repeat(1, minmax(0, 1fr));
  }
}
</style>

