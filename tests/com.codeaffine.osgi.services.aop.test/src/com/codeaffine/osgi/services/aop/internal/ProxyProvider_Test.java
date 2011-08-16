package com.codeaffine.osgi.services.aop.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

import com.codeaffine.osgi.services.aop.JoinPointDefinition;
import com.codeaffine.osgi.services.aop.JoinPointFactory;
import com.codeaffine.osgi.services.aop.internal.ProxyProvider;


public class ProxyProvider_Test {
  private ProxyProvider proxyProvider;
  private Bundle proxyProviderBundle;
  private BundleContext proxyProviderBundleContext;

  public interface TestService {
    public void doIt();
  }
  
  @Before
  public void setUp() {
    initializeProxyProvider();
  }

  private void initializeProxyProvider() {
    proxyProvider = new ProxyProvider();
    ComponentContext componentContext = mock( ComponentContext.class );
    proxyProviderBundleContext = mock( BundleContext.class );
    proxyProviderBundle = mock( Bundle.class );
    when( proxyProviderBundleContext.getBundle() ).thenReturn( proxyProviderBundle );
    when( componentContext.getBundleContext() ).thenReturn( proxyProviderBundleContext );
    proxyProvider.activate( componentContext );
  }
  
  @Test
  public void testFind() {
    proxyProvider.addJoinPointDefinition( mockProxyDefintion( TestService.class ) );
    String name = TestService.class.getName();
    Collection<ServiceReference<?>> references = mockOriginReferences();
    
    proxyProvider.find( null, name, null, false, references );
    
    verify( references.iterator() ).remove();
  }
  
  @Test
  public void testFindWithNonMatchingServiceName() {
    proxyProvider.addJoinPointDefinition( mockProxyDefintion( TestService.class ) );
    String name = "anyServiceName";
    Collection<ServiceReference<?>> references = mockOriginReferences();
    
    proxyProvider.find( null, name, null, false, references );
    
    verify( references.iterator(), never() ).remove();
  }
  
  @Test
  public void testFindInCaseOfProxyService() {
    proxyProvider.addJoinPointDefinition( mockProxyDefintion( TestService.class ) );
    String name = TestService.class.getName();
    Collection<ServiceReference<?>> references = mockReferencesWithProxyReference();
    
    proxyProvider.find( null, name, null, false, references );
    
    verify( references.iterator(), never() ).remove();
  }

  @SuppressWarnings( "rawtypes" )
  @Test
  public void testAddProxyDefinition() throws InvalidSyntaxException {
    JoinPointDefinition proxyDefinition = mockProxyDefintion( TestService.class );
    
    proxyProvider.addJoinPointDefinition( proxyDefinition );
    
    verify( proxyProviderBundleContext ).addServiceListener( any( ServiceListener.class ),
                                                             any( String.class ) );
  }
  
  private Collection<ServiceReference<?>> mockReferencesWithProxyReference() {
    return mockReferencesFor( proxyProviderBundle );
  }

  private Collection<ServiceReference<?>> mockOriginReferences() {
    return mockReferencesFor( mock( Bundle.class ) );
  }

  private Collection<ServiceReference<?>> mockReferencesFor( Bundle referenceBundle ) {
    @SuppressWarnings( "rawtypes" )
    ServiceReference serviceReference = mock( ServiceReference.class );
    return mockReferencesFor( referenceBundle, serviceReference );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private Collection<ServiceReference<?>> mockReferencesFor( Bundle referenceBundle,
                                                             ServiceReference reference )
  {
    Collection<ServiceReference<?>> result = mock( Collection.class );
    Iterator<ServiceReference<?>> references = mock( Iterator.class );
    when( references.hasNext() ).thenReturn( Boolean.TRUE, Boolean.FALSE );
    when( references.next() ).thenReturn( reference );
    when( result.iterator() ).thenReturn( references );
    when( reference.getBundle() ).thenReturn( referenceBundle );
    return result;
  }

  @SuppressWarnings( "rawtypes" )
  private JoinPointDefinition mockProxyDefintion( Class serviceType ) {
    JoinPointDefinition result = mock( JoinPointDefinition.class );
    JoinPointFactory joinPointFactory = mock( JoinPointFactory.class );
    when( result.getJoinPointFactory() ).thenReturn( joinPointFactory );
    when( joinPointFactory.getServiceType() ).thenReturn( serviceType );
    return result;
  }
}