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

  public interface PointCut<T> {
    T before();
    T after();
    T onError();
  }

  PointCut<T> schedule( Object advice );
  <P> P any( Class<P> paramType );
}