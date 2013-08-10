/*
 *  Copyright 2010-2011 Benjamin Lings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.codingwell.scalaguice

import com.google.inject._
import binder._
import java.lang.annotation.Annotation

/**
 * Allows binding via type parameters. Mix into <code>AbstractModule</code>
 *  (or subclass) to allow using a type parameter instead of
 * <code>classOf[Foo]</code> or <code>new TypeLiteral[Bar[Foo]] {}</code>.
 *
 * For example, instead of
 * {{{
 * class MyModule extends AbstractModule {
 *   def configure {
 *     bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *     bind(classOf[CreditCardPaymentService])
 *     bind(new TypeLiteral[Bar[Foo]]{}).to(classOf[FooBarImpl])
 *     bind(classOf[PaymentService]).to(classOf[CreditCardPaymentService])
 *   }
 * }
 * }}}
 * use
 * {{{
 * class MyModule extends AbstractModule with ScalaModule {
 *   def configure {
 *     bind[Service].to[ServiceImpl].in[Singleton]
 *     bind[CreditCardPaymentService]
 *     bind[Bar[Foo]].to[FooBarImpl]
 *     bind[PaymentService].to[CreditCardPaymentService]
 *   }
 * }
 * }}}
 *
 * '''Note''' This syntax allows binding to and from generic types.
 * It doesn't currently allow bindings between wildcard types because the
 * manifests for wildcard types don't provide access to type bounds.
 */

trait InternalModule[B <: Binder] {
  import ScalaModule._

  protected[this] def binderAccess: B

  protected[this] def bind[T: Manifest] = new ScalaAnnotatedBindingBuilder[T] {
    val myBinder = binderAccess
    val self = myBinder.bind(typeLiteral[T])
  }

  protected[this] def bindScope[T <: Annotation : Manifest](scope: Scope) = binderAccess.bindScope(cls[T], scope)
  protected[this] def requestStaticInjection[T: Manifest](): Unit = binderAccess.requestStaticInjection(cls[T])
  protected[this] def getProvider[T: Manifest] = binderAccess.getProvider(cls[T])
  protected[this] def getMembersInjector[T: Manifest] = binderAccess.getMembersInjector(typeLiteral[T])

  private[this] def cls[T: Manifest] = manifest[T].erasure.asInstanceOf[Class[T]]
}

trait ScalaModule extends AbstractModule with InternalModule[Binder] {
  // should be:
  // this: AbstractModule =>
  // see http://lampsvn.epfl.ch/trac/scala/ticket/3564

  //Hack, no easy way to exclude the bind method that gets added to classes inheriting ScalaModule
  //So we experimentally figured out how many calls up is the source, so we use that
  //Commit 52c2e92f8f6131e4a9ea473f58be3e32cd172ce6 has better class exclusion
  protected[this] def binderAccess = super.binder.withSource( (new Throwable).getStackTrace()(4) ) // should not need super
}

trait ScalaPrivateModule extends PrivateModule with InternalModule[PrivateBinder] {
  // should be:
  // this: PrivateModule =>
  // see http://lampsvn.epfl.ch/trac/scala/ticket/3564

  import ScalaModule._

  //Hack, no easy way to exclude the bind method that gets added to classes inheriting ScalaModule
  //So we experimentally figured out how many calls up is the source, so we use that
  //Commit 52c2e92f8f6131e4a9ea473f58be3e32cd172ce6 has better class exclusion
  protected[this] def binderAccess = super.binder.withSource( (new Throwable).getStackTrace()(5) ) // should not need super

  protected[this] def expose[T: Manifest] = new ScalaAnnotatedElementBuilder[T] {
     val myBinder = binderAccess
     val self = myBinder.expose(typeLiteral[T])
  }
}

object ScalaModule {
  import java.lang.annotation.{Annotation => JAnnotation}

  trait ScalaScopedBindingBuilder extends ScopedBindingBuilderProxy {
    def in[TAnn <: JAnnotation : ClassManifest] = self in annotation[TAnn]
  }

  trait ScalaLinkedBindingBuilder[T] extends ScalaScopedBindingBuilder
    with LinkedBindingBuilderProxy[T] { outer =>
    def to[TImpl <: T : Manifest] = new ScalaScopedBindingBuilder {
      val self = outer.self to typeLiteral[TImpl]
    }
    def toProvider[TProvider <: Provider[_ <: T] : Manifest] = new ScalaScopedBindingBuilder {
      val self = outer.self toProvider typeLiteral[TProvider]
    }
  }

  trait ScalaAnnotatedBindingBuilder[T] extends ScalaLinkedBindingBuilder[T]
    with AnnotatedBindingBuilderProxy[T] { outer =>
    def annotatedWith[TAnn <: JAnnotation : ClassManifest] = new ScalaLinkedBindingBuilder[T] {
      val self = outer.self annotatedWith annotation[TAnn]
    }
  }

  trait ScalaAnnotatedElementBuilder[T] extends AnnotatedElementBuilderProxy[T] {
    def annotatedWith[TAnn <: JAnnotation : ClassManifest] = self annotatedWith annotation[TAnn]
  }
}
