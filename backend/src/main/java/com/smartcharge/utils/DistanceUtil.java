package com.smartcharge.utils;

/**
 * 距离计算工具类
 * 使用Haversine公式计算两点间的地理距离
 */
public class DistanceUtil {
    private static final double EARTH_RADIUS = 6371.0; // 地球半径（公里）

    /**
     * 计算两点间距离（Haversine公式）
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 距离（米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLon1 = Math.toRadians(lon1);
        double radLon2 = Math.toRadians(lon2);

        // 计算差值
        double deltaLat = radLat2 - radLat1;
        double deltaLon = radLon2 - radLon1;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(radLat1) * Math.cos(radLat2) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离（公里转米）
        return EARTH_RADIUS * c * 1000;
    }
}

