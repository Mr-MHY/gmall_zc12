package com.gmall.service;

import com.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    public CartInfo addCart(String useId,String skuId,Integer num);
    public List<CartInfo> cartList(String useId);
    public List<CartInfo> mergeCartList(String userIdDest,String userIdOrig);
    public void checkCart(String userId,String skuId,String isChecked) ;

    public  List<CartInfo>  getCheckedCartList(String userId);
}

