<script setup>
import {getDeptList,addDept,updateDept,deleteDept,getDeptById} from '@/api/dept'
import { ref, onMounted } from 'vue'
import { ElMessage,ElMessageBox } from 'element-plus'

const deptList = ref([])
const dialogFormVisible = ref(false)
const formtitle = ref('')
const dept = ref({name: ''})
const rules = {
    name: [
        { required: true, message: '部门名称不能为空', trigger: 'blur' },
        { min: 2, max: 10, message: '部门名称长度在2到10个字符之间', trigger: 'blur' }
    ]
}
const deptFormRef = ref(null)

const search = async () => {

    const response = await getDeptList()
    if (response.code){
        deptList.value = response.data
    }
   
}

const openAddDeptDialog = async (dept) => {
    formtitle.value = '添加部门'
    dept.value = {name: ''}
    dialogFormVisible.value = true
    if(deptFormRef.value) {
        deptFormRef.value.clearValidate()
    }
}

const save = async () => {
    if(!deptFormRef.value) return
    deptFormRef.value.validate(async (valid) => {
        if (valid) {
            let result;

            if(dept.value.id) {
                result = await updateDept(dept.value)
            }else {
                result = await addDept(dept.value)
            }
            
            if (result.code) {
                dialogFormVisible.value = false
                ElMessage.success('成功'+formtitle.value)
                search()
            }else {
                ElMessage.error(result.msg)
            }
            
        } else {
            ElMessage.error('请填写正确的信息')
        }
    })
    
}

const openUpdateDeptDialog = async (id) => {
    const result = await getDeptById(id)
    if(deptFormRef.value) {
        deptFormRef.value.clearValidate()
    }
    if (result.code) {
        dept.value = result.data
        formtitle.value = '编辑部门'
        dialogFormVisible.value = true
       
    } else {
        ElMessage.error(result.msg)
    }
}

const openDeleteDeptElMsgBox = async (id) => {
    ElMessageBox.confirm(
        '此操作将永久删除该部门, 是否继续?',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        const result = await deleteDept(id)
        if (result.code) {
            ElMessage.success('删除成功')
            search()
        } else {
            ElMessage.error(result.msg)
        }
    }).catch(() => {
        ElMessage({
            type: 'info',
            message: '已取消删除'
        })
    })
    
}

onMounted(() => {
    search()
})

</script>

<template>
    <h1>部门管理</h1>
    <div class="button-container">
        <el-button type="primary" @click="openAddDeptDialog">+ 添加部门</el-button>
    </div>

    <el-table :data="deptList" border stripe style="width: 100%" class="table-container">
        <el-table-column type="index" label="序号" align="center" />
        <el-table-column prop="name" label="部门名称" align="center"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" align="center"></el-table-column>
        <el-table-column label="操作" align="center">
            <template #default="scope" >
                <el-button type="primary" size="small" @click="openUpdateDeptDialog(scope.row.id)">编辑</el-button>
                <el-button type="danger" size="small" @click="openDeleteDeptElMsgBox(scope.row.id)">删除</el-button>
            </template>
        </el-table-column>
    </el-table>

    <el-dialog v-model="dialogFormVisible" width="500px">
        <template #title>
            <span class="formtitle">{{ formtitle }}</span>
        </template>
        <el-form :model="dept" :rules="rules" ref="deptFormRef">
        <el-form-item label="部门名称" label-width="100px" prop="name">
            <el-input v-model="dept.name" autocomplete="off" />
        </el-form-item>
        </el-form>
        <template #footer>
        <div class="dialog-footer">
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="save">确认</el-button>
        </div>
        </template>
    </el-dialog>

</template>

<style scoped>
h1 {
    border-left: 5px solid #409EFF;
    padding-left: 10px;
}
.button-container {
    margin-bottom: 20px;
}
.table-container {
    padding: 20px;
    border-radius: 4px;
}
.formtitle {
    font-size: 18px;
    font-weight: bold;
    border-left: 5px solid #409EFF;
    padding-left: 10px;
}
</style>
