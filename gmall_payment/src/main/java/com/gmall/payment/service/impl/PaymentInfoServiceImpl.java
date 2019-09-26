package com.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.gmall.bean.PaymentInfo;
import com.gmall.bean.PaymentStatus;
import com.gmall.payment.mapper.PaymentInfoMapper;
import com.gmall.service.PaymentInfoService;
import com.gmall.util.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;


@Service
public class PaymentInfoServiceImpl implements PaymentInfoService{

    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(paymentInfoQuery);
        return paymentInfo;
    }

    @Override
    public void updatePaymentInfoByOutTradeNo(String outTradeNo, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentToOrder(String orderId, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            MessageProducer producer = session.createProducer(session.createQueue("PAYMENT_TO_ORDER"));
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",orderId);
            mapMessage.setString("result",result);
            producer.send(mapMessage);
            session.commit();


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public PaymentStatus checkAlipayPayment(PaymentInfo paymentInfo){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{"+
                "\"out_trade_no\":\""+paymentInfo.getOutTradeNo()+"\""+
                "}");
        AlipayTradeQueryResponse response=null;
        try {
            response=alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()){
            System.out.println("调用成功");
            if ("TRADE_SUCCESS".equals(response.getTradeStatus())){
                return PaymentStatus.PAID;
            }else if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())){
                return PaymentStatus.UNPAID;
            }
        }else {
            System.out.println("调用失败");
            return PaymentStatus.UNPAID;
        }
        return null;
    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, Long delaySec, Integer checkCount) {
        Connection connection = activeMQUtil.getConnection();
        try {
            Session session = connection.createSession(true,Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();

            mapMessage.setString("outTradeNo",outTradeNo);
            mapMessage.setLong("delaySec",delaySec);
            mapMessage.setInt("checkCount",checkCount);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySec*1000);

            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE" ,containerFactory = "jmsQueueListener")
    public void consumeDelayCheck(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("outTradeNo");
        long delaySec = mapMessage.getLong("delaySec");
        Integer checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfoResult = getPaymentInfo(paymentInfoQuery);
        /**
         * 判断要不要检查 如果该笔单据本地已经付款，不需要询问支付宝，也不用再检查一次
         * 如果本地未支付，查支付宝
         * 如果支付宝得到成功 修改状态 发通知给订单 不要再查支付宝
         *
         * 如果支付宝未付款
         * 判断checkCount>0 把延迟次数减一 再发延迟队列
         *
         */
        if (paymentInfoResult.getPaymentStatus()!=PaymentStatus.UNPAID){
            return;
        }
        PaymentStatus paymentStatus = checkAlipayPayment(paymentInfoQuery);
        if (paymentStatus==PaymentStatus.PAID){
            paymentInfoResult.setPaymentStatus(PaymentStatus.PAID);
            updatePaymentInfoByOutTradeNo(outTradeNo,paymentInfoResult);
            sendPaymentToOrder(paymentInfoResult.getOrderId(),"success");
        }else if (paymentStatus==PaymentStatus.UNPAID){
            if (checkCount>0){
                checkCount--;
                sendDelayPaymentResult(outTradeNo,delaySec,checkCount);
            }
        }

    }
}
