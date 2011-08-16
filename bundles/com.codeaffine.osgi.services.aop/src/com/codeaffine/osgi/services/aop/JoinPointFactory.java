package com.codeaffine.osgi.services.aop;

import com.codeaffine.osgi.services.aop.internal.JoinPointImpl;


public class JoinPointFactory<T> {
  private Class<T> serviceType;
  private JoinPointDefinition<T> proxyDefinition;

  public JoinPointFactory( Class<T> serviceType, JoinPointDefinition<T> proxyDefinition ) {
    this.serviceType = serviceType;
    this.proxyDefinition = proxyDefinition;
  }
  
  public Class<T> getServiceType() {
    return serviceType;
  }
  
  public JoinPoint<T> create() {
    JoinPoint<T> result = new JoinPointImpl<T>( serviceType );
    proxyDefinition.register( result );
    return result;
  }
}