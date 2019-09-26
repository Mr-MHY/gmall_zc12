package com.gmall.service;

import com.gmall.bean.PaymentInfo;

public interface PaymentInfoService {

    public void savePaymentInfo(PaymentInfo paymentInfo);

    public PaymentInfo  getPaymentInfo(PaymentInfo paymentInfo);

    public void updatePaymentInfoByOutTradeNo(String outTradeNo,PaymentInfo paymentInfo);

    public  void sendPaymentToOrder(String orderId,String result) ;

    //发送延迟支付结果
    public void sendDelayPaymentResult(String outTradeNo,Long delaySec ,Integer checkCount);
}
