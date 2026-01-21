package com.atguigu.daijia.map.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.map.service.LocationService;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.map.SearchNearByDriverForm;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import com.atguigu.daijia.model.vo.map.NearByDriverVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        Point point = new Point(updateDriverLocationForm.getLongitude().doubleValue(), updateDriverLocationForm.getLatitude().doubleValue());
        redisTemplate.opsForGeo().add(
                RedisConstant.DRIVER_GEO_LOCATION, point,
                updateDriverLocationForm.getDriverId().toString()
        );
        return true;
    }

    @Override
    public Boolean removeDriverLocation(Long driverId) {
        redisTemplate.opsForGeo().remove(RedisConstant.DRIVER_GEO_LOCATION, driverId.toString());
        return true;
    }

    @Override
    public List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm) {
        Point point = new Point(searchNearByDriverForm.getLongitude().doubleValue(), searchNearByDriverForm.getLatitude().doubleValue());
        Distance distance = new Distance(SystemConstant.NEARBY_DRIVER_RADIUS, RedisGeoCommands.DistanceUnit.KILOMETERS);
        Circle circle = new Circle(point, distance);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();
        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = redisTemplate.opsForGeo().radius(RedisConstant.DRIVER_GEO_LOCATION, circle, args);
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = radius.getContent();

        List<NearByDriverVo> nearByDriverVoList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : content) {
            Long driverId = Long.parseLong(geoResult.getContent().getName());

            Result<DriverSet> driverSetResult = driverInfoFeignClient.getDriverSet(driverId);
            DriverSet driverSet = driverSetResult.getData();

            // 判断订单历程
            BigDecimal orderDistance = driverSet.getOrderDistance();
            if (orderDistance.compareTo(BigDecimal.ZERO) != 0 && orderDistance.doubleValue() < searchNearByDriverForm.getMileageDistance().doubleValue()) {
                continue;
            }

            // 判断接单距离
            BigDecimal acceptDistance = driverSet.getAcceptDistance();
            BigDecimal currentDistant = BigDecimal.valueOf(geoResult.getDistance().getValue()).setScale(2, RoundingMode.HALF_UP);
            if (acceptDistance.compareTo(BigDecimal.ZERO) !=0 && acceptDistance.doubleValue() < currentDistant.doubleValue()){
                continue;
            }

            NearByDriverVo nearByDriverVo = new NearByDriverVo();
            nearByDriverVo.setDistance(currentDistant);
            nearByDriverVo.setDriverId(driverId);
            nearByDriverVoList.add(nearByDriverVo);
        }
        return nearByDriverVoList;
    }
}
