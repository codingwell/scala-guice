Scala extensions for Google Guice
=================================

Getting Started
---------------

Just add this maven dependency:
```xml
<dependency>
    <groupId>net.codingwell</groupId>
    <artifactId>scala-guice_2.9</artifactId>
    <version>3.0.1</version>
</dependency>
```

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

Wrap the injector in a ScalaInjector for even more rich scala magic:
```scala
object MyServer {
  def main(args: Array[String]) {
    val injector = new ScalaInjector(Guice.createInjector(new MyModule(), new MyPrivateModule))

    val service = injector.instance[Service]
    val foo = injector.instance[Foo]
    ...
  }
}
```

Features:
---------

* `Manifest[T]` -> `TypeLiteral[T]` conversion (`typeLiteral[T]`)
* Binding DSL extensions using type parameters to specify types
* ScalaModule trait including the above binding DSL extensions
* PrivateModule support
* ScalaInjector wrapper over Injector

Future:
-------

* `TypeLiteral[T]` -> `Manifest[T]` conversion
* Have `Manifest[T]` injected anywhere Guice could inject `TypeLiteral[T]` (see [Jesse's post for context](http://blog.publicobject.com/2008/11/guice-punches-erasure-in-face.html))
* Extractor objects for pattern-matching against the binding AST

If technically possible:

* Inject `Option[T]` for optional dependencies
* Injection that takes variance into account

Build Server:
-------------

[Jenkins](http://ci.codingwell.net/job/Scala-Guice/)
