Scala extensions for Google Guice 3.0
=====================================

**Master:** [![Build Status](https://travis-ci.org/codingwell/scala-guice.png?branch=master)](https://travis-ci.org/codingwell/scala-guice)  
**Develop:** [![Build Status](https://travis-ci.org/codingwell/scala-guice.png?branch=develop)](https://travis-ci.org/codingwell/scala-guice)

Getting Started
---------------

### Add dependency
We currently support Scala `2.8.2, 2.9.1, 2.9.2, 2.10`

##### maven:
```xml
<dependency>
    <groupId>net.codingwell</groupId>
    <artifactId>scala-guice_2.10</artifactId>
    <version>3.0.2</version>
</dependency>
```

#####sbt:
```scala
"net.codingwell" % "scala-guice_2.10" % "3.0.2"
"net.codingwell" %% "scala-guice" % "3.0.2"
```

##### gradle:
```groovy
'net.codingwell:scala-guice_2.10:3.0.2'
```

### Mixin
Mixin ScalaModule with your AbstractModule for rich scala magic (or ScalaPrivateModule with your PrivateModule):
```scala
class MyModule extends AbstractModule with ScalaModule {
  def configure {
    bind[Service].to[ServiceImpl].in[Singleton]
    bind[CreditCardPaymentService]
    bind[Bar[Foo]].to[FooBarImpl]
    bind[PaymentService].to[CreditCardPaymentService]
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

    import net.codingwell.scalaguice.ScalaExtensons._
    val service = injector.instance[Service]
    val foo = injector.instance[Foo]
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
bind[A].toProvider[BProvider]
bind[A].toProvider[TypeProvider[B]]
bind[A[String]].to[B[String]]
bind[A].to[B].in[Singleton]
```

### Multibinding

The ScalaMultibinder adds scala style multibindings:

```scala
val multi = ScalaMultibinder.newSetBinder[String]( binder )
multi.addBinding.to[A]
multi.addBinding.toInstance("A")
```

### And the stuff we forgot...

If you find a feature we support but don't mention here, submit an issue and we will add it.

If you find a feature we don't support but want, implement it and send us a pull request. Alternatively, you can file an issue and we may or may not get to it.
