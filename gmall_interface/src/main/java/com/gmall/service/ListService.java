package com.gmall.service;


import com.gmall.bean.SkuLsInfo;
import com.gmall.bean.SkuLsParams;
import com.gmall.bean.SkuLsResult;

public interface ListService {

    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) ;

    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams );

    public  void incrHotScore(String skuId);
}