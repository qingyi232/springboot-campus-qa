<template>
  <div class="registration-statistics">
    <div class="page-header">
      <h2>
        <i class="el-icon-data-analysis"></i>
        报名统计分析
      </h2>
      <div class="header-actions">
        <el-button size="small" icon="el-icon-refresh" @click="loadAllData">刷新数据</el-button>
        <el-button size="small" icon="el-icon-download" @click="exportData">导出数据</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
          <i class="el-icon-trophy"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总活动数</div>
          <div class="stat-value">{{ overallStats.totalActivities || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">总报名数</div>
          <div class="stat-value">{{ overallStats.totalRegistrations || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
          <i class="el-icon-success"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">已通过</div>
          <div class="stat-value">{{ overallStats.approvedRegistrations || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
          <i class="el-icon-time"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">待审核</div>
          <div class="stat-value">{{ overallStats.pendingRegistrations || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
          <i class="el-icon-s-data"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">本月新增</div>
          <div class="stat-value">{{ overallStats.monthRegistrations || 0 }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #30cfd0, #330867)">
          <i class="el-icon-sunrise"></i>
        </div>
        <div class="stat-info">
          <div class="stat-label">今日新增</div>
          <div class="stat-value">{{ overallStats.dayRegistrations || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="chart-panel">
          <div class="panel-header">
            <span>报名趋势</span>
            <el-select v-model="trendDays" size="small" @change="loadTrendData">
              <el-option label="最近7天" :value="7"></el-option>
              <el-option label="最近15天" :value="15"></el-option>
              <el-option label="最近30天" :value="30"></el-option>
            </el-select>
          </div>
          <div id="trendChart" style="height: 350px"></div>
        </div>
      </el-col>
      
      <el-col :span="12">
        <div class="chart-panel">
          <div class="panel-header">
            <span>活动分类统计</span>
          </div>
          <div id="categoryChart" style="height: 350px"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <div class="chart-panel">
          <div class="panel-header">
            <span>热门活动排行</span>
          </div>
          <div id="popularChart" style="height: 400px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 用户活跃度 -->
    <div class="content-panel" style="margin-top: 20px">
      <div class="panel-header">
        <span>用户活跃度TOP20</span>
      </div>
      <el-table :data="userActivityList" style="width: 100%">
        <el-table-column type="index" label="排名" width="80" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.$index === 0" type="danger" size="small">🏆</el-tag>
            <el-tag v-else-if="scope.$index === 1" type="warning" size="small">🥈</el-tag>
            <el-tag v-else-if="scope.$index === 2" type="success" size="small">🥉</el-tag>
            <span v-else>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" min-width="150"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
        <el-table-column prop="totalRegistrations" label="总报名数" width="120" sortable></el-table-column>
        <el-table-column prop="approvedCount" label="已通过" width="120" sortable></el-table-column>
        <el-table-column label="通过率" width="120">
          <template slot-scope="scope">
            {{ ((scope.row.approvedCount / scope.row.totalRegistrations) * 100).toFixed(1) }}%
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  getOverallStatistics,
  getRegistrationTrend,
  getCategoryStatistics,
  getPopularActivities,
  getUserActivityStatistics,
  exportData as exportDataApi
} from '@/api/registrationStatistics'

export default {
  name: 'RegistrationStatistics',
  data() {
    return {
      overallStats: {},
      trendDays: 7,
      userActivityList: [],
      trendChart: null,
      categoryChart: null,
      popularChart: null
    }
  },
  mounted() {
    this.loadAllData()
  },
  beforeDestroy() {
    if (this.trendChart) this.trendChart.dispose()
    if (this.categoryChart) this.categoryChart.dispose()
    if (this.popularChart) this.popularChart.dispose()
  },
  methods: {
    async loadAllData() {
      await Promise.all([
        this.loadOverallStats(),
        this.loadTrendData(),
        this.loadCategoryData(),
        this.loadPopularData(),
        this.loadUserActivityData()
      ])
    },
    async loadOverallStats() {
      try {
        const res = await getOverallStatistics()
        if (res.code === 200 && res.data) {
          this.overallStats = res.data
        }
      } catch (error) {
        console.error('加载总体统计失败:', error)
      }
    },
    async loadTrendData() {
      try {
        const res = await getRegistrationTrend(this.trendDays)
        if (res.code === 200 && res.data) {
          this.renderTrendChart(res.data)
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
      }
    },
    async loadCategoryData() {
      try {
        const res = await getCategoryStatistics()
        if (res.code === 200 && res.data) {
          this.renderCategoryChart(res.data)
        }
      } catch (error) {
        console.error('加载分类统计失败:', error)
      }
    },
    async loadPopularData() {
      try {
        const res = await getPopularActivities(10)
        if (res.code === 200 && res.data) {
          this.renderPopularChart(res.data)
        }
      } catch (error) {
        console.error('加载热门活动失败:', error)
      }
    },
    async loadUserActivityData() {
      try {
        const res = await getUserActivityStatistics(20)
        if (res.code === 200 && res.data) {
          this.userActivityList = res.data
        }
      } catch (error) {
        console.error('加载用户活跃度失败:', error)
      }
    },
    renderTrendChart(data) {
      const chartDom = document.getElementById('trendChart')
      if (!chartDom) return
      
      if (!this.trendChart) {
        this.trendChart = echarts.init(chartDom)
      }
      
      const dates = data.map(item => item.date)
      const counts = data.map(item => item.count)
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          boundaryGap: false
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '报名数',
            type: 'line',
            data: counts,
            smooth: true,
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(102, 126, 234, 0.5)' },
                  { offset: 1, color: 'rgba(102, 126, 234, 0.1)' }
                ]
              }
            },
            itemStyle: {
              color: '#667eea'
            }
          }
        ]
      }
      
      this.trendChart.setOption(option)
    },
    renderCategoryChart(data) {
      const chartDom = document.getElementById('categoryChart')
      if (!chartDom) return
      
      if (!this.categoryChart) {
        this.categoryChart = echarts.init(chartDom)
      }
      
      const chartData = data.map(item => ({
        name: item.category,
        value: item.registrationCount
      }))
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: '10%',
          top: 'center'
        },
        series: [
          {
            name: '报名数',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: chartData
          }
        ]
      }
      
      this.categoryChart.setOption(option)
    },
    renderPopularChart(data) {
      const chartDom = document.getElementById('popularChart')
      if (!chartDom) return
      
      if (!this.popularChart) {
        this.popularChart = echarts.init(chartDom)
      }
      
      const activities = data.map(item => item.activityName)
      const counts = data.map(item => item.registrationCount)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: activities.reverse(),
          axisLabel: {
            formatter: function(value) {
              return value.length > 15 ? value.substr(0, 15) + '...' : value
            }
          }
        },
        series: [
          {
            name: '报名数',
            type: 'bar',
            data: counts.reverse(),
            itemStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 1,
                y2: 0,
                colorStops: [
                  { offset: 0, color: '#667eea' },
                  { offset: 1, color: '#764ba2' }
                ]
              },
              borderRadius: [0, 5, 5, 0]
            }
          }
        ]
      }
      
      this.popularChart.setOption(option)
    },
    async exportData() {
      try {
        this.$message.info('正在导出数据...')
        const res = await exportDataApi()
        if (res.code === 200 && res.data) {
          // 简单地将数据转为JSON并下载
          const dataStr = JSON.stringify(res.data, null, 2)
          const blob = new Blob([dataStr], { type: 'application/json' })
          const url = URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `报名数据_${new Date().toLocaleDateString()}.json`
          link.click()
          URL.revokeObjectURL(url)
          this.$message.success('导出成功')
        }
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败')
      }
    }
  }
}
</script>

<style scoped>
.registration-statistics {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h2 i {
  color: #409EFF;
  font-size: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #2c3e50;
}

.chart-panel,
.content-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}
</style>


