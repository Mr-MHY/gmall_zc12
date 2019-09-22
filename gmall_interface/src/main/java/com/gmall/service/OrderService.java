package com.gmall.service;

import com.gmall.bean.OrderInfo;

public interface OrderService {


    public  void  saveOrder(OrderInfo orderInfo);

    public  String  genToken(String userId);

    public  boolean  verifyToken(String userId,String token);



}

