package com.atguigu.daijia.customer.service.impl;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.client.CustomerInfoFeignClient;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerInfoFeignClient client;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String login(String code) {
        Result<Long> loginResult = client.login(code);

        Integer codeResult = loginResult.getCode();
        if (codeResult != 200) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }

        Long customerId = loginResult.getData();
        if (customerId == null) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                customerId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                TimeUnit.SECONDS
        );

        return token;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(String token) {

        String customerId = (String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        if (!StringUtils.hasText(customerId)) {
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        Result<CustomerLoginVo> customerLoginInfo = client.getCustomerLoginInfo(Long.parseLong(customerId));
        Integer codeResult = customerLoginInfo.getCode();
        if (codeResult != 200) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }
        CustomerLoginVo customerLoginVo = customerLoginInfo.getData();
        if (customerLoginVo == null) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }
        return customerLoginVo;
    }

    @Override
    public CustomerLoginVo getCustomerInfo(Long customerId) {
        Result<CustomerLoginVo> customerLoginInfo = client.getCustomerLoginInfo(customerId);
        Integer codeResult = customerLoginInfo.getCode();
        if (codeResult != 200) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }
        CustomerLoginVo customerLoginVo = customerLoginInfo.getData();
        if (customerLoginVo == null) {
            throw new GuiguException(ResultCodeEnum.FEIGN_FAIL);
        }
        return customerLoginVo;
    }

    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        client.updateWxPhoneNumber(updateWxPhoneForm);
        return true;
    }

}
