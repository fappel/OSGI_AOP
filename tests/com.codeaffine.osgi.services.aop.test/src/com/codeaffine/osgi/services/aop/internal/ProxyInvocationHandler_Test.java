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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class ProxyInvocationHandler_Test {
  
  interface Service {
    Object serve( Object param );
  }
  
  interface Advise {
    Object beforeServe( Object param );
    Object afterServe( Object param );
    Object onExceptionServe( Object param, Exception exception );
  }
  
  private JoinPointImpl<Service> joinPoint;
  private ProxyInvocationHandler invocationHandler;
  private Method method;
  
  @Mock
  private Service service;
  @Mock
  private Object param;
  @Mock
  private Advise advise;
  
  @Before
  public void setUp() {
    joinPoint = new JoinPointImpl<Service>( Service.class );
    invocationHandler = new ProxyInvocationHandler( service );
    invocationHandler.setJoinPoint( joinPoint );
    initializeMethod();
  }

  private void initializeMethod() {
    try {
      method = Service.class.getMethod( "serve", Object.class );
    } catch( Exception shouldNotHappen ) {
      throw new IllegalStateException( shouldNotHappen );
    }
  }
  
  @Test
  public void testExceptionWithoutAnExceptionAdvice() throws Throwable {
    Exception exception = fakeExceptionInServe();
    
    Object thrown = ( Object )invoke();
    
    assertSame( exception, thrown );
  }

  @Test
  public void testExceptionWithExceptionAdvice() throws Throwable {
    Exception exception = fakeExceptionInServe();
    scheduleExceptionAdvice();
    
    Object result = invoke();
    
    assertNull( result );
    verify( advise ).onExceptionServe( param, exception );
  }
  
  @Test
  public void testExceptionWithExceptionAdviceThatReturnsValue() throws Throwable {
    Exception exception = fakeExceptionInServe();
    Object resultOnException = new Object();
    scheduleExceptionAdvice( exception, resultOnException );
    
    Object result = invoke();
    
    assertSame( resultOnException, result );
    verify( advise ).onExceptionServe( param, exception );
  }
  
  @Test
  public void testExceptionInBeforeAdviceWithoutExceptionAdvice() throws Throwable {
    Exception exception = fakeExceptionInBeforeAdvice();
    
    Object thrown = invoke();
    
    assertSame( exception, thrown );
  }
  
  @Test
  public void testExceptionInBeforeAdviceWithExceptionAdvice() throws Throwable {
    Exception exception = fakeExceptionInBeforeAdvice();
    scheduleExceptionAdvice();

    Object thrown = invoke();

    assertNull( thrown );
    verify( advise ).onExceptionServe( param, exception );
  }
  
  @Test
  public void testExceptionInAfterAdvice() throws Throwable {
    Exception exception = fakeExceptionInAfterAdvice();
    
    Object thrown = invoke();
    
    assertSame( exception, thrown );
  }
  
  @Test
  public void testExceptionInAfterAdviceWithExceptionAdvice() throws Throwable {
    Exception exception = fakeExceptionInAfterAdvice();
    scheduleExceptionAdvice();
    
    Object thrown = invoke();

    assertNull( thrown );
    verify( advise ).onExceptionServe( param, exception );
  }
  
  @Test
  public void testErrorInServe() throws Throwable {
    Error error = fakeErrorInServe();
    scheduleExceptionAdvice();
    
    Object thrown = invoke();
    
    assertSame( error, thrown );
    verify( advise, never() ).onExceptionServe( any( Object.class ), any( Exception.class ) );
  }
  
  @Test
  public void testErrorInBeforeAdvice() throws Throwable {
    Error error = fakeErrorInBeforeAdvice();
    
    Object thrown = invoke();
    
    assertSame( error, thrown );
  }
  
  @Test
  public void testErrorInExceptionAdvice() throws Throwable {
    Error error = fakeErrorInExceptionAdvice();
    
    Object thrown = invoke();
    
    assertSame( error, thrown );
  }
  
  @Test
  public void testErrorInAfterAdvice() throws Throwable {
    Error error = fakeErrorInAfterAdvice();
    
    Object thrown = invoke();

    assertSame( error, thrown );
  }

  private Error fakeErrorInAfterAdvice() {
    scheduleAfterAdvice();
    Error error = new Error( "Error in after" );
    when( advise.afterServe( param ) ).thenThrow( error );
    return error;
  }
  
  private Error fakeErrorInExceptionAdvice() {
    Exception exception = fakeExceptionInServe();
    scheduleExceptionAdvice();
    Error result = new Error( "Error in onException" );
    when( advise.onExceptionServe( param, exception ) ).thenThrow( result );
    return result;
  }

  private Error fakeErrorInBeforeAdvice() {
    scheduleBeforeAdvice();
    Error result = new Error( "Error in beforeServe" );
    when( advise.beforeServe( param ) ).thenThrow( result );
    return result;
  }

  private Error fakeErrorInServe() {
    Error result = new Error( "Error in Serve" );
    when( service.serve( param ) ).thenThrow( result );
    return result;
  }

  private Exception fakeExceptionInBeforeAdvice() {
    scheduleBeforeAdvice();
    Exception result = new RuntimeException( "Exception in before" );
    when( advise.beforeServe( param ) ).thenThrow( result );
    return result;
  }

  private Exception fakeExceptionInAfterAdvice() {
    scheduleAfterAdvice();
    Exception result = new RuntimeException( "Exception in after" );
    when( advise.afterServe( param ) ).thenThrow( result );
    return result;
  }

  private void scheduleAfterAdvice() {
    joinPoint.scheduleAfter( advise ).serve( joinPoint.any( Object.class ) );
  }
  
  private void scheduleBeforeAdvice() {
    joinPoint.scheduleBefore( advise ).serve( joinPoint.any( Object.class ) );
  }
  
  private void scheduleExceptionAdvice( Exception exception, Object resultOnException ) {
    scheduleExceptionAdvice();
    when( advise.onExceptionServe( param, exception ) ).thenReturn( resultOnException );
  }

  private void scheduleExceptionAdvice() {
    joinPoint.scheduleOnException( advise ).serve( joinPoint.any( Object.class ) );
  }
  
  private Exception fakeExceptionInServe() {
    Exception result = new RuntimeException( "Exception in Serve" );
    when( service.serve( param ) ).thenThrow( result );
    return result;
  }
  
  private Object invoke() throws Throwable {
    Object result = null;
    try {
      result = invocationHandler.invoke( null, method, new Object[] { param } );
    } catch( Throwable expected ) {
      result = expected;
    }
    return result;
  }
}
