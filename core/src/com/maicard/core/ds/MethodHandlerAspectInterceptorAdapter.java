package com.maicard.core.ds;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Created with IntelliJ IDEA.
 * @author: iron
 * Date: 16-12-6
 * Time: 上午9:55
 * To change this template use File | Settings | File Templates.
 */
public abstract class MethodHandlerAspectInterceptorAdapter implements MethodHandlerAspectInterceptor {

    @Override
    public void doBefore(JoinPoint jp) {

    }
    @Override
    public Object doAround(ProceedingJoinPoint jp) throws Throwable {
        return null;
    }

    @Override
    public void doAfter(JoinPoint jp) {

    }

    @Override
    public void doThrowing(JoinPoint jp, Throwable ex) {

    }
}
