package com.maicard.boss.controller.v3;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.Config;
import com.maicard.core.entity.DataDefine;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.exception.EisException;
import com.maicard.core.service.DataDefineService;
import com.maicard.security.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/dataDefine")
public class DataDefineController extends ValidateBaseController {

    @Resource
    private DataDefineService dataDefineService;



    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String list(HttpServletRequest request,
                       HttpServletResponse response, ModelMap map, @RequestParam Map<String,Object> param)
            throws Exception {
        long ownerId = (long) map.get("ownerId");
        Assert.isTrue(ownerId > 0, "OwnerId error");
        User partner = new User();
        boolean valid = this.loginValidate(request,response,map, partner);

        if (!valid) {
            logger.error("需要先登录");
            throw new EisException(EisError.userNotFoundInSession.getId(), "请先登录");
        }
        int rows = ServletRequestUtils.getIntParameter(request, "rows", ROW_PER_PAGE);
        int page = ServletRequestUtils.getIntParameter(request, "page", 1);

        CriteriaMap criteriaMap = CriteriaMap.create(param);
        criteriaMap.put("ownerId", ownerId);
        sort(criteriaMap,Config.class);
        map.clear();
        map.put("message",EisMessage.success());
        int total = dataDefineService.count(criteriaMap);
        map.put("total",total);

        List<DataDefine> list = dataDefineService.listOnPage(CriteriaMap.create());
        map.put("rows", list);
        map.put("message", EisMessage.success());
        return VIEW;
    }


    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         @RequestParam long id) throws Exception {

        Assert.isTrue(id > 0, "ID不能为0");
        DataDefine condition = new DataDefine();
        condition.setId(id);
        dataDefineService.deleteAsync(condition);


        map.put("message", EisMessage.success());
        return ViewNames.partnerMessageView;
    }


}

