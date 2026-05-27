import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/styles/global.css'
import axios from './utils/request'

Vue.config.productionTip = false

Vue.use(ElementUI)
Vue.prototype.$axios = axios

// 在应用启动时同步用户信息
if (store.state.token) {
  store.dispatch('syncUser')
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

