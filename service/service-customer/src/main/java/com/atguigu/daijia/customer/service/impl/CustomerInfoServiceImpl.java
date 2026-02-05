package com.atguigu.daijia.customer.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.customer.mapper.CustomerInfoMapper;
import com.atguigu.daijia.customer.mapper.CustomerLoginLogMapper;
import com.atguigu.daijia.customer.service.CustomerInfoService;
import com.atguigu.daijia.model.entity.customer.CustomerInfo;
import com.atguigu.daijia.model.entity.customer.CustomerLoginLog;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private CustomerInfoMapper customerInfoMapper;

    @Autowired
    private CustomerLoginLogMapper customerLoginLogMapper;

    @Override
    public Long login(String code) {
        WxMaJscode2SessionResult sessionInfo = null;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        String openid = sessionInfo.getOpenid();

        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfo::getWxOpenId, openid);
        CustomerInfo customerInfo = customerInfoMapper.selectOne(wrapper);

        if (customerInfo == null) {
            customerInfo = new CustomerInfo();
            customerInfo.setWxOpenId(openid);
            customerInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            customerInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            customerInfoMapper.insert(customerInfo);
        }

        CustomerLoginLog customerLoginLog = new CustomerLoginLog();
        customerLoginLog.setCustomerId(customerInfo.getId());
        customerLoginLog.setMsg("小程序登录");
        customerLoginLogMapper.insert(customerLoginLog);

        return customerInfo.getId();
    }

    @Override
    public CustomerLoginVo getCustomerInfo(Long customerId) {
        CustomerInfo customerInfo = customerInfoMapper.selectById(customerId);
        CustomerLoginVo customerLoginVo = new CustomerLoginVo();

        BeanUtils.copyProperties(customerInfo, customerLoginVo);
        customerLoginVo.setIsBindPhone(StringUtils.hasText(customerInfo.getPhone()));

        return customerLoginVo;
    }

    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
        WxMaPhoneNumberInfo phoneNoInfo = null;
        if (updateWxPhoneForm.getCode() != null) {
            try {
                phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(updateWxPhoneForm.getCode());
            } catch (WxErrorException e) {
                throw new GuiguException(ResultCodeEnum.DATA_ERROR);
            }
        } else {
//            微信小程序个人无法获取手机号时的备用学习测试方案
            phoneNoInfo = new WxMaPhoneNumberInfo();
            phoneNoInfo.setPhoneNumber("13632014750");
        }

        String phoneNumber = phoneNoInfo.getPhoneNumber();
        UpdateWrapper<CustomerInfo> customerInfoUpdateWrapper = new UpdateWrapper<>();
        customerInfoUpdateWrapper.eq("id", updateWxPhoneForm.getCustomerId());
        customerInfoUpdateWrapper.set("phone", phoneNumber);
        customerInfoMapper.update(null, customerInfoUpdateWrapper);

        return true;
    }

    @Override
    public String getCustomerOpenId(Long customerId) {
        CustomerInfo customerInfo = customerInfoMapper.selectById(customerId);
        return customerInfo.getWxOpenId();
    }
}
