package com.gmall.passport.controller;

import com.gmall.bean.UserInfo;
import com.gmall.web.util.JwtUtil;
import com.gmall.service.UserService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

public class PassportController {

    @Autowired
    UserService userService;

    @Value("${token.key}")
    String signKey;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){
        // 取得ip地址
        String remoteAddr  = request.getHeader("X-forwarded-for");
        if (userInfo!=null) {
            UserInfo loginUser = userService.login(userInfo);
            if (loginUser == null) {
                return "fail";
            } else {
                // 生成token
                Map map = new HashMap();
                map.put("userId", loginUser.getId());
                map.put("nickName", loginUser.getNickName());
                //公共：加密方法，过期时间  私有：个人用户信息   密钥：盐：IP地址
                String token = JwtUtil.encode(signKey, map, remoteAddr);
                return token;
            }
        }
        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        // 检查token
        // Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if (map!=null){
            // 检查redis信息
            String userId = (String) map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if (userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }




//    @Test
//    public void test01(){
//        String key = "atguigu";
//        String ip="192.168.67.201";
//        Map map = new HashMap();
//        map.put("userId","1001");
//        map.put("nickName","marry");
//        String token = JwtUtil.encode(key, map, ip);
//        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.67.102");
//    }



}
