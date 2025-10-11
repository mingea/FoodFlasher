package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WECHAT_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User wechatLogin(UserLoginDTO userLoginDTO) {
        log.info("微信登录，微信登录信息：{}", userLoginDTO);
        String openid = getOpenId(userLoginDTO.getCode());
        // openid是否异常
        if (openid == null) {
            throw new RuntimeException(MessageConstant.LOGIN_FAILED);
        }
        // 调用微信接口，获取微信用户信息
        User user = userMapper.getByOpenid(openid);
        // 判断用户是否存在，不存在则自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
                    userMapper.insert(user);
            log.info("新用户注册：{}", user);
        }
        return user;
    }
    /**
     * 获取微信用户openid
     * @param code
     * @return
     */
    private String getOpenId(String code) {
        HashMap<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WECHAT_LOGIN, map);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
