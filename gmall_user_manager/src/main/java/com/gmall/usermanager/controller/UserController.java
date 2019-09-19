package com.gmall.usermanager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.UserInfo;

import com.gmall.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Reference
    UserService userService;


    @GetMapping("allUsers")
    public List<UserInfo> getAllList(){
        return userService.getUserInfoListAll();
    }

    @PostMapping("addUser")
    public String addUser(UserInfo userInfo){
        userService.addUser(userInfo);
        return "success";
    }

    @PostMapping("updateUser")
    public String updateUser(UserInfo userInfo){
        userService.updateUser(userInfo);
        return "success";
    }

    @PostMapping("updateUserByName")
    public String updateUserByName(UserInfo userInfo){
        userService.updateUserByName(userInfo.getName(),userInfo);
        return "success";
    }

    @PostMapping("delUser")
    public String delUser(UserInfo userInfo){
        userService.delUser(userInfo);
        return "success";
    }

    @GetMapping("getUser")
    public UserInfo getUser (String id){
        return userService.getUserInfoById(id);

    }


}
