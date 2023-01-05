package com.maicard.boss.controller.v3;

import com.maicard.base.CriteriaMap;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisCache;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.CacheService;
import com.maicard.core.service.CenterDataService;
import com.maicard.mb.service.MessageService;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Random;


/**
 * 系统缓存管理接口
 *
 * @author NetSnake
 */
@Controller
@RequestMapping("/cache")
public class CacheController extends ValidateBaseController {

    @Resource
    private CacheService cacheService;

    @Resource
    private CenterDataService centerDataService;

    @Resource
    private MessageService messageService;


    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String list(HttpServletRequest request,
                       HttpServletResponse response, ModelMap map)
            throws Exception {

        map.clear();
        map.put("rows", _list());
        map.put("message", EisMessage.success());
        return "common/cache/list";
    }


    @RequestMapping(value = "/delete", method= RequestMethod.POST)
    public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         @RequestParam String name) throws Exception {

        Assert.isTrue(StringUtils.isNotBlank(name), "缓存名不能为空");
        EisCache eisCache = new EisCache();
        eisCache.setName(name);
        cacheService.deleteSync(eisCache);


        map.put("message", EisMessage.success());
        return ViewNames.partnerMessageView;
    }

    @RequestMapping("/redis/list")
    @IgnorePrivilegeCheck
    public String listRedis(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
        String key = "rand#" + new Random().nextInt();
        centerDataService.delete(key);
        return ViewNames.partnerMessageView;
    }


    private List<EisCache> _list() {
        List<EisCache> list = cacheService.list(CriteriaMap.create());


        return list;
    }
}

