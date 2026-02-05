<template>
  <div class="login-container">
    <el-card class="login-box">
      <template #header>
        <div class="login-header">
          <h2>系统登录</h2>
        </div>
      </template>

      <el-form @submit.prevent="handleSubmit">
        <el-form-item>
          <el-input
              v-model="username"
              placeholder="请输入用户名"
              size="large"
              clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-alert
            v-if="errorMessage"
            :title="errorMessage"
            type="error"
            show-icon
            :closable="false"
            style="margin-bottom: 20px"
        />

        <el-alert
            v-if="successMessage"
            :title="successMessage"
            type="success"
            show-icon
            :closable="false"
            style="margin-bottom: 20px"
        />

        <el-form-item>
          <el-button
              type="primary"
              size="large"
              @click="handleSubmit"
              :loading="loading"
              style="width: 100%"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <el-alert
            title="测试账号"
            type="info"
            description="admin / admin123 | user / user123"
            show-icon
            :closable="false"
        />
      </el-form>
    </el-card>
  </div>
</template>

<script>
import AuthService from '../services/auth.js'
import { User, Lock } from '@element-plus/icons-vue'

export default {
  name: 'LoginView',
  components: {
    // eslint-disable-next-line vue/no-unused-components
    User,
    // eslint-disable-next-line vue/no-unused-components
    Lock
  },
  data() {
    return {
      username: '',
      password: '',
      loading: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    async handleSubmit() {
      this.loading = true
      this.errorMessage = ''
      this.successMessage = ''

      try {
        const response = await AuthService.login(this.username, this.password)
        this.successMessage = response.message

        // 登录成功后跳转到首页
        setTimeout(() => {
          this.$router.push('/home')
        }, 1000)

      } catch (error) {
        if (error.response && error.response.data) {
          this.errorMessage = error.response.data.message || '登录失败'
        } else {
          this.errorMessage = '网络错误，请稍后重试'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px;
}

:deep(.el-card__body) {
  padding: 30px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input) {
  width: 100%;
}

:deep(.el-button) {
  transition: all 0.3s;
}

:deep(.el-button:hover:not(.is-disabled)) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}
</style>