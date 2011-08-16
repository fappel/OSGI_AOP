package com.codeaffine.osgi.services.aop;


public interface JoinPointDefinition<T> {
  JoinPointFactory<T> getJoinPointFactory();
  void register( JoinPoint<T> joinPoint );
}
