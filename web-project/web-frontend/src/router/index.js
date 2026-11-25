import { createRouter, createWebHistory } from 'vue-router'
import Index from '../views/index/index.vue'
import Clazz from '../views/clazz/index.vue'
import Students from '../views/stu/index.vue'
import Dept from '../views/dept/index.vue'
import Emp from '../views/emp/index.vue'
import EmpData from '../views/report/emp/index.vue'
import StuData from '../views/report/stu/index.vue'
import Logs from '../views/log/index.vue'
import Login from '../views/login/index.vue'
import Layout from '../views/layout/index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '',
      component: Layout,
      redirect: '/index',
      children: [
        {path: 'index',name: 'index',component: Index},
        {path: 'clazz',name: 'clazz',component: Clazz},
        {path: 'students',name: 'students',component: Students},
        {path: 'dept',name: 'dept',component: Dept},
        {path: 'emp',name: 'emp',component: Emp},
        {path: 'empData',name: 'empData',component: EmpData},
        {path: 'stuData',name: 'stuData',component: StuData},
        {path: 'logs',name: 'logs',component: Logs},
      ]
    },
    
    {path: '/login',name: 'login',component: Login},
    
  ],
})

export default router
