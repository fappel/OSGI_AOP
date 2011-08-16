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