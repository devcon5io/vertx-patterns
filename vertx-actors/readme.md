## vertx-actors

Simplifies the deployment, invocations and implementation of the actor model using Vert.x.

In Vert.x a Verticle are the actors and messages are passed between Verticles using the vert.x
event bus. All messages must be serializable. Out-of-the-box Vert.x supports primitive types,
Json structures and binary buffers to be passed between Verticles. 

Sending and receiving messages require to know the address of the receiver and the definition of 
a callback handler. All of these concepts are rather "technical" and may distract from business logic.

This module builds on top of the Verticles and the EventBus to work directly in Java interfaces which
act as _contracts_ between message sender and receiver (actor). 

# Contracts

Contracts are interfaces that define which operations are provided by an actor that can be invoked by a caller.

These contracts or contractual interfaces have some limitations:

- Return type of each method must be:
    - void - for fire-and-forget methods
    - Future<Void> - if the state of the method invocation is relevant
    - Future<RETURN_TYPE> - carrying the actual response body
    - RETURN_TYPE - is only allowed when the caller _does not_ run on the EventLoop as the call is blocking
     
- Method arguments and RETURN_TYPEs (see above) may be
    - any primitive type or their Object representations, such as boolean and Boolean
    - JsonObject and JsonArray
    - Buffer (vertx)
    - List,Maps or Sets of Pojos/JavaBeans
    - Pojos/JavaBeans that only have fields of the above types. It's recommended to implement the Shareable interface
      of Vertx as this improves communication inside a JVM. Further it's recommended to ensure these
      classes can easily be converted to and from Json 

In the proceedings of this readme, we'll use a very simple contract:

```java
interface MyContract {
  Future<String> hello(String user);
} 
```

# Implementing an Actor

Every Verticle can be used as an actor. 

They must invoke the `Actors.register(this)` method upon startup 
which will scan all interfaces implemented by the actor and registers event bus handlers and codecs for
these methods:


```java
class MyVerticle implements Verticle, MyContract {
  ...
  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Actors.register(this);
    startFuture.complete();
  }
  
  @Override
  public Future<String> hello(String user){
    return Future.succeededFuture("Hello " + user);
  }
}
```

To concentrate on business functionality and inherit all the implicit vertx handling, Actors my extend the AbstractActor class:
```java
class MyActory extends AbstractActor implements MyContract {
  
   @Override
    public Future<String> hello(String user){
      return Future.succeededFuture("Hello " + user);
    }
}
```
By extending the AbstractActor, implementors get:

- access to the Vertx instance
- access to the configuration of the Verticle deployment
- support for Actor-Auto Deployment (see below) 


# Auto-Deployment of Actors
Actors that implement the Actor interface - which all subclasses of AbstractActor implicitly do - can be deployed automatically
when the register themselves as service in the JVM.
This mechanism uses the java ServiceLoader. Modules that provide one or more Actors may define a file in the jar file named
`META-INF/services/io.devcon5.vertx.actors.Actor` that contains a list of all fully-qualified classnames of actors that are 
provided by the module.
The deployment of _all_ actors in the classpath/modulepath can be done with a single invocation:

```java
Future<CompositeFuture> deployment = Actors.deployAll()

//or for passing configurations
JsonObject config = ...
Future<CompositeFuture> deployment = Actors.deployAll(config)
```

# Calling an Actor
Actors can be invoked from everywhere, not necessarily other Actors class - every caller is an implicit actor anyway.
Calling an Actor is done by using the contract the caller wants to use:

```java

MyContract contract = Actors.withContract(MyContract.class);

Future<String> response = contract.hello("Bob");
//now we define what to do with response:
response.setHandler(greeting -> {
  if(greeting.succeeded()){
    System.out.println(greeting.body());
  } else {
    greeting.cause().printStackTrace();
  }
})

```
