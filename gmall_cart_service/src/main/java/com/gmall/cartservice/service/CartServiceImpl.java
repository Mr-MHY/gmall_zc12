package com.gmall.cartservice.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.CartInfo;
import com.gmall.bean.SkuInfo;
import com.gmall.cartservice.mapper.CartInfoMapper;
import com.gmall.service.CartService;
import com.gmall.service.ManageService;
import com.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {
        /**
         * 添加购物车 （skuId,userId,num） 数据库+缓存
         *      数据库
         *          通过传的用户ID，商品ID，查出购物车表之中的详情
         *          若详情不为空，则从商品表中查出对应的商品，进行更新
         *          若为空，则新增
         *      缓存
         *          购物车里面不只有一个商品
         *          使用hash存储userId,skuId,cartInfo(相当于key,map)
         *                 登录之后
         *                      直接使用userId(请求属性)
         *                 未登录
         *                      生成UUID的userId
         * 显示购物车
         *      已登录根据userId查询
         *      未登录根据暂时userID查询
         *      登录之后和未登录是的购物车合并
         *      先从缓存中查出，查出的是一个商品集合，遍历集合放入新集合返回（注意json的转换）
         *      缓存中没有则从数据库查出，并放入缓存,自定义xml语言。查出的是一商品集合，遍历
         *          以商品ID为key存入map中，存入缓存
         *
         */
        CartInfo cartInfoQuery=new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExists=null;
        cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        if(cartInfoExists!=null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        }else{
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());

            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists=cartInfo;
        }

        /*Jedis jedis = redisUtil.getJedis();
        // 加缓存
        //   type  hash     key   cart:101:info     field   skuId   value   cartInfoJson
        //   如果购物车中已有该sku  增加个数  如果没有新增一条
        String cartKey="cart:"+userId+":info";
        String cartInfoJson = JSON.toJSONString(cartInfoExists);
        jedis.hset(cartKey,skuId,cartInfoJson) ;//新增 也可以覆盖

        jedis.close();
        loadCartCache(userId);*/

        return cartInfoExists;

    }

    @Override
    public List<CartInfo> cartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String cartKey="cart:"+userId+":info";
        List<String> cartJsonList = jedis.hvals(cartKey);
        List<CartInfo> cartList=new ArrayList<>();
        if(cartJsonList!=null&&cartJsonList.size()>0){
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            return    cartList;
        }else {
            return loadCartCache(userId);
        }
    }

    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {

        cartInfoMapper.mergeCartList(userIdDest,userIdOrig);

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);

        List<CartInfo> cartInfoList = loadCartCache(userIdDest);

        return cartInfoList;
    }


    public List<CartInfo>  loadCartCache(String userId){

        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithSkuPrice(userId);

        if(cartInfoList!=null&&cartInfoList.size()>0) {
            Map<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            Jedis jedis = redisUtil.getJedis();
            String cartKey = "cart:" + userId + ":info";
            jedis.del(cartKey);
            jedis.hmset(cartKey, cartMap);
            jedis.expire(cartKey, 60 * 60 * 24);
            jedis.close();
        }
        return  cartInfoList;

    }
    public void  loadCartCacheIfNotExists(String userId){
        String cartkey="cart:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartkey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartkey,ttlInt+10);
        Boolean exists = jedis.exists(cartkey);
        jedis.close();
        if( !exists){
            loadCartCache( userId);
        }

    }


    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        loadCartCacheIfNotExists(userId);// 检查一下缓存是否存在 避免因为缓存失效造成 缓存和数据库不一致

        //  isCheck数据 值保存在缓存中
        //保存标志
        String cartKey = "cart:" + userId + ":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey,skuId,cartInfoJsonNew);
        // 为了订单结账 把所有勾中的商品单独 在存放到一个checked购物车中
        String cartCheckedKey = "cart:" + userId + ":checked";
        if(isChecked.equals("1")){  //勾中加入到待结账购物车中， 取消勾中从待结账购物车中删除
            jedis.hset(cartCheckedKey,skuId,cartInfoJsonNew);
            jedis.expire(cartCheckedKey,60*60);
        }else{
            jedis.hdel(cartCheckedKey,skuId);
        }
        jedis.close();

    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        String cartCheckedKey = "cart:" + userId + ":checked";
        Jedis jedis = redisUtil.getJedis();

        List<String> checkedCartList = jedis.hvals(cartCheckedKey);
        List<CartInfo> cartInfoList=new ArrayList<>();
        for (String cartInfoJson : checkedCartList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }


        jedis.close();

        return cartInfoList;
    }
}
