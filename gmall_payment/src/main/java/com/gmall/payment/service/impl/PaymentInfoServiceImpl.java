package com.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.PaymentInfo;
import com.gmall.payment.mapper.PaymentInfoMapper;
import com.gmall.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PaymentInfoServiceImpl implements PaymentInfoService{

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }
}
