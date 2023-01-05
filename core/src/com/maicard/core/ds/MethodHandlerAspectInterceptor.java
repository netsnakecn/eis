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
public interface MethodHandlerAspectInterceptor {

    public void doBefore(JoinPoint jp);

    public Object doAround(ProceedingJoinPoint jp) throws Throwable;

    public void doAfter(JoinPoint jp);

    public void doThrowing(JoinPoint jp, Throwable ex);
}
