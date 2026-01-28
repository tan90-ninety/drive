package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.LocationService;
import com.atguigu.daijia.map.client.LocationFeignClient;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import com.atguigu.daijia.model.form.map.UpdateOrderLocationForm;
import com.atguigu.daijia.model.vo.map.OrderLocationVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        Long driverId = updateDriverLocationForm.getDriverId();
        Result<DriverSet> driverSetResult = driverInfoFeignClient.getDriverSet(driverId);
        DriverSet driverSet = driverSetResult.getData();
        if (driverSet.getServiceStatus() != 1){
            throw new GuiguException(ResultCodeEnum.NO_START_SERVICE);
        }
        Result<Boolean> booleanResult = locationFeignClient.updateDriverLocation(updateDriverLocationForm);
        return booleanResult.getData();
    }

    @Override
    public Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm) {
        Result<Boolean> booleanResult = locationFeignClient.updateOrderLocationToCache(updateOrderLocationForm);
        return booleanResult.getData();
    }

    @Override
    public OrderLocationVo getCacheOrderLocation(Long orderId) {
        Result<OrderLocationVo> orderLocationVoResult = locationFeignClient.getCacheOrderLocation(orderId);
        return orderLocationVoResult.getData();
    }
}
