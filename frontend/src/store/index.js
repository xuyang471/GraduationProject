import { createStore } from 'vuex'

export default createStore({
  state: {
    currentUser: null,
    isAuthenticated: false
  },
  mutations: {
    SET_CURRENT_USER(state, user) {
      state.currentUser = user
      state.isAuthenticated = !!user
    },
    CLEAR_CURRENT_USER(state) {
      state.currentUser = null
      state.isAuthenticated = false
    }
  },
  actions: {
    login({ commit }, user) {
      commit('SET_CURRENT_USER', user)
    },
    logout({ commit }) {
      commit('CLEAR_CURRENT_USER')
    }
  },
  getters: {
    currentUser: state => state.currentUser,
    isAuthenticated: state => state.isAuthenticated
  }
})

