package com.maicard.core.exception;

import com.maicard.core.constants.EisError;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;

public class EisException extends RuntimeException {
   
	private static final long serialVersionUID = -8885914334858893402L;
	protected String id;
    protected String message;
    protected String errorCode;
    protected String realClassName;

    public EisException() {
        this.initId();
    }


    public EisException(String errorCode) {
        this.initId();
        this.errorCode = errorCode;
    }


    public EisException(int errorCode) {
        this.initId();
        this.errorCode = String.valueOf(errorCode);
    }
    
    public EisException(int errorCode, String message) {
        this.initId();
        this.errorCode = String.valueOf(errorCode);
        this.message = message;
    }


    public void initId() {
        this.id = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String getMessage() {
        String mes = "";
        Field[] fields = this.getClass().getDeclaredFields();
        Field[] arr$ = fields;
        int len$ = fields.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Field field = arr$[i$];

            try {
                field.setAccessible(true);
                Object e = field.get(this);
                if(e instanceof EisException) {
                	EisException jps = (EisException)e;
                    if(jps.getErrorCode() == this.getErrorCode()) {
                        mes = field.getName();
                        break;
                    }
                }
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

        return mes + " " + this.errorCode + " " + this.message;
    }

    public String getJustMessage() {
        return this.message;
    }

    public void setMessage(String message, Object... args) {
        this.message = MessageFormat.format(message, args);
    }

   

    public boolean codeEquals(EisException e) {
        return e == null?false:(!e.getClass().equals(this.getClass())?false:e.getErrorCode() == this.getErrorCode());
    }

    public EisException upcasting() {
        if(this.getClass().equals(EisException.class)) {
            return this;
        } else {
            EisException superexception = new EisException(this.errorCode);
            superexception.message = this.message;
            superexception.realClassName = this.getClass().getName();
            superexception.id = this.id;
            superexception.setStackTrace(this.getStackTrace());
            return superexception;
        }
    }

    @SuppressWarnings("rawtypes")
	public EisException downcasting() {
        if(this.realClassName != null && !EisException.class.getName().equals(this.realClassName)) {
            Class<?> clz = null;

            try {
                clz = Class.forName(this.realClassName);
            } catch (Exception var5) {
                ;
            }

            if(clz == null) {
                return this;
            } else {
                try {
                    Constructor e = clz.getDeclaredConstructor(new Class[]{String.class});
                    e.setAccessible(true);
                    EisException newException = (EisException)e.newInstance(new Object[]{this.errorCode});
                    newException.message = this.message;
                    newException.id = this.id;
                    newException.setStackTrace(this.getStackTrace());
                    return newException;
                } catch (Throwable var4) {
                    throw new RuntimeException("create exception instance fail : " + var4.getMessage(), var4);
                }
            }
        } else {
            return this;
        }
    }

    public String getRealClassName() {
        return this.realClassName == null?this.getClass().getName():this.realClassName;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public StackTraceElement[] getCoreStackTrace() {
        ArrayList list = new ArrayList();
        StackTraceElement[] stackTrace = this.getStackTrace();
        int len$ = stackTrace.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            StackTraceElement traceEle = stackTrace[i$];
            if(traceEle.getClassName().startsWith("com.yeepay")) {
                list.add(traceEle);
            }
        }

        stackTrace = new StackTraceElement[list.size()];
        return (StackTraceElement[])list.toArray(stackTrace);
    }

    public String getCoreStackTraceStr() {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] arr$ = this.getCoreStackTrace();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            StackTraceElement traceEle = arr$[i$];
            sb.append("\n" + traceEle.toString());
        }

        return sb.toString();
    }

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}


