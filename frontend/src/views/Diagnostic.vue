<template>
  <div class="diagnostic-container">
    <el-card shadow="hover">
      <div slot="header">
        <h2>系统诊断页面</h2>
      </div>
      
      <el-button type="primary" @click="runAllTests" :loading="testing">运行所有测试</el-button>
      <el-button @click="clearResults">清空结果</el-button>
      
      <div class="test-results" v-if="results.length > 0">
        <el-table :data="results" style="width: 100%; margin-top: 20px">
          <el-table-column prop="name" label="测试项" width="200"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
                {{ scope.row.status === 'success' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="详情" min-width="300"></el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Diagnostic',
  data() {
    return {
      testing: false,
      results: []
    }
  },
  methods: {
    async runAllTests() {
      this.testing = true
      this.results = []
      
      // 测试1: 检查localStorage
      this.testLocalStorage()
      
      // 测试2: 检查Vuex store
      this.testVuexStore()
      
      // 测试3: 检查axios配置
      this.testAxiosConfig()
      
      // 测试4-11: 测试各个API
      await this.testAPI('统计数据API', '/statistics/home')
      await this.testAPI('内容管理API', '/content/page', { current: 1, size: 10 })
      await this.testAPI('活动管理API', '/activity/page', { current: 1, size: 10 })
      await this.testAPI('校园资讯API', '/news/page', { current: 1, size: 10 })
      await this.testAPI('用户管理API', '/user/page', { current: 1, size: 10 })
      await this.testAPI('操作日志API', '/log/page', { current: 1, size: 10 })
      await this.testAPI('消息通知API', '/notification/page', { current: 1, size: 10 })
      await this.testAPI('问答语料库API', '/qa/corpus/page', { current: 1, size: 10 })
      
      this.testing = false
      this.$message.success('所有测试已完成')
    },
    testLocalStorage() {
      try {
        const token = localStorage.getItem('token')
        const user = localStorage.getItem('user')
        
        if (token && user) {
          const userObj = JSON.parse(user)
          this.results.push({
            name: 'LocalStorage检查',
            status: 'success',
            message: `Token存在, 用户: ${userObj.username}, 角色: ${userObj.role}`
          })
        } else {
          this.results.push({
            name: 'LocalStorage检查',
            status: 'error',
            message: 'Token或用户信息不存在'
          })
        }
      } catch (error) {
        this.results.push({
          name: 'LocalStorage检查',
          status: 'error',
          message: '读取LocalStorage失败: ' + error.message
        })
      }
    },
    testVuexStore() {
      try {
        const user = this.$store.state.user
        const token = this.$store.state.token
        const isAdmin = this.$store.getters.isAdmin
        
        this.results.push({
          name: 'Vuex Store检查',
          status: 'success',
          message: `用户: ${user.username || '未登录'}, Token: ${token ? '存在' : '不存在'}, 管理员: ${isAdmin ? '是' : '否'}`
        })
      } catch (error) {
        this.results.push({
          name: 'Vuex Store检查',
          status: 'error',
          message: 'Vuex Store错误: ' + error.message
        })
      }
    },
    testAxiosConfig() {
      try {
        const baseURL = this.$axios.defaults.baseURL
        const timeout = this.$axios.defaults.timeout
        const token = localStorage.getItem('token')
        
        this.results.push({
          name: 'Axios配置检查',
          status: 'success',
          message: `BaseURL: ${baseURL}, Timeout: ${timeout}ms, Token: ${token ? '已设置' : '未设置'}`
        })
      } catch (error) {
        this.results.push({
          name: 'Axios配置检查',
          status: 'error',
          message: 'Axios配置错误: ' + error.message
        })
      }
    },
    async testAPI(name, url, params = {}) {
      try {
        const res = await this.$axios.get(url, { params })
        
        if (res.code === 200) {
          const dataInfo = res.data
          let message = 'API调用成功'
          
          if (dataInfo && dataInfo.records) {
            message += `, 返回${dataInfo.records.length}条记录, 总计${dataInfo.total}条`
          } else if (typeof dataInfo === 'object') {
            message += `, 返回数据: ${JSON.stringify(dataInfo).substring(0, 100)}`
          }
          
          this.results.push({
            name: name,
            status: 'success',
            message: message
          })
        } else {
          this.results.push({
            name: name,
            status: 'error',
            message: `API返回错误: code=${res.code}, message=${res.message}`
          })
        }
      } catch (error) {
        this.results.push({
          name: name,
          status: 'error',
          message: `API调用失败: ${error.message || error.toString()}`
        })
      }
    },
    clearResults() {
      this.results = []
    }
  }
}
</script>

<style scoped>
.diagnostic-container {
  padding: 20px;
}

.test-results {
  margin-top: 20px;
}
</style>


