/*******************************************************************************
 * Copyright (c) 2011 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 ******************************************************************************/
package com.codeaffine.osgi.services.aop.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.codeaffine.osgi.services.aop.JoinPoint;


public class JoinPointImpl<T> implements JoinPoint<T> {
  private static final String POINT_CUT_BEFORE = "before";
  private static final String POINT_CUT_AFTER = "after";
  private static final String POINT_CUT_ON_EXCEPTION = "onException";

  final Class<T> type;
  final List <AdviceHolder> beforeAdvices;
  final List <AdviceHolder> afterAdvices;
  AdviceHolder onExceptionAdvice;
  
  private static class AdviceHolder {
    private final Method targetMethod;
    private final Method adviceMethod;
    private final Object advice;
    
    AdviceHolder( Method targetMethod, Method adviceMethod, Object advice ) {
      this.targetMethod = targetMethod;
      this.adviceMethod = adviceMethod;
      this.advice = advice;
    }

    public Method getTargetMethod() {
      return targetMethod;
    }

    public Method getAdviceMethod() {
      return adviceMethod;
    }

    public Object getAdvice() {
      return advice;
    }
  }

  public JoinPointImpl( Class<T> type ) {
    this.type = type;
    beforeAdvices = new LinkedList<AdviceHolder>();
    afterAdvices = new LinkedList<AdviceHolder>();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public T scheduleBefore( Object advice ) {
    return ( T )createProxy( type, advice, POINT_CUT_BEFORE );
  }

  @Override
  @SuppressWarnings("unchecked")
  public T scheduleAfter( Object advice ) {
    return ( T )createProxy( type, advice, POINT_CUT_AFTER );
  }

  @Override
  @SuppressWarnings("unchecked")
  public T scheduleOnException( Object advice ) {
    checkIfOnExceptionAdviseHasAlreadyBeenRegistered();
    return ( T )createProxy( type, advice, POINT_CUT_ON_EXCEPTION );
  }

  private void checkIfOnExceptionAdviseHasAlreadyBeenRegistered() {
    if( onExceptionAdvice != null ) {
      String pattern = "There is already an exception advise registered for service ''{0}''.";
      String msg = MessageFormat.format( pattern, type.getName() );
      throw new IllegalStateException( msg );
    }
  }

  Object createProxy( Class<T> type, final Object advice, final String prefix ) {
    ClassLoader loader = type.getClassLoader();
    Class<?>[] interfaces = new Class<?>[]{ type };
    InvocationHandler invocationHandler = new InvocationHandler() {
    
      @Override
      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        String name =   prefix 
                      + method.getName().substring( 0,1 ).toUpperCase()
                      + method.getName().substring( 1 );
        if( POINT_CUT_BEFORE.equals( prefix ) ) {
          Method adviceMethod = advice.getClass().getMethod( name, method.getParameterTypes() );
          beforeAdvices.add( new AdviceHolder( method, adviceMethod, advice ) );
        }
        if( POINT_CUT_AFTER.equals( prefix ) ) {
          Method adviceMethod = advice.getClass().getMethod( name, method.getParameterTypes() );
          afterAdvices.add( new AdviceHolder( method, adviceMethod, advice ) );
        }
        if( POINT_CUT_ON_EXCEPTION.equals( prefix ) ) {
          Class<?>[] targetTypes = method.getParameterTypes();
          Class<?>[] parameterTypes = new Class<?>[ targetTypes.length + 1 ];
          System.arraycopy( targetTypes, 0, parameterTypes, 0, targetTypes.length );
          parameterTypes[ targetTypes.length ] = Exception.class;
          Method adviceMethod = advice.getClass().getMethod( name, parameterTypes );
          onExceptionAdvice = new AdviceHolder( method, adviceMethod, advice );
        }
        return null;
      }
    };
    return Proxy.newProxyInstance( loader, interfaces, invocationHandler );
  }
  
  @Override
  public <P> P any( Class<P> paramType ) {
    return null;
  }

  public void excuteBefore( Method method, Object[] args ) throws Exception {
    executeAdvices( method, args, beforeAdvices.iterator() );
  }

  public void excuteAfter( Method method, Object[] args ) throws Exception {
    executeAdvices( method, args, afterAdvices.iterator() );
  }
  
  public Object executeOnException( Method method, Object[] args, Exception exception )
    throws Exception
  {
    Object[] arguments = args == null ? new Object[ 0 ] : args;
    Object[] argsWithException = new Object[ arguments.length + 1 ];
    System.arraycopy( arguments, 0, argsWithException, 0, arguments.length );
    argsWithException[ arguments.length ] = exception;
    return executeAdvice( onExceptionAdvice, argsWithException );
  }  
  
  public boolean hasExceptionAdvice() {
    return onExceptionAdvice != null;
  }
  
  private Object executeAdvices( Method method, Object[] args, Iterator<AdviceHolder> advices )
    throws Exception
  {
    Object result = null;
    while( advices.hasNext() ) {
      AdviceHolder holder = advices.next();
      if( holder.getTargetMethod().equals( method ) ) {
        result = executeAdvice( holder, args );
      }
    }
    return result;
  }

  private Object executeAdvice( AdviceHolder holder, Object[] args ) throws Exception {
    Method adviceMethod = holder.getAdviceMethod();
    adviceMethod.setAccessible( true );
    return adviceMethod.invoke( holder.getAdvice(), args );
  }
}