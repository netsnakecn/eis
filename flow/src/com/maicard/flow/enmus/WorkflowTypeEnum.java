package com.maicard.flow.enmus;

/**
 * @author: iron
 * @description: 工作流类型
 * @date : created in  2017/12/13 18:08.
 */
public enum  WorkflowTypeEnum {
    merchant("merchant","商户"),
    document("document","文档"),
    product("product","产品");
    private String id;

    private String name;
    private WorkflowTypeEnum(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static WorkflowTypeEnum getEnum(String id) {
        if(null == id) {
            return null;
        } else {
            WorkflowTypeEnum[] arr$ = values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                WorkflowTypeEnum c = arr$[i$];
                if(id.equals(c.id)) {
                    return c;
                }
            }
            return null;
        }
    }
}
