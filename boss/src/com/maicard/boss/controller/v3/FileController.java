package com.maicard.boss.controller.v3;

import cn.hutool.core.io.FileUtil;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.entity.EisMessage;
import com.maicard.core.service.ApplicationContextService;
import com.maicard.security.annotation.IgnoreLoginCheck;
import com.maicard.security.annotation.IgnorePrivilegeCheck;
import com.maicard.utils.JsonUtils;
import com.maicard.wesite.service.BizService;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.maicard.core.constants.ViewNames.frontMessageView;

@Controller
@RequestMapping("/file")
public class FileController extends ValidateBaseController {


    @Resource
    private ApplicationContextService applicationContextService;

    @Resource
    private BizService bizService;


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @IgnoreLoginCheck
    @IgnorePrivilegeCheck
    public String upload(HttpServletRequest request, HttpServletResponse response, ModelMap map,@RequestParam Map<String, Object> params
                                ) throws Exception {

       /* User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return VIEW;
        }*/


        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        logger.info("上传文件:" + JsonUtils.toStringFull(multipartRequest.getFileNames()) + ",参数:" + JsonUtils.toStringFull(  params ));
        List<String> fileList = new ArrayList<>();

        String fileType = null;
        if (params.containsKey("type")) {
            fileType = params.get("type").toString();
        }
        if(fileType == null){
            fileType = "default";
        }
        String outDir = applicationContextService.getDataDir() + "/" + bizService.getUploadPath(fileType) + "/";
        Iterator<String> names = multipartRequest.getFileNames();
        while (names.hasNext()) {
            String name = names.next();
            MultipartFile file = multipartRequest.getFile(name);
            String ext = FileUtil.extName(file.getOriginalFilename());
            String fileName = fileType.trim().toLowerCase() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + RandomStringUtils.randomAlphabetic(5).toLowerCase(Locale.ROOT) + "." + ext;
            String fileDest = outDir + fileName;
            logger.info("获取文件:" + name + ",大小:" + file.getSize() + ",类型:" + fileType + ",原始名字:" + file.getOriginalFilename() + ",保存到:" + fileDest);
            File f = new File(fileDest);
            file.transferTo(f);
            fileList.add(fileName);
        }
        map.clear();
        map.put("fileList",fileList);
        map.put("message", EisMessage.success());

        return frontMessageView;
    }
}
