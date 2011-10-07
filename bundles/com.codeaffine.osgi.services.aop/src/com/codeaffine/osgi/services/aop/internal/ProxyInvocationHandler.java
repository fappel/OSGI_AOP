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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.codeaffine.osgi.services.aop.JoinPoint;

class ProxyInvocationHandler implements InvocationHandler {
    private final Object service;
    private JoinPointImpl<?> joinPoint;

    ProxyInvocationHandler( Object service ) {
      this.service = service;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
      Object result = null;
      try {
        if( joinPoint != null ) {
          joinPoint.excuteBefore( method, args ); 
        }
        result = method.invoke( service, args );
        if( joinPoint != null ) {
          joinPoint.excuteAfter( method, args );
        }        
      } catch( Exception exception ) {
        Exception cause = getCause( exception );
        if( !joinPoint.hasExceptionAdvice() ) {
          throw cause;
        }
        try {
          result = joinPoint.executeOnException( method, args, cause );
        } catch( Exception errorHandlerException ) {
          throw getCause( errorHandlerException );
        }
      }
      return result;
    }

    private Exception getCause( Exception exception ) {
      Exception result = exception;
      if( exception instanceof InvocationTargetException ) {
        result = ( Exception )stripOfInvocationTargetException( exception ); 
      }
      return result;
    }

    private Throwable stripOfInvocationTargetException( Exception exception ) {
      Throwable result = ( ( InvocationTargetException ) exception ).getTargetException();
      if( result instanceof Error ) {
        throw ( Error )result;
      }
      return result;
    }

    void setJoinPoint( JoinPoint<?> joinPoint ) {
      this.joinPoint = ( JoinPointImpl<?> )joinPoint;
    }
  }