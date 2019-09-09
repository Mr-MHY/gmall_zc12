package com.gmall.orderweb.controller;



import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.UserInfo;
import com.gmall.service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Reference
    UserService userService;

    @GetMapping("trade")
    public UserInfo trade(@RequestParam("userid") String userid){
        UserInfo userInfo = userService.getUserInfoById(userid);

        return  userInfo;
    }
}
