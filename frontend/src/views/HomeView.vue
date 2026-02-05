<template>
  <div class="home-container">
    <div class="header">
      <h1>欢迎, {{ currentUser.username }}!</h1>
      <button @click="handleLogout" class="logout-btn">退出登录</button>
    </div>

    <div class="content">
      <div class="card">
        <h3>登录信息</h3>
        <p><strong>用户名:</strong> {{ currentUser.username }}</p>
        <p><strong>登录时间:</strong> {{ loginTime }}</p>
        <p><strong>Token:</strong> {{ truncatedToken }}</p>
      </div>

      <div class="card">
        <h3>系统功能</h3>
        <ul>
          <li>用户管理</li>
          <li>数据统计</li>
          <li>系统设置</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '../services/auth.js'

export default {
  name: 'HomeView',
  data() {
    return {
      currentUser: {},
      loginTime: new Date().toLocaleString()
    }
  },
  computed: {
    truncatedToken() {
      const token = localStorage.getItem('token')
      return token ? token.substring(0, 20) + '...' : '无'
    }
  },
  created() {
    const user = AuthService.getCurrentUser()
    if (user) {
      this.currentUser = user
    } else {
      this.$router.push('/')
    }
  },
  methods: {
    handleLogout() {
      AuthService.logout()
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f7fafc;
}

.header {
  background: white;
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  color: #333;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  background: #e53e3e;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #c53030;
}

.content {
  padding: 40px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.card h3 {
  color: #2d3748;
  margin-top: 0;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

ul {
  list-style: none;
  padding: 0;
}

li {
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}

li:last-child {
  border-bottom: none;
}

p {
  color: #4a5568;
  line-height: 1.6;
}

strong {
  color: #2d3748;
}
</style>