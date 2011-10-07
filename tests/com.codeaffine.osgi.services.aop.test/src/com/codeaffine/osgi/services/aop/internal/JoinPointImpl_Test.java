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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;


public class JoinPointImpl_Test {
  
  private JoinPointImpl<Service> joinPoint;
  private Advice advice;

  interface Service {
    void serve();
    void serve( String parameter );
  }
  
  interface Advice {
    void beforeServe();
    void afterServe();
    void onExceptionServe( Exception exception );
    
    void beforeServe( String parameter );
    void afterServe( String parameter );
    void onExceptionServe( String parameter, Exception exception );
  }
  
  @Before
  public void setUp() {
    joinPoint = new JoinPointImpl<Service>( Service.class );
    advice = mock( Advice.class );
  }
  
  @Test
  public void testScheduleWithoutParameter() throws Exception {
    joinPoint.scheduleBefore( advice ).serve();
    joinPoint.scheduleAfter( advice ).serve();
    joinPoint.scheduleOnException( advice ).serve();
    Method serveMethod = Service.class.getMethod( "serve", ( Class<?>[])null );
    Exception error = new RuntimeException();
    
    joinPoint.excuteBefore( serveMethod, null );
    joinPoint.excuteAfter( serveMethod, null );
    joinPoint.executeOnException( serveMethod, null, error );
    
    verify( advice ).beforeServe();
    verify( advice ).afterServe();
    verify( advice ).onExceptionServe( error );
  }
  
  @Test
  public void testScheduleWithParameter() throws Exception {
    joinPoint.scheduleBefore( advice ).serve( joinPoint.any( String.class ) );
    joinPoint.scheduleAfter( advice ).serve( joinPoint.any( String.class ) );
    joinPoint.scheduleOnException( advice ).serve( joinPoint.any( String.class ) );
    Method serveMethod = Service.class.getMethod( "serve", new Class<?>[] { String.class } );
    Exception error = new RuntimeException();
    
    String parameter = "parameter";
    joinPoint.excuteBefore( serveMethod, new Object[] { parameter } );
    joinPoint.excuteAfter( serveMethod, new Object[] { parameter } );
    joinPoint.executeOnException( serveMethod, new Object[] { parameter }, error );
    
    verify( advice ).beforeServe( parameter );
    verify( advice ).afterServe( parameter );
    verify( advice ).onExceptionServe( parameter, error );
  }
  
  @Test
  public void testHasExceptionAdvice() {
    joinPoint.scheduleOnException( advice ).serve( joinPoint.any( String.class ) );
    
    boolean hasExceptionAdvice = joinPoint.hasExceptionAdvice();
    
    assertTrue( hasExceptionAdvice );
  }
  

  @Test
  public void testOnlyOneExceptionAdviceAllowed() {
    joinPoint.scheduleOnException( advice ).serve( joinPoint.any( String.class ) );
    
    try {
      joinPoint.scheduleOnException( new Object() );
      fail();
    } catch( IllegalStateException expected ) {
      // expected
    }
  }
}
