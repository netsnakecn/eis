package com.maicard.core.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maicard.core.iface.ExtAccess;
import com.maicard.utils.ClassUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonView;
import com.maicard.core.iface.ExtraValueAccess;
import com.maicard.utils.NumericUtils;
import com.maicard.views.JsonFilterView;

@Data
public class BaseEntity implements Serializable, Cloneable, ExtraValueAccess, ExtAccess {

    protected static final long serialVersionUID = -1L;


    public BaseEntity(long id, long ownerId){
    }


    @Id
    protected long id = 0;

    protected long index = 0;

    protected int currentStatus;



    @JsonView({JsonFilterView.Full.class})
    protected long ownerId;        //平台ID

    @JsonView({JsonFilterView.Full.class})
    protected long version;

    protected Map<String, Object> data;

    protected Map<String, Object> ext;

    protected int syncFlag;

    protected int updateMode = 0;

    /**
     * 是否已经完全获取了所有扩展数据和相关数据
     * 0 没有获取任何扩展和关联数据
     * 1 已获取扩展数据orgData
     * 2 已获取地址、评论等挂念数据
     */
    @JsonView(JsonFilterView.Partner.class)
    private int fetchedLevel;

    public BaseEntity() {

    }

    public Map<String,Object> getExt(){
        if(ext == null){
            initExtra();
        }
        return ext;
    }

    @JsonIgnore
    public boolean isCacheable(){
        return false;
    }

    @JsonIgnore
    public String getEntityType(){
        return ClassUtils.getEntityType(this.getClass());
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BaseEntity other = (BaseEntity) obj;
        if (id != 0 && other.id != 0 && id == other.id) {
            return true;
        }
        return false;
    }












    @Override
    public BaseEntity clone() {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(this);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);

            return (BaseEntity) in.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public  String getObject(){
        return ClassUtils.getEntityType(this.getClass());
       // return StringUtils.uncapitalize(this.getClass().getSimpleName());
    }


    public long incrVersion() {
        this.version++;
        return this.version;
    }



    @Override
    public boolean getBooleanDto(String dataCode) {
        if (this.ext == null || this.ext.size() < 1) {
            return false;
        }
        if (this.ext.containsKey(dataCode)) {
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


    @Override
    public String toString(){
        return getEntityType() + "#" + getId();
    }
    /*public static Class<?> getObject() {
        return MethodHandles.lookup().lookupClass();
    }*/

    @Override
    public boolean getBooleanExtra(String dataCode) {
        if (this.data == null || this.data.size() < 1) {
            if (this.ext == null || this.ext.size() < 1) {
                return false;
            }
            if (this.ext.get(dataCode) != null) {
                Object o = this.ext.get(dataCode);
                if (o != null && o.toString().trim().equalsIgnoreCase("true")) {
                    return true;
                }
            }
        }
        if (this.data.get(dataCode) != null) {
            Object o = this.data.get(dataCode);
            if (o != null && o.toString().trim().equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getLongExtra(String dataCode) {
        if (this.data == null || this.data.size() < 1) {
            if (this.ext == null || this.ext.size() < 1) {
                return 0;
            }
            if (this.ext.get(dataCode) != null) {
                return NumericUtils.parseLong(this.data.get(dataCode));
            }
        }
        if (this.data.containsKey(dataCode)) {
            return NumericUtils.parseLong(this.data.get(dataCode));
        }
        return 0;
    }

    @Override
    public float getFloatExtra(String dataCode) {
        if (this.data == null || this.data.size() < 1) {
            if (this.ext == null || this.ext.size() < 1) {
                return 0;
            }
            if (this.ext.get(dataCode) != null) {
                return NumericUtils.parseFloat(this.data.get(dataCode));
            }
        }
        if (this.data.containsKey(dataCode)) {
            return NumericUtils.parseFloat(this.data.get(dataCode));
        }
        return 0;
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


    @Override
    public void setExtra(String dataCode, Object dataValue) {
        if (dataCode == null) {
            return;
        }
        if (this.data == null) {
            initExtra();
        }
        if (dataValue == null) {
            this.data.remove(dataCode);
        } else {
            if (NumericUtils.isNumeric(dataValue)) {
                this.data.put(dataCode, "" + dataValue);
            } else {
                this.data.put(dataCode, dataValue);
            }
        }
    }

    public void initExtra() {
        if(this.data == null)    this.data = new LinkedHashMap<String, Object>();
        if(this.ext == null)        this.ext = new LinkedHashMap<String, Object>();
    }

    public Map<String,Object> getData(){
        if(data == null){
            initExtra();
        }
        return data;
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
    public <T> T removeDto(String dataCode) {

        if (this.ext == null || this.ext.size() < 1) {
            return null;
        }
        if (!this.ext.containsKey(dataCode)) {
            return null;
        }
        Object value = this.ext.remove(dataCode);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getExtra(String dataCode) {

        if (this.data == null || this.data.size() < 1) {
            return null;
        }
        if (!this.data.containsKey(dataCode)) {
            return null;
        }
        Object value = this.data.get(dataCode);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T removeExtra(String dataCode) {

        if (this.data == null || this.data.size() < 1) {
            return null;
        }
        if (!this.data.containsKey(dataCode)) {
            return null;
        }
        Object value = this.data.remove(dataCode);
        if (value == null) {
            return null;
        }
        return (T) value;
    }


    public int getSyncFlag() {
        return syncFlag;
    }


    public void setSyncFlag(int syncFlag) {
        this.syncFlag = syncFlag;
    }

    public int getFetchedLevel() {
        return fetchedLevel;
    }

    public void setFetchedLevel(int fetchedLevel) {
        this.fetchedLevel = fetchedLevel;
    }


    public int getUpdateMode() {
        return updateMode;
    }


    public void setUpdateMode(int updateMode) {
        this.updateMode = updateMode;
    }

}



