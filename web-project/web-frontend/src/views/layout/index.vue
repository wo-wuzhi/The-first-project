<script setup>


</script>

<template>
 
  <el-container>
    <el-header class="header">智能学习辅助系统</el-header>
    
    <el-container class="aside-main">
      <el-aside width=200px class="aside">
        <el-menu 
          router
          default-active="$route.path"
          class="aside-menu"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          
          <el-menu-item index="/index">
            <el-icon><Monitor /></el-icon>首页
          </el-menu-item>

          <el-sub-menu index="/management">
            <template #title>
              <el-icon><User /></el-icon>
              <span>班级学员管理</span>
            </template>
            <el-menu-item index="/clazz">班级管理</el-menu-item>
            <el-menu-item index="/students">学生管理</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统信息管理</span>
            </template>
            <el-menu-item index="/dept">部门管理</el-menu-item>
            <el-menu-item index="/emp">员工管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/report">
            <template #title>
              <el-icon><PieChart /></el-icon>
              <span>数据统计管理</span>
            </template>
            <el-menu-item index="/empData">员工信息统计</el-menu-item>
            <el-menu-item index="/stuData">学员信息统计</el-menu-item>
            <el-menu-item index="/logs">日志信息统计</el-menu-item>
          </el-sub-menu>

        </el-menu>
      </el-aside>

      <el-main>
        <router-view />
      </el-main>

    </el-container>
  </el-container>
</template>

<style scoped>
.header {
  display: flex;
  color: #ece3e3;
  font-size: 40px;
  height: 100px;
  align-items: center;
  background-image: linear-gradient(to right, #545c64 0%, #363738 70%);
}
.aside-main {
  height: 100vh;
}
.aside-menu {
  height: 100%;
  border-right: none;
}
</style>