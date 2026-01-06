package com.smartcharge.common;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_EXISTS(1004, "手机号已注册"),
    STATION_NOT_FOUND(2001, "充电站不存在"),
    PILE_NOT_FOUND(2002, "充电桩不存在"),
    PILE_BUSY(2003, "充电桩正在使用中"),
    PILE_FAULT(2004, "充电桩故障"),
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_CANCELLED(3002, "订单已取消"),
    ORDER_COMPLETED(3003, "订单已完成"),
    RESERVATION_EXPIRED(3004, "预约已过期"),
    BALANCE_INSUFFICIENT(4001, "余额不足"),
    PAYMENT_FAILED(4002, "支付失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

