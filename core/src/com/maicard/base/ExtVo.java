package com.maicard.base;

import com.maicard.core.iface.ExtAccess;
import com.maicard.utils.NumericUtils;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 包含了ext数据的Vo
 */
public abstract  class ExtVo extends BaseVo implements ExtAccess {
    protected Map<String,Object> ext;

    public Map<String,Object> getExt(){
        if(ext == null){
            initExtra();
        }
        return ext;
    }
    protected void             initExtra(){
        if(ext == null) {
            ext = new LinkedHashMap<>();
        }
    }

    public void setDto(String dataCode, Object dataValue) {
        if (dataCode == null) {
            return;
        }
        if (this.ext == null) {
            initExtra();
        }
        if (dataValue == null) {
            this.ext.remove(dataCode);
        } else {
            this.ext.put(dataCode, dataValue);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getDto(String dataCode) {

        if (this.ext == null || this.ext.size() < 1) {
            return null;
        }
        if (!this.ext.containsKey(dataCode)) {
            return null;
        }
        Object value = this.ext.get(dataCode);
        if (value == null) {
            return null;
        }
        return (T) value;
    }


    @Override
    public boolean getBooleanDto(String dataCode) {
        if (this.ext == null || this.ext.size() < 1) {
            return false;
        }
        if (this.ext.get(dataCode) != null) {
            Object o = this.ext.get(dataCode);
            if (o != null && o.toString().trim().equalsIgnoreCase("true")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public long getLongDto(String dataCode) {
        if (this.ext == null || this.ext.size() < 1) {
            return 0;
        }
        if (this.ext.containsKey(dataCode)) {
            return NumericUtils.parseLong(this.ext.get(dataCode));
        }
        return 0;

    }

    @Override
    public float getFloatDto(String dataCode) {
        if (this.ext == null || this.ext.size() < 1) {
            return 0;
        }
        if (this.ext.containsKey(dataCode)) {
            return NumericUtils.parseFloat(this.ext.get(dataCode));
        }

        return 0;
    }


}
