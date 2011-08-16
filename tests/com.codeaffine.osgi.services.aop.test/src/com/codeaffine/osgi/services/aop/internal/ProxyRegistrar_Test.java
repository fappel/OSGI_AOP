package com.codeaffine.osgi.services.aop.internal;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.codeaffine.osgi.services.aop.JoinPointDefinition;
import com.codeaffine.osgi.services.aop.JoinPointFactory;
import com.codeaffine.osgi.services.aop.internal.ProxyRegistrar;


public class ProxyRegistrar_Test {
  private static final String PROP_VALUE = "value";
  private static final String PROP_NAME = "name";
  private static final Class<Runnable> SERVICE_TYPE = Runnable.class;
  
  private BundleContext bundleContext;
  private Runnable service;
  @SuppressWarnings( "rawtypes" )
  private ServiceReference serviceReference;
  private Dictionary<String,Object> properties;

  private static class ProxyInterceptor implements Answer<Object> {
    private Runnable proxy;

    ProxyInterceptor() {
    }

    @Override
    public Object answer( InvocationOnMock invocation )
      throws Throwable
    {
      proxy = ( Runnable )invocation.getArguments()[ 1 ];
      return null;
    }

    Runnable getProxy() {
      return proxy;
    }
  }
  
  
  @Before
  public void setUp() {
    bundleContext = mock( BundleContext.class );
    service = mock( SERVICE_TYPE );
    mockServiceReference();
  }

  @SuppressWarnings( "unchecked" )
  private void mockServiceReference() {
    serviceReference = mock( ServiceReference.class );
    when( bundleContext.getService( serviceReference ) ).thenReturn( service );
    properties = new Hashtable<String,Object>();
    properties.put( PROP_NAME, PROP_VALUE );
    when( serviceReference.getPropertyKeys() ).thenReturn( new String[] { PROP_NAME } );
    when( serviceReference.getProperty( PROP_NAME ) ).thenReturn( PROP_VALUE );
  }
  
  @SuppressWarnings( {
    "unchecked", "rawtypes"
  } )
  @Test
  public void testAddingService() {
    JoinPointDefinition definition = mock( JoinPointDefinition.class );
    JoinPointFactory joinPointFactory = mock( JoinPointFactory.class );
    when( definition.getJoinPointFactory() ).thenReturn( joinPointFactory );
    ProxyRegistrar proxyRegistrar = new ProxyRegistrar( bundleContext, SERVICE_TYPE, definition );
    ProxyInterceptor proxyInterceptor = registerProxyInterceptor();
    
    proxyRegistrar.addingService( serviceReference );
    
    checkDelegatingProxyHasBeenRegistered( proxyInterceptor );
  }

  private void checkDelegatingProxyHasBeenRegistered( ProxyInterceptor proxyInterceptor ) {
    proxyInterceptor.getProxy().run();
    assertNotSame( service, proxyInterceptor.getProxy() );
    verify( service ).run();
    verify( bundleContext ).registerService( eq( SERVICE_TYPE.getName() ),
                                             any( SERVICE_TYPE ), 
                                             eq( properties ) );
  }

  private ProxyInterceptor registerProxyInterceptor() {
    ProxyInterceptor result = new ProxyInterceptor();
    when( bundleContext.registerService( eq( SERVICE_TYPE.getName() ),
                                         any( SERVICE_TYPE ), 
                                         eq( properties ) ) ).thenAnswer( result );
    return result;
  }
  
  @SuppressWarnings( {
    "unchecked", "rawtypes"
  } )
  @Test
  public void testIgnoreAddingServiceWithProxyReferences() {
    JoinPointDefinition definition = mock( JoinPointDefinition.class );
    ProxyRegistrar proxyRegistrar = new ProxyRegistrar( bundleContext, SERVICE_TYPE, definition );
    ProxyInterceptor proxyInterceptor = registerProxyInterceptor();
    createServiceProxy();
    
    proxyRegistrar.addingService( serviceReference );
    
    assertNull( proxyInterceptor.getProxy() );
  }

  @SuppressWarnings( "unchecked" )
  private void createServiceProxy() {
    ClassLoader loader = this.getClass().getClassLoader();
    Class<?>[] interfaces = new Class<?>[] { SERVICE_TYPE };
    InvocationHandler invocationHandler = mock( InvocationHandler.class );
    Object proxy = Proxy.newProxyInstance( loader, interfaces, invocationHandler );
    when( bundleContext.getService( serviceReference ) ).thenReturn( proxy );
  }
}