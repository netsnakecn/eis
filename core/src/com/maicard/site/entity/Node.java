package com.maicard.site.entity;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.constants.Constants;
import com.maicard.core.entity.IndexableEntity;
import com.maicard.utils.NumericUtils;
import com.maicard.views.JsonFilterView;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Node extends IndexableEntity {
    @Serial
    private static final long serialVersionUID = -1L;

    public static final int NODE_TYPE_NEWS = 1;
    public static final int NODE_TYPE_BIZ = 4;


    public boolean isCacheable(){
        return true;
    }

    /**
     * 栏目的分类
     */
    protected String category;


    /**
     * 栏目的二级分类
     */
    protected String classify;

    /**
     * 与外部系统关联的ID
     */
    @JsonView(JsonFilterView.Partner.class)
    protected String outId;

    /**
     * 节点的预览图地址
     */
    protected String pic;

    /**
     * 浏览该栏目所需的级别
     */
    @JsonView(JsonFilterView.Partner.class)
    protected int viewLevel;


    @JsonView(JsonFilterView.Partner.class)
    protected long parentNodeId;


    /**
     * 该栏目类型，如果是业务类型，则应当出现在网站导航栏中，如电商的所有商品这种栏目
     */
    @JsonView(JsonFilterView.Partner.class)
    protected int nodeTypeId;

    @JsonView(JsonFilterView.Partner.class)
    protected int level;

    @JsonView(JsonFilterView.Partner.class)
    protected String alias;

    @JsonView(JsonFilterView.Partner.class)
    protected int displayWeight;


    @JsonView(JsonFilterView.Partner.class)
    protected String redirectTo;

    protected String path;

    @JsonView(JsonFilterView.Partner.class)
    protected String processor;

    @JsonView(JsonFilterView.Partner.class)
    protected long templateId;

    @JsonView(JsonFilterView.Partner.class)
    protected String siteCode;

    @JsonView(JsonFilterView.Partner.class)
    protected Date lastModified;

    @JsonView(JsonFilterView.Partner.class)
    protected Set<IncludeNodeConfig> includeNodeSet;


    //非持久化属性
    @JsonView(JsonFilterView.Partner.class)
    protected String templateLocation;

    @JsonView(JsonFilterView.Partner.class)
    protected List<Node> subNodeList;


    @JsonView(JsonFilterView.Partner.class)
    protected String parentNodeName;

    @JsonView(JsonFilterView.Partner.class)
    protected List<Node> nodePath;

    protected String tags;

    /**
     * 访问该栏目的URL，动态生成
     */
    protected String viewUrl;


    public Node() {
    }

    @Override
    public Node clone() {
        return (Node) super.clone();

    }

    public long getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(long parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public int getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(int nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) id;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Node other = (Node) obj;
        if (id != other.id)
            return false;

        return true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
                "(" +
                "nodeId=" + "'" + id + "'," +
                "nodeTypeId=" + "'" + nodeTypeId + "'," +
                "title=" + "'" + title + "'," +
                "currentStatus=" + "'" + currentStatus + "'," +
                "ownerId=" + "'" + ownerId + "'" +
                ")";
    }

    //	@Override
    @JsonIgnore
    public String getViewUrl() {
        if (this.viewUrl != null) {
            return this.viewUrl;
        }
        this.viewUrl = "/" + Constants.CONTENT_PREFIX + this.path + "/index.shtml";
        return this.viewUrl;


    }


    @JsonIgnore
    public long getClusterId() {
        long id = NumericUtils.parseLong(this.outId);
        if (id > 0) {
            return id;
        } else {
            return getLongExtra("clusterId");
        }
    }

    @JsonIgnore
    public void setClusterId(long id) {
        this.setExtra("clusterId", id);
        this.outId = String.valueOf(id);
    }
}
