package io.devcon5.vertx.messages;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;

/**
 *
 */
class MessageInvocationHandler implements InvocationHandler {

  private static final Logger LOG = getLogger(MessageInvocationHandler.class);

  private final Vertx vertx;
  private final MessageEncoder messageEncoder;
  private final MessageDecoder messageDecoder;
  private final Class contract;

  public MessageInvocationHandler(final Vertx vertx, Class contract) {

    this.vertx = vertx;
    this.contract = contract;
    this.messageEncoder = new MessageEncoder();
    this.messageDecoder = new MessageDecoder();
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

    final String ebAddress;
    Address address = method.getAnnotation(Address.class);
    if (address == null) {
      ebAddress = contract.getSimpleName() + "." + method.getName();
    } else {
      ebAddress = address.value();
    }
    final Class returnType = getReturnType(method);
    if (returnType == Void.class) {
      vertx.eventBus().send(ebAddress, messageEncoder.encode(args));
      return null;
    } else {
      final Future result = Future.future();
      vertx.eventBus().send(ebAddress, messageEncoder.encode(args), result.completer());

      if (isNonBlocking(method)) {
        return result.map(msgObj -> messageDecoder.decode(((Message) msgObj).body(), returnType));
      } else {
        //TODO throw exception only when running on eventloop thread
        throw new UnsupportedOperationException("Blocking methods are not supported. All methods must have a future "
                                                    + "as return type, but "
                                                    + method
                                                    + " returns "
                                                    + method.getReturnType());
      }

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
