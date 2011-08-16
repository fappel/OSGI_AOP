package com.codeaffine.osgi.services.aop.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.codeaffine.osgi.services.aop.JoinPoint;


public class JoinPointImpl<T> implements JoinPoint<T> {
  private static final String POINT_CUT_BEFORE = "before";
  private static final String POINT_CUT_AFTER = "after";
  private static final String POINT_CUT_ON_ERROR = "onError";

  final Class<T> type;
  final List <AdviceHolder> beforeAdvices;
  final List <AdviceHolder> afterAdvices;
  final List <AdviceHolder> onErrorAdvices;
  
  private static class AdviceHolder {
    private final Method targetMethod;
    private final Method adviceMethod;
    private final Object advice;
    
    AdviceHolder( Method targetMethod, Method adviceMethod, Object advice ) {
      this.targetMethod = targetMethod;
      this.adviceMethod = adviceMethod;
      this.advice = advice;
    }

    public Method getTargetMethod() {
      return targetMethod;
    }

    public Method getAdviceMethod() {
      return adviceMethod;
    }

    public Object getAdvice() {
      return advice;
    }
  }

  public JoinPointImpl( Class<T> type ) {
    this.type = type;
    beforeAdvices = new LinkedList<AdviceHolder>();
    afterAdvices = new LinkedList<AdviceHolder>();
    onErrorAdvices = new LinkedList<AdviceHolder>();
  }

  Object createProxy( Class<T> type, final Object advice, final String prefix ) {
    ClassLoader loader = type.getClassLoader();
    Class<?>[] interfaces = new Class<?>[]{ type };
    InvocationHandler invocationHandler = new InvocationHandler() {
    
      @Override
      public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
        String name =   prefix 
                      + method.getName().substring( 0,1 ).toUpperCase()
                      + method.getName().substring( 1 );
        if( POINT_CUT_BEFORE.equals( prefix ) ) {
          Method adviceMethod = advice.getClass().getMethod( name, method.getParameterTypes() );
          beforeAdvices.add( new AdviceHolder( method, adviceMethod, advice ) );
        }
        if( POINT_CUT_AFTER.equals( prefix ) ) {
          Method adviceMethod = advice.getClass().getMethod( name, method.getParameterTypes() );
          afterAdvices.add( new AdviceHolder( method, adviceMethod, advice ) );
        }
        if( POINT_CUT_ON_ERROR.equals( prefix ) ) {
          Class<?>[] targetTypes = method.getParameterTypes();
          Class<?>[] parameterTypes = new Class<?>[ targetTypes.length + 1 ];
          System.arraycopy( targetTypes, 0, parameterTypes, 0, targetTypes.length );
          parameterTypes[ targetTypes.length ] = Throwable.class;
          Method adviceMethod = advice.getClass().getMethod( name, parameterTypes );
          onErrorAdvices.add( new AdviceHolder( method, adviceMethod, advice ) );
        }
        return null;
      }
    };
    return Proxy.newProxyInstance( loader, interfaces, invocationHandler );
  }

  @Override
  public PointCut<T> schedule( final Object advice ) {
    return new PointCut<T>() {

      @SuppressWarnings( "unchecked" )
      @Override
      public T before() {
        return ( T )createProxy( type, advice, POINT_CUT_BEFORE );
      }

      @SuppressWarnings( "unchecked" )
      @Override
      public T after() {
        return ( T )createProxy( type, advice, POINT_CUT_AFTER );
      }

      @SuppressWarnings( "unchecked" )
      @Override
      public T onError() {
        return ( T )createProxy( type, advice, POINT_CUT_ON_ERROR );
      }
    };
  }
  
  @Override
  public <P> P any( Class<P> paramType ) {
    return null;
  }

  public void excuteBefore( Method method, Object[] args ) {
    executeAdvices( method, args, beforeAdvices.iterator() );
  }

  public void excuteAfter( Method method, Object[] args ) {
    executeAdvices( method, args, afterAdvices.iterator() );
  }
  
  public void executeOnError( Method method, Object[] args, Throwable error ) {
    Object[] arguments = args == null ? new Object[ 0 ] : args;
    Object[] argsWithError = new Object[ arguments.length + 1 ];
    System.arraycopy( arguments, 0, argsWithError, 0, arguments.length );
    argsWithError[ arguments.length ] = error;
    executeAdvices( method, argsWithError, onErrorAdvices.iterator() );
    
  }  
  
  private void executeAdvices( Method method, Object[] args, Iterator<AdviceHolder> advices ) {
    while( advices.hasNext() ) {
      AdviceHolder holder = advices.next();
      if( holder.getTargetMethod().equals( method ) ) {
        executeAdvice( holder, args );
      }
    }
  }

  private void executeAdvice( AdviceHolder holder, Object[] args ) {
    try {
      Method adviceMethod = holder.getAdviceMethod();
      adviceMethod.setAccessible( true );
      adviceMethod.invoke( holder.getAdvice(), args );
    } catch( IllegalArgumentException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch( IllegalAccessException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch( InvocationTargetException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}