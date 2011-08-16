package com.codeaffine.osgi.services.aop.internal;

import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.codeaffine.osgi.services.aop.JoinPointDefinition;
import com.codeaffine.osgi.services.aop.JoinPointFactory;


public class ProxyRegistrar extends ServiceTracker<Object, Object> {
  private final Class<?> serviceType;
  private final Class<?>[] proxyTypes;
  private final ClassLoader classLoader;
  private final JoinPointDefinition<?> definition;

  @SuppressWarnings( "unchecked" )
  public ProxyRegistrar( BundleContext context, Class<?> serviceType, JoinPointDefinition<?> definition ) {
    super( context, ( Class<Object> )serviceType, null );
    this.serviceType = serviceType;
    this.definition = definition;
    this.proxyTypes = new Class[] { serviceType };
    this.classLoader = serviceType.getClassLoader();
  }
  
  @Override
  public Object addingService( ServiceReference<Object> reference ) {
    Object service = context.getService( reference );
    if( !Proxy.isProxyClass( service.getClass() ) ) {
      ProxyInvocationHandler invocationHandler = new ProxyInvocationHandler( service );
      JoinPointFactory<?> joinPointFactory = definition.getJoinPointFactory();
      invocationHandler.setJoinPoint( joinPointFactory.create() );
      Object proxy = Proxy.newProxyInstance( classLoader, proxyTypes, invocationHandler );
      context.registerService( serviceType.getName(), proxy, copyProperties( reference ) );
    }
    return super.addingService( reference );
  }

  private Dictionary<String, ?> copyProperties( ServiceReference<Object> reference ) {
    Hashtable<String, Object> result = new Hashtable<String, Object>();
    String[] propertyKeys = reference.getPropertyKeys();
    for( String key : propertyKeys ) {
      result.put( key, reference.getProperty( key ) );
    }
    return result;
  }
}