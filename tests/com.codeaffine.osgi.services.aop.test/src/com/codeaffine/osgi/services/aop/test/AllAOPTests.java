package com.codeaffine.osgi.services.aop.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.codeaffine.osgi.services.aop.internal.JoinPointImpl_Test;
import com.codeaffine.osgi.services.aop.internal.ProxyProvider_Test;
import com.codeaffine.osgi.services.aop.internal.ProxyRegistrar_Test;

@RunWith( Suite.class )
@Suite.SuiteClasses( {
  ProxyProvider_Test.class,
  ProxyRegistrar_Test.class,
  JoinPointImpl_Test.class
} )
public class AllAOPTests {
  // no content
}
