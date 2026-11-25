import request from '@/utils/request'

export const getDeptList = () => request.get('/depts')

export const addDept = (dept) => request.post('/depts', dept)

export const updateDept = (dept) => request.put(`/depts`, dept)

export const deleteDept = (id) => request.delete(`/depts?id=${id}`)

export const getDeptById = (id) => request.get(`/depts/${id}`)