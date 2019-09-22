package com.gmall.cartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.CartInfo;
import com.gmall.service.CartService;
import com.gmall.web.config.LoginRequire;
import com.gmall.web.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {
    @Reference
    CartService cartService;

    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addCart(@RequestParam("skuId") String skuId, @RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response){

        String userId =(String) request.getAttribute("userId");
        if (userId==null){
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if (userId==null){
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request,response,"user_tmp_id",userId,60*60*24,false);
            }

        }
        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo",cartInfo);
        request.setAttribute("num",num);
        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public  String  cartList(HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");  //查看用户登录id

        if(userId!=null){   //有登录
            List<CartInfo> cartList=null;   //如果登录前（未登录）时，存在临时购物车 ，要考虑合并
            String userTmpId=CookieUtil.getCookieValue(request, "user_tmp_id", false); //取临时id
            if(userTmpId!=null){
                List<CartInfo> cartTempList =  cartService.cartList(  userTmpId);  //如果有临时id ，查是否有临时购物车
                if( cartTempList!=null&&cartTempList.size()>0){
                    cartList=  cartService.mergeCartList(userId,userTmpId); // 如果有临时购物车 ，那么进行合并 ，并且获得合并后的购物车列表
                }
            }
            if(cartList==null||cartList.size()==0){
                cartList =  cartService.cartList(  userId);  //如果不需要合并 ，再取登录后的购物车
            }
            request.setAttribute("cartList",cartList);
        }else {   //未登录 直接取临时购物车
            String userTmpId=CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if(userTmpId!=null) {
                List<CartInfo> cartTempList = cartService.cartList(userTmpId);
                request.setAttribute("cartList",cartTempList);
            }

        }

        return "cartList";
    }


    @PostMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    @ResponseBody
    public void checkCart(@RequestParam("isChecked") String isChecked ,@RequestParam("skuId") String skuId,HttpServletRequest request){
        String userId =(String)request.getAttribute("userId");
        if(userId==null){
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
        }

        cartService.checkCart(userId,skuId,isChecked);

    }
}
