package com.atguigu.daijia.customer.controller;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.model.entity.customer.CustomerInfo;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "客户API接口管理")
@RestController
@RequestMapping("/customer")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "获取客户登录信息")
    @GetMapping("/getCustomerLoginInfo")
    public Result<CustomerLoginVo> getCustomerLoginInfo(@RequestHeader(value = "token") String token) {
        return Result.ok(customerService.getCustomerLoginInfo(token));
    }

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> wxLogin(@PathVariable String code) {
        return Result.ok(customerService.login(code));
    }

    @Operation(summary = "获取客户基本信息")
    @GetMapping("/getCustomerInfo")
    public Result<CustomerInfo> getCustomerInfo() {
        return Result.ok(null);
    }
}

