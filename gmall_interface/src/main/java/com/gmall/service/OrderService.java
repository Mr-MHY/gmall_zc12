package com.gmall.service;

import com.gmall.bean.OrderInfo;
import com.gmall.bean.ProcessStatus;

public interface OrderService {


    public  String  saveOrder(OrderInfo orderInfo);

    public OrderInfo getOrderInfo(String orderId);

    public  String  genToken(String userId);

    public  boolean  verifyToken(String userId,String token);

    public  void updateStatus(String orderId, ProcessStatus processStatus, OrderInfo... orderInfo );

}

