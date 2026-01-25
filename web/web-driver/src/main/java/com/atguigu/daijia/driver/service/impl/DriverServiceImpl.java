package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.dispatch.client.NewOrderFeignClient;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.map.client.LocationFeignClient;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private NewOrderFeignClient newOrderFeignClient;

    @Override
    public String login(String code) {
        Result<Long> loginResult = driverInfoFeignClient.login(code);
        Long driverId = loginResult.getData();

        String token = UUID.randomUUID().toString().replaceAll("-", "");

        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token, driverId.toString(), RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public DriverLoginVo getDriverInfo(Long driverId) {
        Result<DriverLoginVo> driverLoginInfo = driverInfoFeignClient.getDriverLoginInfo(driverId);
        return driverLoginInfo.getData();
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        Result<DriverAuthInfoVo> driverAuthInfo = driverInfoFeignClient.getDriverAuthInfo(driverId);
        return driverAuthInfo.getData();
    }

    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        Result<Boolean> booleanResult = driverInfoFeignClient.updateDriverAuthInfo(updateDriverAuthInfoForm);
        return booleanResult.getData();
    }

    @Override
    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        Result<Boolean> booleanResult = driverInfoFeignClient.createDriverFaceModel(driverFaceModelForm);
        return booleanResult.getData();
    }

    @Override
    public Boolean isFaceRecognition(Long driverId) {
        Result<Boolean> faceRecognition = driverInfoFeignClient.isFaceRecognition(driverId);
        return faceRecognition.getData();
    }

    @Override
    public Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        Result<Boolean> booleanResult = driverInfoFeignClient.verifyDriverFace(driverFaceModelForm);
        return booleanResult.getData();
    }

    @Override
    public Boolean startService(Long driverId) {
        DriverLoginVo driverLoginVo = driverInfoFeignClient.getDriverLoginInfo(driverId).getData();
        if (driverLoginVo.getAuthStatus() != 2) {
            throw new GuiguException(ResultCodeEnum.AUTH_ERROR);
        }

        Boolean isFaceRecognition = driverInfoFeignClient.isFaceRecognition(driverId).getData();
        if (!isFaceRecognition) {
            throw new GuiguException(ResultCodeEnum.FACE_ERROR);
        }

        driverInfoFeignClient.updateServiceStatus(driverId, 1);
        locationFeignClient.removeDriverLocation(driverId);
        newOrderFeignClient.clearNewOrderQueueData(driverId);

        return true;
    }

    @Override
    public Boolean stopService(Long driverId) {
        driverInfoFeignClient.updateServiceStatus(driverId, 0);
        locationFeignClient.removeDriverLocation(driverId);
        newOrderFeignClient.clearNewOrderQueueData(driverId);
        return true;
    }
}
