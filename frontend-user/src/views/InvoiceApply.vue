<template>
  <div class="invoice-apply">
    <el-card>
      <template #header>
        <span>申请发票</span>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="订单号">
          <el-input v-model="order.orderNo" disabled />
        </el-form-item>
        <el-form-item label="发票金额">
          <el-input :value="`¥${order.totalAmount || 0}`" disabled />
        </el-form-item>
        <el-form-item label="发票类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="个人">个人</el-radio>
            <el-radio label="企业">企业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发票抬头" prop="title">
          <el-input v-model="form.title" placeholder="请输入发票抬头" />
        </el-form-item>
        <el-form-item
          v-if="form.type === '企业'"
          label="税号"
          prop="taxNumber"
        >
          <el-input v-model="form.taxNumber" placeholder="请输入税号" />
        </el-form-item>
        <el-form-item label="发票内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="3"
            placeholder="请输入发票内容（默认：充电服务费）"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail } from '@/api/order'
import { applyInvoice } from '@/api/invoice'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const order = ref({})

const form = reactive({
  type: '个人',
  title: '',
  taxNumber: '',
  content: '充电服务费'
})

const rules = {
  type: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  taxNumber: [
    { required: true, message: '请输入税号', trigger: 'blur', validator: (rule, value, callback) => {
      if (form.type === '企业' && !value) {
        callback(new Error('企业发票必须填写税号'))
      } else {
        callback()
      }
    }}
  ]
}

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    await loadOrderDetail(orderId)
  } else {
    ElMessage.error('订单ID不能为空')
    router.back()
  }
})

const loadOrderDetail = async (orderId) => {
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载订单信息失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await applyInvoice({
          orderId: order.value.id,
          type: form.type,
          title: form.title,
          taxNumber: form.taxNumber,
          content: form.content
        })
        if (res.code === 200) {
          ElMessage.success('发票申请成功')
          router.push('/invoice')
        }
      } catch (error) {
        ElMessage.error('申请失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.invoice-apply {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}
</style>

