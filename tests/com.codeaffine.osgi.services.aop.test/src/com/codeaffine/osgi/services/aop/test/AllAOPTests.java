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
