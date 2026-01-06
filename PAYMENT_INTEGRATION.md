# 支付功能集成说明

## 1. 支付方式

系统支持以下支付方式：
- **余额支付**：使用电子钱包余额支付（已实现）
- **微信支付**：集成微信支付SDK（待配置）
- **支付宝支付**：集成支付宝SDK（待配置）

## 2. 微信支付集成

### 2.1 准备工作

1. 注册微信支付商户号
2. 获取以下信息：
   - AppID
   - 商户号（MchID）
   - API密钥（API Key）
   - 证书文件（用于退款等高级功能）

### 2.2 配置

在 `application.yml` 中配置：

```yaml
wechat:
  app-id: YOUR_WECHAT_APPID
  mch-id: YOUR_MCH_ID
  api-key: YOUR_API_KEY
  notify-url: https://your-domain.com/api/payment/wechat/notify
```

### 2.3 实现支付接口

需要在 `PaymentServiceImpl` 中实现微信支付逻辑：

```java
// 创建微信支付订单
// 调用微信支付统一下单API
// 返回支付参数给前端
// 前端调用微信支付SDK完成支付
```

### 2.4 支付回调

实现 `PaymentController` 中的回调接口：

```java
@PostMapping("/wechat/notify")
public Result<?> wechatNotify(@RequestBody String xmlData) {
    // 验证签名
    // 更新订单状态
    // 返回成功响应
}
```

## 3. 支付宝支付集成

### 3.1 准备工作

1. 注册支付宝开放平台账号
2. 创建应用并获取：
   - AppID
   - 应用私钥
   - 支付宝公钥

### 3.2 配置

在 `application.yml` 中配置：

```yaml
alipay:
  app-id: YOUR_ALIPAY_APPID
  private-key: YOUR_PRIVATE_KEY
  public-key: YOUR_PUBLIC_KEY
  notify-url: https://your-domain.com/api/payment/alipay/notify
```

### 3.3 实现支付接口

需要在 `PaymentServiceImpl` 中实现支付宝支付逻辑：

```java
// 创建支付宝支付订单
// 调用支付宝统一收单接口
// 返回支付表单给前端
// 前端提交表单完成支付
```

### 3.4 支付回调

实现 `PaymentController` 中的回调接口：

```java
@PostMapping("/alipay/notify")
public Result<?> alipayNotify(@RequestParam Map<String, String> params) {
    // 验证签名
    // 更新订单状态
    // 返回success
}
```

## 4. 前端集成

### 4.1 微信支付

在 `Payment.vue` 中，当选择微信支付时：

```javascript
if (paymentMethod.value === 1) {
  // 调用后端接口获取支付参数
  const res = await pay(order.value.id, 1)
  if (res.code === 200) {
    // 调用微信支付SDK
    wx.chooseWXPay({
      timestamp: res.data.timeStamp,
      nonceStr: res.data.nonceStr,
      package: res.data.package,
      signType: res.data.signType,
      paySign: res.data.paySign,
      success: function() {
        // 支付成功
        ElMessage.success('支付成功')
        router.push('/orders')
      }
    })
  }
}
```

### 4.2 支付宝支付

在 `Payment.vue` 中，当选择支付宝支付时：

```javascript
if (paymentMethod.value === 2) {
  // 调用后端接口获取支付表单
  const res = await pay(order.value.id, 2)
  if (res.code === 200) {
    // 创建表单并提交
    const form = document.createElement('form')
    form.method = 'POST'
    form.action = 'https://openapi.alipay.com/gateway.do'
    form.innerHTML = res.data.formHtml
    document.body.appendChild(form)
    form.submit()
  }
}
```

## 5. 支付流程

1. 用户选择支付方式
2. 前端调用 `/api/payment/pay` 接口
3. 后端根据支付方式：
   - 余额支付：直接扣款，更新订单状态
   - 微信/支付宝：创建支付订单，返回支付参数
4. 前端调用支付SDK完成支付
5. 支付平台回调后端接口
6. 后端验证并更新订单状态
7. 前端轮询或接收WebSocket通知，更新订单状态

## 6. 注意事项

1. **安全性**：
   - 所有支付接口必须使用HTTPS
   - 验证支付回调的签名
   - 防止重复支付

2. **幂等性**：
   - 支付接口需要保证幂等性
   - 使用订单号作为幂等键

3. **异常处理**：
   - 支付超时处理
   - 支付失败回滚
   - 网络异常重试

4. **测试**：
   - 使用沙箱环境测试
   - 测试各种支付场景
   - 测试回调处理

## 7. 当前状态

- ✅ 余额支付：已实现
- ⏳ 微信支付：SDK已引入，需要配置和实现
- ⏳ 支付宝支付：SDK已引入，需要配置和实现

## 8. 下一步

1. 配置微信支付和支付宝的商户信息
2. 实现支付接口的具体逻辑
3. 实现支付回调处理
4. 前端集成支付SDK
5. 测试支付流程

