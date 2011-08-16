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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.codeaffine.osgi.services.aop.internal.JoinPointImpl;


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
    void onErrorServe( Throwable throwable );
    
    void beforeServe( String parameter );
    void afterServe( String parameter );
    void onErrorServe( String parameter, Throwable throwable );
  }
  
  @Before
  public void setUp() {
    joinPoint = new JoinPointImpl<Service>( Service.class );
    advice = mock( Advice.class );
  }
  
  @Test
  public void testScheduleWithoutParameter() throws Exception {
    joinPoint.schedule( advice ).before().serve();
    joinPoint.schedule( advice ).after().serve();
    joinPoint.schedule( advice ).onError().serve();
    Method serveMethod = Service.class.getMethod( "serve", ( Class<?>[])null );
    Throwable error = new RuntimeException();
    
    joinPoint.excuteBefore( serveMethod, null );
    joinPoint.excuteAfter( serveMethod, null );
    joinPoint.executeOnError( serveMethod, null, error );
    
    verify( advice ).beforeServe();
    verify( advice ).afterServe();
    verify( advice ).onErrorServe( error );
  }
  
  @Test
  public void testScheduleWithParameter() throws Exception {
    joinPoint.schedule( advice ).before().serve( joinPoint.any( String.class ) );
    joinPoint.schedule( advice ).after().serve( joinPoint.any( String.class ) );
    joinPoint.schedule( advice ).onError().serve( joinPoint.any( String.class ) );
    Method serveMethod = Service.class.getMethod( "serve", new Class<?>[] { String.class } );
    Throwable error = new RuntimeException();
    
    String parameter = "parameter";
    joinPoint.excuteBefore( serveMethod, new Object[] { parameter } );
    joinPoint.excuteAfter( serveMethod, new Object[] { parameter } );
    joinPoint.executeOnError( serveMethod, new Object[] { parameter }, error );
    
    verify( advice ).beforeServe( parameter );
    verify( advice ).afterServe( parameter );
    verify( advice ).onErrorServe( parameter, error );
  }
}
