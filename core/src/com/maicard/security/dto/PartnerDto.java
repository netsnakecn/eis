package com.maicard.security.dto;

import com.maicard.base.BaseDto;
import com.maicard.security.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PartnerDto extends UserDto {

    protected String roles;

    protected int[] role;

    protected Map<String,String>ext;



    public static User toUser(PartnerDto vo) {
        User user = new User();
        BeanUtils.copyProperties(vo,user);
        return user;
    }
}
