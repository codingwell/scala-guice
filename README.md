Scala extensions for Google Guice 4.0
=====================================

**Master:** [![Build Status](https://travis-ci.org/codingwell/scala-guice.png?branch=master)](https://travis-ci.org/codingwell/scala-guice)
**Develop:** [![Build Status](https://travis-ci.org/codingwell/scala-guice.png?branch=develop)](https://travis-ci.org/codingwell/scala-guice)

Please submit pull requests against the **develop** branch.

**Note: Multibinding behaviour had changed in beta4, duplicates are now ignored instead of throwing an exception.**

Getting Started
---------------

### Add dependency
We currently support Scala `2.10, 2.11`

##### maven:
```xml
<dependency>
    <groupId>net.codingwell</groupId>
    <artifactId>scala-guice_2.10</artifactId>
    <version>4.0.0-beta5</version>
</dependency>
```

#####sbt:
```scala
"net.codingwell" %% "scala-guice" % "4.0.0-beta5"
```

##### gradle:
```groovy
'net.codingwell:scala-guice_2.10:4.0.0-beta5'
```

### Mixin
Mixin ScalaModule with your AbstractModule for rich scala magic (or ScalaPrivateModule with your PrivateModule):
```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure {
    bind[Service].to[ServiceImpl].in[Singleton]
    bind[CreditCardPaymentService]
    bind[Bar[Foo]].to[FooBarImpl]
    bind[PaymentService].annotatedWith(Names.named("paypal")).to[CreditCardPaymentService]
  }
}

class MyPrivateModule extends PrivateModule with ScalaPrivateModule {
  def configure {
    bind[Foo].to[RealFoo]
    expose[Foo]

    install(new TransactionalBarModule())
    expose[Bar].annotatedWith[Transactional]

    bind[SomeImplementationDetail]
    install(new MoreImplementationDetailsModule())
  }
}
```

### Inject
Wrap the injector in a ScalaInjector for even more rich scala magic:
```scala
object MyServer {
  def main(args: Array[String]) {
    val injector = Guice.createInjector(new MyModule(), new MyPrivateModule)

    import net.codingwell.scalaguice.InjectorExtensions._
    val service = injector.instance[Service]
    val foo = injector.instance[Foo]

    // Retrieve a Bar annotated with Transactional
    val bar = injector.instance[Bar, Transactional]

    // Retrieve a PaymentService annotated with a specific Annotation instance.
    val paymentService = injector.instance[PaymentService](Names.named("paypal"))
    ...
  }
}
```

Additional Features
-------------------

### Module Traits

```scala
class MyModule extends AbstractModule with ScalaModule
```
```scala
class MyPrivateModule extends PrivateModule with ScalaPrivateModule
```

This gives to access to scala style bindings:

```scala
bind[A].to[B]
bind[A].to(classOf[B])
bind[A].to(typeLiteral[B])
bind[A].toInstance("A")
bind[A].annotatedWith[Ann].to[B]
bind[A].annotatedWith( Names.named("name") ).to[B]
bind[A].annotatedWithName("name").to[B]
bind[A].toProvider[BProvider]
bind[A].toProvider[TypeProvider[B]]
bind[A[String]].to[B[String]]
bind[A].to[B].in[Singleton]

bindInterceptor[AOPI](methodMatcher = annotatedWith[AOP])
```

### Multibinding

The ScalaMultibinder adds scala style multibindings:

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure {
    val stringMulti = ScalaMultibinder.newSetBinder[String](binder)
    stringMulti.addBinding.toInstance("A")

    val annotatedMulti = ScalaMultibinder.newSetBinder[A, Annotation](binder)
    annotatedMulti.addBinding.to[A]

    val namedMulti = ScalaMultibinder.newSetBinder[ServiceConfiguration](binder, Names.named("backend"))
    namedMulti.addBinding.toInstance(config.getAdminServiceConfiguration)
  }
}
```

And then they may be retrieved as immutable.Set[T]. (examples in order)

```scala
class StringThing @Inject() (strings: immutable.Set[String]) { ... }

class AThing @Inject() (@Annotation configs: immutable.Set[A]) { ... }

class Service @Inject() (@Names.named("backend") configs: immutable.Set[ServiceConfiguration]) { ... }
```

### OptionBinding

Newly available in Guice 4.0-beta5, we've got some support for OptionalBinder.

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure {
    val optBinder = ScalaOptionBinder.newOptionBinder[String](binder)
    optBinder.setDefault.toInstance("A")
    // To override the default binding (likely in another module):
    optBinder.setBinding.toInstance("B")

    val annotatedOptBinder = ScalaOptionBinder.newOptionBinder[A, Annotation](binder)
    annotatedOptBinder.setDefault.to[A]

    val namedOptBinder = ScalaOptionBinder.newOptionBinder[ServiceConfiguration](binder, Names.named("backend"))
    namedOptBinder.setBinding.toInstance(config.getAdminServiceConfiguration)
  }
}
```

And then they may be retrieved as `Option[T]`, `Option[Provider[T]]`, and `Option[javax.inject.Provider[T]]`. (examples in order)

```scala
class StringThing @Inject() (name: Option[String]) { ... }

class AThing @Inject() (@Annotation aProvider: Option[Provider[T]]) { ... }

class Service @Inject() (@Names.named("backend") configProvider: Option[javax.inject.Provider[ServiceConfiguration]]) { ... }
```

### MapBinding

The ScalaMapBinder adds scala style mapbindings:

```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure {
    val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
    mBinder.addBinding("1").toInstance(1)
  }
}
```

And then may be retrieved as any of the following:
- `immutable.Map[K, V]`
- `immutable.Map[K, Provider[V]]`
- `immutable.Map[K, javax.inject.Provider[V]]`

If you call `mapBinder.permitDuplicates()` on the binder then you may also inject:
- `immutable.Map[K, immutable.Set[V]]`
- `immutable.Map[K, immutable.Set[Provider[V]]]`

### Interceptor Binding

bindInterceptor adds scala style interceptor binding

```java
bindInterceptor(Matchers.any(), Matchers.annotatedWith(classOf[Logging]), new LoggingInterceptor())
```

```scala
bindInterceptor[LoggingInterceptor](methodMatcher = annotatedWith[Logging])
```

## Gotchas

In Scala, the words `override` and `with` are reserved and must be escaped to be used.
```scala
Modules.`override`(new BaseModule).`with`(new TestModule)
```

### And the stuff we forgot...

If you find a feature we support but don't mention here, submit an issue and we will add it.

If you find a feature we don't support but want, implement it and send us a pull request. Alternatively, you can file an issue and we may or may not get to it.
