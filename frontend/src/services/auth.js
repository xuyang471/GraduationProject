const API_BASE_URL = 'http://localhost:8081/api';
import store from '@/store'  // 添加这一行

// 测试连接
export const testConnection = async () => {
    try {
        console.log('测试后端连接:', `${API_BASE_URL}/auth/test`);

        const response = await fetch(`${API_BASE_URL}/auth/test`);
        if (!response.ok) {
            throw new Error(`HTTP错误: ${response.status}`);
        }

        const data = await response.json();
        console.log('后端连接测试结果:', data);

        return {
            success: true,
            data: data,
            message: '后端服务正常'
        };
    } catch (error) {
        console.error('连接后端失败:', error);
        return {
            success: false,
            message: `连接后端失败: ${error.message}`,
            error: error
        };
    }
};

// 登录方法
export const login = async (username, password) => {
    try {
        console.log('发送登录请求到:', `${API_BASE_URL}/auth/login`);
        console.log('登录数据:', { username, password: '[PROTECTED]' });

        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        console.log('登录响应状态:', response.status, response.statusText);

        const data = await response.json();
        console.log('登录响应数据:', data);

        if (!response.ok) {
            return {
                success: false,
                code: response.status,
                message: data.message || '登录失败',
                data: data
            };
        }

        // 保存token到localStorage
        if (data.success && data.data && data.data.token) {
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('user', JSON.stringify(data.data.user));

            // 提交用户信息到Vuex store
            store.dispatch('login', data.data.user);

            console.log('Token已保存:', data.data.token);
        }

        return {
            success: data.success || false,
            code: data.code || response.status,
            message: data.message || '登录成功',
            data: data.data || data
        };

    } catch (error) {
        console.error('登录请求异常:', error);
        return {
            success: false,
            code: 0,
            message: `网络错误: ${error.message}`,
            error: error
        };
    }
};


// 简单登录（表单格式，备用）
export const simpleLogin = async (username, password) => {
    try {
        const formData = new URLSearchParams();
        formData.append('username', username);
        formData.append('password', password);

        const response = await fetch(`${API_BASE_URL}/auth/simple-login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: formData
        });

        const data = await response.json();

        if (data.success && data.data && data.data.token) {
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('user', JSON.stringify(data.data.user));
        }

        return data;

    } catch (error) {
        console.error('简单登录失败:', error);
        return {
            success: false,
            message: `登录失败: ${error.message}`
        };
    }
};

// 检查认证状态
export const checkAuth = () => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    return {
        isAuthenticated: !!token,
        token: token,
        user: user ? JSON.parse(user) : null
    };
};

// 登出
export const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    // 从Vuex store中清除用户信息
    store.dispatch('logout');

    console.log('用户已登出');
    return { success: true, message: '登出成功' };
};


// 新增：获取当前用户信息
export const getCurrentUser = () => {
    // 优先从Vuex store获取用户信息
    if (store.state.currentUser) {
        return store.state.currentUser;
    }

    // 如果Vuex中没有，则从localStorage获取
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
};


// 更新默认导出对象，包含新增的 getCurrentUser 方法
const AuthService = {
    login,
    checkAuth,
    logout,
    getCurrentUser
};

export default AuthService;
