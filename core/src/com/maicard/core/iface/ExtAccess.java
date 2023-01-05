package com.maicard.core.iface;

import javax.servlet.http.HttpServletRequest;

public interface ExtAccess {

    boolean getBooleanDto(String dataCode);

    long getLongDto(String dataCode);

    float getFloatDto(String dataCode);

    void setDto(String dataCode, Object dataValue);


    <T> T getDto(String dataCode);

    <T> T removeDto(String  key);
}
