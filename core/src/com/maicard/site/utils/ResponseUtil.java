package com.maicard.site.utils;

import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import org.springframework.ui.ModelMap;

public class ResponseUtil {

    public static String result(ModelMap modelMap, EisMessage msg){
        modelMap.clear();
        modelMap.put("message", msg);

        return ViewNames.frontMessageView;
    }
    public static String success(ModelMap modelMap){
        modelMap.clear();
        modelMap.put("message", EisMessage.success());

        return ViewNames.frontMessageView;
    }
    public static String error(ModelMap modelMap, int code, String msg){
        modelMap.clear();
        modelMap.put("message", EisMessage.error(code,msg));

        return ViewNames.frontMessageView;
    }
}
