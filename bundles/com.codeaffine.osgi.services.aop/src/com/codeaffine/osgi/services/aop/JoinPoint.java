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


public interface JoinPoint<T> {

  <P> P any( Class<P> paramType );
  
  T scheduleBefore( Object advice );
  T scheduleAfter( Object advice );
  T scheduleOnException( Object advice );
}