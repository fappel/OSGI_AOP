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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.FindHook;
import org.osgi.service.component.ComponentContext;

import com.codeaffine.osgi.services.aop.JoinPointDefinition;


public class ProxyProvider implements FindHook {
  private ComponentContext proxyProviderContext;
  private Map<JoinPointDefinition<?>,ProxyRegistrar> joinPointDefinitions;
  private Set<JoinPointDefinition<?>> activationBuffer;
  
  public ProxyProvider() {
    joinPointDefinitions = new HashMap<JoinPointDefinition<?>,ProxyRegistrar>();
    activationBuffer = new HashSet<JoinPointDefinition<?>>();
  }
  public void activate( ComponentContext proxyProviderContext ) {
    synchronized( joinPointDefinitions ) {
      this.proxyProviderContext = proxyProviderContext;
      if( !activationBuffer.isEmpty() ) {
        Iterator<JoinPointDefinition<?>> iterator = activationBuffer.iterator();
        while( iterator.hasNext() ) {
          JoinPointDefinition<?> joinPointDefinition = iterator.next();
          registerJoinPointDefinitions( joinPointDefinition );
        }
        activationBuffer.clear();
      }
    }
  }

  @Override
  public void find( BundleContext bundleContext,
                    String name,
                    String filter,
                    boolean allServices,
                    Collection<ServiceReference<?>> references )
  {
    if( hasProxyDefinitionFor( name ) ) {
      removeOriginServiceReference( references );
    }
  }

  private void removeOriginServiceReference( Collection<ServiceReference<?>> references ) {
    Iterator<ServiceReference<?>> serviceReferences = references.iterator();
    while( serviceReferences.hasNext() ) {
      ServiceReference<?> serviceReference = serviceReferences.next();
      BundleContext providerBundle = proxyProviderContext.getBundleContext();
      if( !( serviceReference.getBundle().equals( providerBundle.getBundle() ) ) ) {
        serviceReferences.remove();
      }
    }
  }

  private boolean hasProxyDefinitionFor( String name ) {
    synchronized( name ) {
      Iterator<JoinPointDefinition<?>> definitions = joinPointDefinitions.keySet().iterator();
      boolean result = false;
      while( !result && definitions.hasNext() ) {
        JoinPointDefinition<?> proxyDefinition = definitions.next();
        result = proxyDefinition.getJoinPointFactory().getServiceType().getName().equals( name );
      }
      return result;
    }
  }

  public void addJoinPointDefinition( JoinPointDefinition<?> joinPointDefinition ) {
    synchronized( joinPointDefinitions ) {
      if( proxyProviderContext == null ) {
        activationBuffer.add( joinPointDefinition );
      } else {
        registerJoinPointDefinitions( joinPointDefinition );
      }
    }
  }
  
  private void registerJoinPointDefinitions( JoinPointDefinition<?> joinPointDefinition ) {
    ProxyRegistrar registrar = createProxyRegistar( joinPointDefinition );
    joinPointDefinitions.put( joinPointDefinition, registrar );
    registrar.open();
  }

  private ProxyRegistrar createProxyRegistar( JoinPointDefinition<?> proxyDefinition ) {
    BundleContext bundleContext = proxyProviderContext.getBundleContext();
    Class<?> serviceType = proxyDefinition.getJoinPointFactory().getServiceType();
    return new ProxyRegistrar( bundleContext, serviceType, proxyDefinition );
  }
}