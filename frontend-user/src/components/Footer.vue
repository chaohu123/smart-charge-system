<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h4>关于我们</h4>
          <p>智能充电桩管理系统致力于为新能源车主提供便捷、高效的充电服务。</p>
        </div>
        <div class="footer-section">
          <h4>快速链接</h4>
          <ul>
            <li><a href="#" @click.prevent="$router.push('/orders')">我的订单</a></li>
            <li><a href="#" @click.prevent="$router.push('/wallet')">我的钱包</a></li>
            <li><a href="#" @click.prevent="$router.push('/favorites')">我的收藏</a></li>
            <li><a href="#" @click.prevent="$router.push('/profile')">个人中心</a></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>帮助中心</h4>
          <ul>
            <li><a href="#" @click.prevent="showDialog('user-agreement')">用户协议</a></li>
            <li><a href="#" @click.prevent="showDialog('privacy-policy')">隐私政策</a></li>
            <li><a href="#" @click.prevent="showDialog('service-terms')">服务条款</a></li>
            <li><a href="#" @click.prevent="showDialog('contact')">联系我们</a></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>联系方式</h4>
          <p>客服热线：400-888-8888</p>
          <p>邮箱：service@smartcharge.com</p>
          <div class="social-links">
            <el-icon class="social-icon"><Message /></el-icon>
            <el-icon class="social-icon"><ChatDotRound /></el-icon>
            <el-icon class="social-icon"><Share /></el-icon>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 智能充电桩管理系统. All rights reserved.</p>
        <p>ICP备案号：京ICP备12345678号</p>
      </div>
    </div>
    
    <!-- 协议弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      destroy-on-close
    >
      <div class="dialog-content">
        <p v-if="dialogType === 'user-agreement'">用户协议内容...</p>
        <p v-else-if="dialogType === 'privacy-policy'">隐私政策内容...</p>
        <p v-else-if="dialogType === 'service-terms'">服务条款内容...</p>
        <p v-else-if="dialogType === 'contact'">联系方式：service@smartcharge.com</p>
      </div>
    </el-dialog>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import { Message, ChatDotRound, Share } from '@element-plus/icons-vue'

const dialogVisible = ref(false)
const dialogType = ref('')
const dialogTitle = ref('')

const showDialog = (type) => {
  dialogType.value = type
  const titles = {
    'user-agreement': '用户协议',
    'privacy-policy': '隐私政策',
    'service-terms': '服务条款',
    'contact': '联系我们'
  }
  dialogTitle.value = titles[type] || '提示'
  dialogVisible.value = true
}
</script>

<style scoped>
.app-footer {
  background: #2c3e50;
  color: #ecf0f1;
  margin-top: 60px;
}

.footer-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 60px 32px 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #fff;
}

.footer-section p {
  font-size: 14px;
  line-height: 1.6;
  color: #bdc3c7;
  margin: 8px 0;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section ul li {
  margin-bottom: 12px;
}

.footer-section ul li a {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-section ul li a:hover {
  color: #1a6dff;
}

.social-links {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}

.social-icon {
  font-size: 24px;
  color: #bdc3c7;
  cursor: pointer;
  transition: color 0.3s;
}

.social-icon:hover {
  color: #1a6dff;
}

.footer-bottom {
  border-top: 1px solid #34495e;
  padding-top: 20px;
  text-align: center;
}

.footer-bottom p {
  margin: 4px 0;
  font-size: 12px;
  color: #95a5a6;
}

.dialog-content {
  padding: 20px 0;
  line-height: 1.8;
  color: #212121;
}

@media (max-width: 1200px) {
  .footer-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  
  .footer-container {
    padding: 40px 16px 20px;
  }
}
</style>

