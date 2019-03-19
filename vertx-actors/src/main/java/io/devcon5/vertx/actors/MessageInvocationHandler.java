package io.devcon5.vertx.actors;

import static io.devcon5.vertx.codec.GenericTypeArrayCodec.codecNameFor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 *
 */
class MessageInvocationHandler implements InvocationHandler {

  private final Class contract;
  private final EventBus eb;

  public MessageInvocationHandler(final Vertx vertx, Class contract) {

    this.eb = vertx.eventBus();
    this.contract = contract;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    final String ebAddress = Actors.getImplicitAddress(method);
    //TODO add support for security

    final DeliveryOptions opts = new DeliveryOptions();

    opts.setCodecName(codecNameFor(method.getGenericParameterTypes()));

    final Future result = Future.future();
    eb.send(ebAddress, args, opts, result.completer());
    if (getReturnType(method) == Void.class) {
      //check if really needed
      return null;
    } else if (isNonBlocking(method)) {
        if(getReturnType(method) == Message.class){
          return result;
        } else {
          return result.map(oMsg -> ((Message)oMsg).body());
        }
    } else {
        //TODO throw exception only when running on eventloop thread
        throw new UnsupportedOperationException("Blocking methods are not supported. All methods must have a future "
                                                    + "as return type, but "
                                                    + method
                                                    + " returns "
                                                    + method.getReturnType());
    }
  }

  private boolean isNonBlocking(final Method method) {

    return Future.class.isAssignableFrom(method.getReturnType());
  }

  private Class<?> getReturnType(final Method method) {

    final ReturnType typeHint = method.getAnnotation(ReturnType.class);
    if (typeHint != null) {
      return typeHint.value();
    }

    final Class<?> returnType = method.getReturnType();
    if (Future.class.isAssignableFrom(returnType)) {
      return findTypeArguments(method.getGenericReturnType());
    }
    return returnType;
  }

  private <T> Class<T> findTypeArguments(Type t) {

    if (t == null) {
      return null;
    }
    if (t instanceof ParameterizedType) {
      return (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
    }
    final Class<T> cls = findTypeArguments(((Class) t).getGenericSuperclass());
    if (cls == null) {
      return (Class) t;
    } else {
      return cls;
    }

  }
}