package com.maicard.security.dto;

import com.maicard.base.BaseDto;
import com.maicard.security.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDto extends BaseDto {


    protected long uuid;

    protected long ownerId;

    protected String authKey;

    /**
     * 昵称
     */
    protected String nickName;

    /**
     * 性别 男=1，女=0
     */
    protected int gender;

    /**
     * 用户登录名
     */
    protected String username;

    /**
     * 用户密码
     */
    protected String userPassword;

    /**
     * 头像
     */
    protected String avatar;

    /**
     * 国家
     */
    protected String country;

    /**
     * 省份
     */
    protected String province;

    protected Date createTime;

    /**
     * 地市
     */
    protected String city;


    /**
     * 手机
     */
    protected String mobile;

    /**
     * 邮箱
     */
    protected String email;

    protected String token;


    public static User toUser(UserDto vo) {
        User user = new User();
        BeanUtils.copyProperties(vo,user);
        return user;
    }
}
