import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    user: JSON.parse(localStorage.getItem('user') || '{}'),
    token: localStorage.getItem('token') || '',
    unreadCount: 0
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    CLEAR_USER(state) {
      state.user = {}
      state.token = ''
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },
    SET_UNREAD_COUNT(state, count) {
      state.unreadCount = count
    }
  },
  actions: {
    login({ commit }, data) {
      commit('SET_USER', data.user)
      commit('SET_TOKEN', data.token)
    },
    logout({ commit }) {
      commit('CLEAR_USER')
    },
    async syncUser({ commit, state }) {
      // 从后端同步当前用户信息，确保与JWT Token一致
      if (!state.token) return
      
      try {
        const axios = require('axios')
        const service = axios.create({
          baseURL: '/api',
          timeout: 5000
        })
        
        const response = await service.get('/auth/current', {
          headers: {
            'Authorization': 'Bearer ' + state.token
          }
        })
        
        if (response.data && response.data.code === 200) {
          commit('SET_USER', response.data.data)
          console.log('✅ 用户信息已同步:', response.data.data)
        }
      } catch (error) {
        console.error('❌ 同步用户信息失败:', error)
        // 如果token失效，清除登录状态
        if (error.response && error.response.status === 401) {
          commit('CLEAR_USER')
        }
      }
    },
    async loadUnreadCount({ commit }, userId) {
      try {
        // 这里可以调用后端API获取未读消息数
        // const res = await axios.get(`/notification/unread/${userId}`)
        // commit('SET_UNREAD_COUNT', res.data)
        commit('SET_UNREAD_COUNT', 0)
      } catch (error) {
        console.error('加载未读消息数失败:', error)
      }
    }
  },
  getters: {
    isLoggedIn: state => !!state.token,
    isAdmin: state => state.user.role === 'ADMIN',
    currentUser: state => state.user
  }
})

