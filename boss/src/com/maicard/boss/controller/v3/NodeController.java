package com.maicard.boss.controller.v3;

import com.maicard.base.CriteriaMap;
import com.maicard.core.constants.Constants;
import com.maicard.core.constants.EisError;
import com.maicard.core.constants.OpResult;
import com.maicard.security.annotation.AllowJsonOutput;
import com.maicard.site.constants.NodeType;
import com.maicard.site.entity.Template;
import com.maicard.utils.ClassUtils;
import com.maicard.wesite.boss.abs.ValidateBaseController;
import com.maicard.core.constants.ViewNames;
import com.maicard.core.entity.EisMessage;
import com.maicard.security.annotation.RequestPrivilege;
import com.maicard.security.entity.User;
import com.maicard.site.entity.Node;
import com.maicard.site.service.NodeService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.maicard.core.constants.ViewNames.frontMessageView;

/**
 * 管理系统-栏目控制器
 */
@Controller
@RequestMapping("/node")
public class NodeController extends ValidateBaseController {

    @Resource
    private NodeService nodeService;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    /**
     * 根据条件列出课程
     *
     * @return message MsgVo
     */
    @RequestMapping(value = "/tree")
    @RequestPrivilege(object = "node", operate = "r")
    public String tree(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }

        CriteriaMap criteriaMap = CriteriaMap.create(partner.getOwnerId()).put("nodeTypeId", Node.NODE_TYPE_NEWS);
        criteriaMap.setCacheType(Node.class);
        map.clear();


        List<Node> tree = nodeService.listInTree(criteriaMap);

        map.put("message", EisMessage.success());
        map.put("rows", tree);
        return frontMessageView;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @AllowJsonOutput
    public String delete(HttpServletRequest request, HttpServletResponse response, ModelMap map) throws Exception {
        long id = ServletRequestUtils.getLongParameter(request, "id", 0);

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }
        boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

        if (!isPlatformGenericPartner) {
            map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
            return ViewNames.partnerMessageView;
        }


        Node template = nodeService.select(id);
        if (template == null) {
            logger.warn("找不到要删除的栏目，ID=" + id);
            map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到要删除的栏目ID=" + id));
            return ViewNames.partnerMessageView;
        }
        if (template.getOwnerId() != partner.getOwnerId()) {
            logger.warn("要删除的栏目，partner.getOwnerId()[" + template.getOwnerId() + "]与系统会话中的partner.getOwnerId()不一致:" + id);
            map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到要删除的栏目ID=" + id));
            return ViewNames.partnerMessageView;
        }
        try {
            if (nodeService.delete(id) > 0) {
                map.put("message", EisMessage.success(OpResult.success.getId(), "成功删除栏目:" + id));
                return ViewNames.partnerMessageView;
            }
        } catch (DataIntegrityViolationException forignKeyException) {
            String error = " 无法删除栏目[" + id + "]，因为与其他数据有关联.";
            logger.error(error);
            map.put("message", EisMessage.error(EisError.RELATION_DATA_CONFILECT.id, error));
            return ViewNames.partnerMessageView;
        }


        map.put("message", EisMessage.error(EisError.DATA_ERROR.id, "无法删除栏目"));
        return ViewNames.partnerMessageView;

    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @AllowJsonOutput
    public String create(HttpServletRequest request, HttpServletResponse response, ModelMap map,
                         Node node) throws Exception {
        final String view = ViewNames.partnerMessageView;
        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }
        boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

        if (!isPlatformGenericPartner) {
            map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
            return ViewNames.partnerMessageView;
        }

        node.setOwnerId(partner.getOwnerId());


        if (StringUtils.isBlank(node.getPath())) {
            logger.error("新增栏目未提交前端路径");

            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "请输入栏目路径"));
            return view;
        }
        if (node.getAlias() == null) {
            String[] data = node.getPath().split("/");
            String alias = data[data.length - 1];
            logger.info("新增栏目未提供别名代码，使用路径:{}的最后一部分", node.getPath(), alias);
            node.setAlias(alias);
        } else if (!".".equals(node.getAlias()) && !node.getAlias().matches("[A-Za-z0-9_]+")) {
            logger.error("新增栏目别名代码不合法:" + node.getAlias());
            map.put("message", EisMessage.error(EisError.REQUIRED_PARAMETER.id, "别名不合法，只允许英文字母、数字和下划线"));
            return view;
        }
        if (StringUtils.isBlank(node.getSiteCode())) {
            node.setSiteCode(Constants.DEFAULT_SITE_CODE);
        }
        if (!".".equals(node.getAlias()) && node.getNodeTypeId() < 1) {
            node.setNodeTypeId(NodeType.normal.getId());
        }
        EisMessage message = null;
        try {
            logger.info("在栏目[" + node.getParentNodeId() + "]下新增栏目[" + node.getTitle() + "],默认模版ID[" + node.getTemplateId() + "].路径path:" + node.getPath());
            if (nodeService.insert(node) > 0) {
                message = EisMessage.success(OpResult.success.getId(), "栏目添加成功");
            } else {
                message = EisMessage.error(OpResult.failed.getId(), "栏目添加失败");
            }

        } catch (Exception e) {

            logger.error(ExceptionUtils.getFullStackTrace(e));
            String m = e.getMessage();
            if (m != null && m.indexOf("Duplicate entry") > 0) {
                map.put("message", EisMessage.error(EisError.dataDuplicate.id, "数据重复，请检查输入"));
                return view;
            }
            map.put("message", EisMessage.error(EisError.DATA_UPDATE_FAIL.id, "无法新增栏目"));
            return view;
        }
        map.put("message", message);
        return view;
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @AllowJsonOutput
    public String update(HttpServletRequest request, HttpServletResponse response, ModelMap map, Node node) throws Exception {

        User partner = new User();
        boolean validateResult = loginValidate(request, response, map, partner);
        if (!validateResult) {
            return ViewNames.partnerMessageView;
        }
        boolean isPlatformGenericPartner = authorizeService.isPlatformGenericPartner(partner);

        if (!isPlatformGenericPartner) {
            map.put("message", EisMessage.error(EisError.ownerNotMatch.id, "您尚未登录，请先登录"));
            return ViewNames.partnerMessageView;
        }

        String updateAttribute = ServletRequestUtils.getStringParameter(request, "nativeData");

        Node _oldNode = nodeService.select(node.getId());
        if (_oldNode == null) {
            map.put("message", EisMessage.error(EisError.OBJECT_IS_NULL.id, "找不到指定的栏目"));
            return ViewNames.partnerMessageView;
        }

        boolean updateFlag = false;
        String attributeName = updateAttribute.replaceFirst("^" + Constants.NATIVE_KEY_PREFIX, "");
        String dataValue = ServletRequestUtils.getStringParameter(request, attributeName);
        if (StringUtils.isNotBlank(dataValue)) {
            ClassUtils.setAttribute(_oldNode, attributeName, dataValue.trim(), Constants.COLUMN_TYPE_NATIVE);
            updateFlag = true;
            logger.info("准备更新单独的原生属性:{}=>{}", attributeName, dataValue);
        }

        EisMessage message = null;
        if (updateFlag) {
            _oldNode.incrVersion();
            int rs = nodeService.update(_oldNode);
            logger.info("提交修改栏目:{}，修改结果:{}", _oldNode.getId(), rs);
            if (rs == 1) {
                message = EisMessage.success("栏目修改成功");
            } else {
                message = EisMessage.error(OpResult.failed.getId(), "栏目修改失败");
            }
        } else {
            logger.info("提交修改栏目:{}但是没有修改的内容", _oldNode.getId());
            message = EisMessage.error(OpResult.failed.getId(), "没有需要修改的内容");
        }

        map.put("node", _oldNode);
        map.put("message", message);
        return ViewNames.partnerMessageView;
    }


}
