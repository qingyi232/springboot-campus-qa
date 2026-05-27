import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  // 学生端路由（现代化门户风格）
  {
    path: '/student',
    component: () => import('@/layout/StudentLayout.vue'),
    redirect: '/student/home',
    children: [
      {
        path: 'home',
        name: 'StudentHome',
        component: () => import('@/views/student/Home.vue'),
        meta: { title: '首页', role: 'STUDENT' }
      },
      {
        path: 'qa',
        name: 'StudentQa',
        component: () => import('@/views/student/Qa.vue'),
        meta: { title: '智能问答', role: 'STUDENT' }
      },
      {
        path: 'news',
        name: 'StudentNews',
        component: () => import('@/views/student/News.vue'),
        meta: { title: '校园资讯', role: 'STUDENT' }
      },
      {
        path: 'news/:id',
        name: 'StudentNewsDetail',
        component: () => import('@/views/student/NewsDetail.vue'),
        meta: { title: '资讯详情', role: 'STUDENT' }
      },
      {
        path: 'activity',
        name: 'StudentActivity',
        component: () => import('@/views/student/Activity.vue'),
        meta: { title: '活动中心', role: 'STUDENT' }
      },
      {
        path: 'activity/:id',
        name: 'StudentActivityDetail',
        component: () => import('@/views/student/ActivityDetail.vue'),
        meta: { title: '活动详情', role: 'STUDENT' }
      },
      {
        path: 'my-registrations',
        name: 'MyRegistrations',
        component: () => import('@/views/student/MyRegistrations.vue'),
        meta: { title: '我的报名', role: 'STUDENT' }
      },
      {
        path: 'customer-service',
        name: 'StudentCustomerService',
        component: () => import('@/views/student/CustomerService.vue'),
        meta: { title: '人工客服', role: 'STUDENT' }
      },
      {
        path: 'feedback',
        name: 'StudentFeedback',
        component: () => import('@/views/student/Feedback.vue'),
        meta: { title: '问题反馈', role: 'STUDENT' }
      },
      {
        path: 'content',
        name: 'StudentContent',
        component: () => import('@/views/Content.vue'),
        meta: { title: '内容中心', role: 'STUDENT' }
      },
      {
        path: 'notification',
        name: 'StudentNotification',
        component: () => import('@/views/Notification.vue'),
        meta: { title: '消息通知', role: 'STUDENT' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息', role: 'STUDENT' }
      }
    ]
  },
  // 管理员端路由（后台管理风格）
  {
    path: '/admin',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/admin/home',
    children: [
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', requiresAdmin: true }
      },
      {
        path: 'qa',
        name: 'AdminQA',
        component: () => import('@/views/QA.vue'),
        meta: { title: '智能问答', requiresAdmin: true }
      },
      {
        path: 'content',
        name: 'AdminContent',
        component: () => import('@/views/Content.vue'),
        meta: { title: '内容管理', requiresAdmin: true }
      },
      {
        path: 'activity',
        name: 'AdminActivity',
        component: () => import('@/views/Activity.vue'),
        meta: { title: '活动管理', requiresAdmin: true }
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('@/views/News.vue'),
        meta: { title: '校园资讯', requiresAdmin: true }
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/User.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/OperationLog.vue'),
        meta: { title: '操作日志', requiresAdmin: true }
      },
      {
        path: 'customer-service',
        name: 'AdminCustomerService',
        component: () => import('@/views/CustomerService.vue'),
        meta: { title: '人工客服', requiresAdmin: true }
      },
      {
        path: 'feedback',
        name: 'AdminFeedback',
        component: () => import('@/views/Feedback.vue'),
        meta: { title: '问题反馈', requiresAdmin: true }
      },
      {
        path: 'registration-management',
        name: 'RegistrationManagement',
        component: () => import('../views/admin/RegistrationManagement.vue'),
        meta: { title: '报名管理', icon: 'el-icon-s-claim' }
      },
      {
        path: 'registration-statistics',
        name: 'RegistrationStatistics',
        component: () => import('@/views/RegistrationStatistics.vue'),
        meta: { title: '报名统计', requiresAdmin: true }
      },
      {
        path: 'notification',
        name: 'AdminNotification',
        component: () => import('@/views/Notification.vue'),
        meta: { title: '消息通知', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息', requiresAdmin: true }
      },
      {
        path: 'diagnostic',
        name: 'Diagnostic',
        component: () => import('@/views/Diagnostic.vue'),
        meta: { title: '系统诊断', requiresAdmin: true }
      }
    ]
  },
  // 根路径重定向
  {
    path: '/',
    redirect: to => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      return user.role === 'ADMIN' ? '/admin/home' : '/student/home'
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 修复导航重复错误
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') throw err
  })
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园智能问答系统` : '校园智能问答系统'
  
  // 如果访问登录页，直接放行
  if (to.path === '/login') {
    next()
    return
  }
  
  // 如果没有token，跳转到登录页
  if (!token) {
    next('/login')
    return
  }
  
  // 如果需要管理员权限但当前用户不是管理员
  if (to.meta.requiresAdmin && user.role !== 'ADMIN') {
    alert('需要管理员权限才能访问此页面')
    next(user.role === 'ADMIN' ? '/admin/home' : '/student/home')
    return
  }
  
  // 如果学生尝试访问管理员路径
  if (to.path.startsWith('/admin') && user.role !== 'ADMIN') {
    alert('需要管理员权限')
    next('/student/home')
    return
  }
  
  next()
})

export default router

