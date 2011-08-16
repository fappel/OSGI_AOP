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