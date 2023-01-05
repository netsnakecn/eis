package com.maicard.core.iface;

public interface ExtraValueAccess {


	boolean getBooleanExtra(String dataCode);

	long getLongExtra(String dataCode);

	float getFloatExtra(String dataCode);


    void setExtra(String dataCode, Object dataValue);


	<T>T getExtra(String dataCode);

	<T>T removeExtra(String dataCode);

}
