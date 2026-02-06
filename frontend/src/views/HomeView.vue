<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <i class="el-icon-office-building"></i>
          <span class="logo-text">校园失物招领系统</span>
        </div>
        <div class="user-info" v-if="currentUser">
          <el-avatar :size="36" :src="currentUser.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'">
            {{ displayName.charAt(0) }}
          </el-avatar>
          <div class="user-details">
            <div class="user-name">{{ displayName }}</div>
            <div class="user-role">
              <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
            </div>
          </div>
        </div>
        <div class="user-info" v-else>
          <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png">
            U
          </el-avatar>
          <div class="user-details">
            <div class="user-name">加载中...</div>
            <div class="user-role">
              <el-tag type="info" size="small">未知角色</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="header-right">
        <el-button
            v-if="requirePasswordChange && currentUser"
            type="warning"
            size="small"
            icon="el-icon-lock"
            @click="showChangePassword = true"
        >
          修改初始密码
        </el-button>

        <el-badge :value="unreadCount" :max="99" class="item">
          <el-button
              icon="el-icon-bell"
              circle
              size="small"
              @click="handleNotifications"
          ></el-button>
        </el-badge>

        <el-dropdown @command="handleCommand">
          <el-button type="text" class="more-btn">
            更多操作<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <i class="el-icon-user"></i>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <i class="el-icon-setting"></i>系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <i class="el-icon-switch-button"></i>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主体内容 -->
    <el-container class="main-container">
      <!-- 左侧导航 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
            :default-active="activeMenu"
            class="nav-menu"
            @select="handleMenuSelect"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
        >
          <!-- 首页概览 -->
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span>首页概览</span>
          </el-menu-item>

          <!-- 失物招领功能 -->
          <el-submenu
              index="lost-found"
              v-if="currentUser && (currentUser.role === 1 || currentUser.role === 2)"
          >
            <template #title>
              <i class="el-icon-search"></i>
              <span>失物招领</span>
            </template>
            <el-menu-item index="/lost-items">
              <i class="el-icon-tickets"></i>查找失物
            </el-menu-item>
            <el-menu-item index="/found-items">
              <i class="el-icon-circle-plus"></i>发布招领
            </el-menu-item>
            <el-menu-item index="/report-lost">
              <i class="el-icon-warning-outline"></i>挂失物品
            </el-menu-item>
            <el-menu-item index="/my-claims">
              <i class="el-icon-document"></i>我的认领
            </el-menu-item>
          </el-submenu>

          <!-- 管理员功能 -->
          <el-submenu index="admin" v-if="currentUser && currentUser.role === 3">
            <template #title>
              <i class="el-icon-s-operation"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/users">
              <i class="el-icon-user"></i>用户管理
            </el-menu-item>
            <el-menu-item index="/admin/items">
              <i class="el-icon-s-goods"></i>失物管理
            </el-menu-item>
            <el-menu-item index="/admin/lockers">
              <i class="el-icon-box"></i>储物柜管理
            </el-menu-item>
            <el-menu-item index="/admin/claims">
              <i class="el-icon-check"></i>认领审核
            </el-menu-item>
            <el-menu-item index="/admin/complaints">
              <i class="el-icon-chat-line-round"></i>申诉处理
            </el-menu-item>
            <el-menu-item index="/admin/statistics">
              <i class="el-icon-data-analysis"></i>统计分析
            </el-menu-item>
          </el-submenu>

          <!-- 系统设置 -->
          <el-menu-item index="/settings">
            <i class="el-icon-setting"></i>
            <span>个人设置</span>
          </el-menu-item>

          <!-- 帮助中心 -->
          <el-menu-item index="/help">
            <i class="el-icon-question"></i>
            <span>使用帮助</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-main class="content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>

        <!-- 用户信息已加载 -->
        <template v-else>
          <!-- 统计卡片 -->
          <el-row :gutter="20" class="stats-row">
            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon" style="background: #409EFF;">
                    <i class="el-icon-s-goods"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-title">待认领失物</div>
                    <div class="stat-value">{{ stats.pendingItems || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon" style="background: #67C23A;">
                    <i class="el-icon-check"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-title">已认领失物</div>
                    <div class="stat-value">{{ stats.claimedItems || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon" style="background: #E6A23C;">
                    <i class="el-icon-box"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-title">可用储物柜</div>
                    <div class="stat-value">{{ stats.availableLockers || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6" :lg="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon" style="background: #F56C6C;">
                    <i class="el-icon-timer"></i>
                  </div>
                  <div class="stat-content">
                    <div class="stat-title">待审核申请</div>
                    <div class="stat-value">{{ stats.pendingClaims || 0 }}</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 快速操作 -->
          <el-row :gutter="20" class="quick-action-row">
            <el-col :span="24">
              <el-card class="quick-action-card">
                <template #header>
                  <div class="card-header">
                    <span>快速操作</span>
                    <el-button type="text" @click="refreshQuickActions">刷新</el-button>
                  </div>
                </template>

                <div class="quick-actions">
                  <el-button
                      type="primary"
                      icon="el-icon-search"
                      @click="quickAction('search')"
                  >
                    搜索失物
                  </el-button>

                  <el-button
                      v-if="currentUser && (currentUser.role === 2 || currentUser.role === 3)"
                      type="success"
                      icon="el-icon-circle-plus"
                      @click="quickAction('report')"
                  >
                    发布招领
                  </el-button>

                  <el-button
                      v-if="currentUser && currentUser.role === 3"
                      type="warning"
                      icon="el-icon-s-operation"
                      @click="quickAction('manage')"
                  >
                    管理后台
                  </el-button>

                  <el-button
                      icon="el-icon-help"
                      @click="quickAction('help')"
                  >
                    使用帮助
                  </el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 最新失物和通知 -->
          <el-row :gutter="20" class="info-row">
            <!-- 最新失物 -->
            <el-col :xs="24" :sm="12" :lg="12">
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>最新失物</span>
                    <router-link to="/lost-items" class="more-link">
                      查看更多 <i class="el-icon-arrow-right"></i>
                    </router-link>
                  </div>
                </template>
                <el-table
                    :data="recentItems"
                    style="width: 100%"
                    height="250"
                    @row-click="handleItemClick"
                    v-loading="loadingRecentItems"
                >
                  <el-table-column prop="title" label="物品名称" width="150"></el-table-column>
                  <el-table-column prop="category" label="分类" width="100"></el-table-column>
                  <el-table-column prop="foundTime" label="拾得时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.foundTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="location" label="拾得地点"></el-table-column>
                  <el-table-column label="状态" width="80">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)" size="small">
                        {{ getStatusText(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>

            <!-- 系统通知 -->
            <el-col :xs="24" :sm="12" :lg="12">
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>系统通知</span>
                    <el-button type="text" @click="markAllAsRead" :disabled="unreadCount === 0">
                      全部标记已读
                    </el-button>
                  </div>
                </template>
                <el-timeline>
                  <el-timeline-item
                      v-for="(notification, index) in notifications"
                      :key="index"
                      :timestamp="formatDate(notification.time)"
                      placement="top"
                  >
                    <el-card shadow="hover" :class="{ unread: !notification.read }">
                      <div @click="handleNotificationClick(notification)">
                        <h4>{{ notification.title }}</h4>
                        <p>{{ notification.content }}</p>
                        <div v-if="notification.type" class="notification-type">
                          <el-tag size="mini" :type="getNotificationType(notification.type)">
                            {{ notification.type }}
                          </el-tag>
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </el-card>
            </el-col>
          </el-row>

          <!-- 热点区域 -->
          <el-row v-if="currentUser && currentUser.role === 3" :gutter="20" class="hotspot-row">
            <el-col :span="24">
              <el-card class="hotspot-card">
                <template #header>
                  <div class="card-header">
                    <span>失物热点区域分析</span>
                    <el-button type="text" @click="showHotspotAnalysis">查看详情</el-button>
                  </div>
                </template>
                <div class="hotspot-content">
                  <el-table :data="hotspots" style="width: 100%">
                    <el-table-column prop="location" label="热点区域"></el-table-column>
                    <el-table-column prop="period" label="高发时段"></el-table-column>
                    <el-table-column prop="count" label="失物数量" width="100">
                      <template #default="scope">
                        <el-tag :type="getHeatLevelType(scope.row.level)">
                          {{ scope.row.count }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="危险等级" width="100">
                      <template #default="scope">
                        <el-rate
                            v-model="scope.row.level"
                            disabled
                            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                            :max="3"
                        ></el-rate>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>
      </el-main>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog
        title="修改初始密码"
        v-model="showChangePassword"
        width="400px"
        @close="resetPasswordForm"
    >
      <el-form
          :model="passwordForm"
          :rules="passwordRules"
          ref="passwordFormRef"
          label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              disabled
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showChangePassword = false">取消</el-button>
          <el-button
              type="primary"
              @click="handleChangePassword"
              :loading="changingPassword"
              :disabled="!currentUser"
          >
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import AuthService from '@/services/auth'
import api from '@/api'

export default {
  name: 'HomeView',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能少于6位'))
      } else if (value === this.passwordForm.oldPassword) {
        callback(new Error('新密码不能与原密码相同'))
      } else {
        callback()
      }
    }

    const validateConfirm = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请确认密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      loading: true,
      activeMenu: '/home',
      requirePasswordChange: false,
      unreadCount: 0,
      showChangePassword: false,
      changingPassword: false,
      loadingRecentItems: false,

      // 统计数据
      stats: {
        pendingItems: 0,
        claimedItems: 0,
        availableLockers: 0,
        pendingClaims: 0
      },

      // 最新失物
      recentItems: [],

      // 通知
      notifications: [],

      // 热点数据
      hotspots: [],

      // 密码表单
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },

      // 密码验证规则
      passwordRules: {
        newPassword: [
          { required: true, validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    ...mapState(['currentUser']),

    // 安全访问用户信息
    displayName() {
      if (!this.currentUser) return '游客'
      return this.currentUser.realName || this.currentUser.username || '用户'
    },

    roleText() {
      if (!this.currentUser || !this.currentUser.role) return '未知角色'
      const roleMap = {
        1: '学生',
        2: '教职工',
        3: '管理员'
      }
      return roleMap[this.currentUser.role] || '未知角色'
    },

    roleTagType() {
      if (!this.currentUser || !this.currentUser.role) return 'info'
      const typeMap = {
        1: 'info',
        2: 'warning',
        3: 'danger'
      }
      return typeMap[this.currentUser.role] || 'info'
    }
  },

  created() {
    this.initUser()
  },

  methods: {
    ...mapActions(['logout', 'fetchCurrentUser']),

    // 初始化用户信息
    async initUser() {
      this.loading = true
      try {
        // 尝试从 Vuex 获取用户信息
        if (!this.currentUser) {
          await this.fetchCurrentUser()
        }

        // 如果还是没有用户信息，重定向到登录页
        if (!this.currentUser) {
          this.$message.warning('请先登录')
          this.$router.push('/login')
          return
        }

        // 初始化数据
        await this.initData()
        this.checkFirstLogin()
      } catch (error) {
        console.error('初始化用户失败:', error)
        this.$message.error('加载失败，请重试')
        this.$router.push('/login')
      } finally {
        this.loading = false
      }
    },

    // 初始化数据
    async initData() {
      await Promise.all([
        this.loadStatistics(),
        this.loadRecentItems(),
        this.loadNotifications(),
        this.loadHotspots()
      ])
    },

    async loadStatistics() {
      try {
        const response = await api.getHomeStatistics()
        if (response.success) {
          this.stats = response.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.$message.error('加载统计数据失败')
      }
    },

    async loadRecentItems() {
      this.loadingRecentItems = true
      try {
        const response = await api.getRecentLostItems()
        if (response.success) {
          this.recentItems = response.data || []
        }
      } catch (error) {
        console.error('加载最新失物失败:', error)
        this.recentItems = []
      } finally {
        this.loadingRecentItems = false
      }
    },

    async loadNotifications() {
      try {
        const response = await api.getNotifications()
        if (response.success) {
          this.notifications = response.data || []
          this.unreadCount = this.notifications.filter(n => !n.read).length
        }
      } catch (error) {
        console.error('加载通知失败:', error)
        this.notifications = []
      }
    },

    async loadHotspots() {
      try {
        const response = await api.getHotspots()
        if (response.success) {
          this.hotspots = response.data || []
        }
      } catch (error) {
        console.error('加载热点数据失败:', error)
        this.hotspots = []
      }
    },

    checkFirstLogin() {
      if (!this.currentUser) return

      const requireChange = localStorage.getItem('requirePasswordChange')
      this.requirePasswordChange = requireChange === 'true'

      // 如果是首次登录（使用默认密码），显示修改密码对话框
      if (this.requirePasswordChange) {
        this.showChangePassword = true
        // 设置默认密码为原密码
        const username = this.currentUser.username || ''
        this.passwordForm.oldPassword = username.length >= 6
            ? username.substring(username.length() - 6)
            : username
      }
    },

    handleCommand(command) {
      if (!this.currentUser) {
        this.$message.warning('请先登录')
        return
      }

      switch (command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'settings':
          this.$router.push('/settings')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },

    handleMenuSelect(index) {
      this.$router.push(index)
    },

    quickAction(action) {
      if (!this.currentUser) {
        this.$message.warning('请先登录')
        return
      }

      switch (action) {
        case 'search':
          this.$router.push('/lost-items')
          break
        case 'report':
          if (this.currentUser.role === 2 || this.currentUser.role === 3) {
            this.$router.push('/found-items')
          } else {
            this.$message.warning('您没有发布招领的权限')
          }
          break
        case 'manage':
          if (this.currentUser.role === 3) {
            this.$router.push('/admin')
          } else {
            this.$message.warning('您没有管理员权限')
          }
          break
        case 'help':
          this.$router.push('/help')
          break
      }
    },

    handleItemClick(row) {
      this.$router.push(`/item-detail/${row.id}`)
    },

    handleNotificationClick(notification) {
      notification.read = true
      this.unreadCount = this.notifications.filter(n => !n.read).length
      // 处理通知点击逻辑
    },

    markAllAsRead() {
      this.notifications.forEach(n => n.read = true)
      this.unreadCount = 0
      // 调用API标记全部已读
    },

    handleNotifications() {
      this.$router.push('/notifications')
    },

    showHotspotAnalysis() {
      this.$router.push('/admin/statistics')
    },

    async handleChangePassword() {
      if (!this.currentUser || !this.currentUser.id) {
        this.$message.error('用户信息无效')
        return
      }

      this.$refs.passwordFormRef.validate(async (valid) => {
        if (valid) {
          this.changingPassword = true
          try {
            const response = await AuthService.changePassword({
              userId: this.currentUser.id,
              oldPassword: this.passwordForm.oldPassword,
              newPassword: this.passwordForm.newPassword
            })

            if (response.success) {
              this.$message.success('密码修改成功')
              this.showChangePassword = false
              localStorage.removeItem('requirePasswordChange')
              this.requirePasswordChange = false
            } else {
              this.$message.error(response.message)
            }
          } catch (error) {
            this.$message.error('修改密码失败: ' + error.message)
          } finally {
            this.changingPassword = false
          }
        }
      })
    },

    resetPasswordForm() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      if (this.$refs.passwordFormRef) {
        this.$refs.passwordFormRef.resetFields()
      }
    },

    refreshQuickActions() {
      this.initData()
      this.$message.success('数据已刷新')
    },

    formatDate(date) {
      if (!date) return ''
      try {
        const d = new Date(date)
        return d.toLocaleString('zh-CN')
      } catch (error) {
        return ''
      }
    },

    getStatusType(status) {
      const typeMap = {
        1: 'info',     // 待认领
        2: 'success',  // 已认领
        3: 'warning'   // 已处理
      }
      return typeMap[status] || 'info'
    },

    getStatusText(status) {
      const textMap = {
        1: '待认领',
        2: '已认领',
        3: '已处理'
      }
      return textMap[status] || '未知'
    },

    getNotificationType(type) {
      const typeMap = {
        '系统': 'primary',
        '认领': 'success',
        '申诉': 'warning',
        '紧急': 'danger'
      }
      return typeMap[type] || 'info'
    },

    getHeatLevelType(level) {
      const typeMap = {
        1: 'info',
        2: 'warning',
        3: 'danger'
      }
      return typeMap[level] || 'info'
    },

    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.logout()
        this.$router.push('/login')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.home-container {
  height: 100vh;
  background: #f0f2f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6ebf5;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.logo i {
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-role {
  margin-top: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.more-btn {
  padding: 0 10px;
  font-size: 14px;
}

.main-container {
  height: calc(100vh - 60px);
}

.sidebar {
  background: #304156;
  height: 100%;
}

.nav-menu {
  border-right: none;
  height: 100%;
}

.content {
  padding: 20px;
  overflow-y: auto;
}

.loading-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

/* 统计卡片样式 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

/* 快速操作 */
.quick-action-row {
  margin-bottom: 20px;
}

.quick-action-card .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

/* 信息卡片 */
.info-row {
  margin-bottom: 20px;
}

.info-card .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.more-link {
  font-size: 12px;
  color: #409EFF;
  text-decoration: none;
}

.more-link:hover {
  text-decoration: underline;
}

/* 通知卡片 */
.unread {
  border-left: 3px solid #409EFF;
}

.notification-type {
  margin-top: 8px;
}

/* 热点卡片 */
.hotspot-row {
  margin-bottom: 20px;
}

.hotspot-card .el-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    padding: 10px;
  }

  .header-left, .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .sidebar {
    width: 60px !important;
  }

  .nav-menu span {
    display: none;
  }

  .stat-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  .quick-actions {
    flex-direction: column;
  }
}
</style>