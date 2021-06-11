/*
 *  Copyright 2010-2014 Benjamin Lings
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

import binder._
import com.google.inject.matcher.{Matcher, Matchers}
import com.google.inject.{
  AbstractModule,
  Binder,
  MembersInjector,
  PrivateBinder,
  PrivateModule,
  Scope
}
import java.lang.annotation.Annotation
import java.lang.reflect.AnnotatedElement
import javax.inject.Provider
import org.aopalliance.intercept.MethodInterceptor
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * Allows binding via type parameters. Mix into `AbstractModule`
 * (or subclass) to allow using a type parameter instead of
 * `classOf[Foo]` or `new TypeLiteral[Bar[Foo\]\] {}`.
 *
 * For example, instead of
 * {{{
 * class MyModule extends AbstractModule {
 *   def configure {
 *     bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *     bind(classOf[CreditCardPaymentService])
 *     bind(new TypeLiteral[Bar[Foo]]{}).to(classOf[FooBarImpl])
 *     bind(classOf[PaymentService]).to(classOf[CreditCardPaymentService])
 *
 *     bindInterceptor(Matchers.any(), Matchers.annotatedWith(classOf[AOP]), new AOPI())
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
 *
 *     bindInterceptor[AOPI](methodMatcher = annotatedWith[AOP])
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

  class BindingBuilder[T: TypeTag] extends ScalaAnnotatedBindingBuilder[T] {
    val myBinder = binderAccess
    val self = myBinder.bind(typeLiteral[T])
  }

  protected[this] def binderAccess: B

  protected[this] def bind[T: TypeTag] = new BindingBuilder[T]

  protected[this] def bindInterceptor[I <: MethodInterceptor : ClassTag](classMatcher: Matcher[_ >: Class[_]] = Matchers.any(), methodMatcher: Matcher[_ >: AnnotatedElement]): Unit = {
    val myBinder = binderAccess
    val interceptor = implicitly[ClassTag[I]].runtimeClass.getDeclaredConstructor(Array.empty[Class[_]]: _*).newInstance().asInstanceOf[MethodInterceptor]
    myBinder.requestInjection(interceptor)
    myBinder.bindInterceptor(classMatcher, methodMatcher, interceptor)
  }

  protected[this] def annotatedWith[A <: Annotation : ClassTag]: Matcher[AnnotatedElement] = {
    Matchers.annotatedWith(cls[A])
  }

  protected[this] def bindScope[T <: Annotation : ClassTag](scope: Scope): Unit = binderAccess.bindScope(cls[T], scope)
  protected[this] def requestStaticInjection[T: ClassTag](): Unit = binderAccess.requestStaticInjection(cls[T])
  protected[this] def getProvider[T: ClassTag]: Provider[T] = binderAccess.getProvider(cls[T])
  protected[this] def getMembersInjector[T: TypeTag]: MembersInjector[T] = binderAccess.getMembersInjector(typeLiteral[T])
}

trait ScalaModule extends AbstractModule with InternalModule[Binder] {
  // should be:
  // this: AbstractModule =>
  // see http://lampsvn.epfl.ch/trac/scala/ticket/3564

  import ScalaModule._

  protected[this] def binderAccess: Binder = super.binder.withSource(filterTrace((new Throwable).getStackTrace)) // should not need super
}

trait ScalaPrivateModule extends PrivateModule with InternalModule[PrivateBinder] {
  // should be:
  // this: PrivateModule =>
  // see http://lampsvn.epfl.ch/trac/scala/ticket/3564

  import ScalaModule._

  protected[this] def binderAccess: PrivateBinder = super.binder.withSource(filterTrace((new Throwable).getStackTrace)) // should not need super

  class ElementBuilder[T: TypeTag] extends ScalaAnnotatedElementBuilder[T] {
    val myBinder = binderAccess
    val self = myBinder.expose(typeLiteral[T])
  }

  protected[this] def expose[T: TypeTag] = new ElementBuilder[T]
}

object ScalaModule {
  import java.lang.annotation.{Annotation => JAnnotation}

  def filterTrace(trace:Array[StackTraceElement]):StackTraceElement = {
    import scala.language.existentials //For inner class name retrieval

    trace.find((s) => {
      val complex =
      s.getClassName != classOf[ScalaModule].getName &&
      s.getClassName != classOf[ScalaPrivateModule].getName &&
      s.getClassName != classOf[ScalaPrivateModule#ElementBuilder[_]].getName &&
      s.getClassName != classOf[InternalModule[_]].getName &&
      s.getClassName != classOf[InternalModule[_]#BindingBuilder[_]].getName &&
      s.getClassName != classOf[ScalaModule].getName+"$class" && //Scala <= 2.11 (Ancillary Class for traits)
      s.getClassName != classOf[ScalaPrivateModule].getName+"$class" && //Scala <= 2.11 (Ancillary Class for traits)
      s.getMethodName != "binderAccess" && //Scala <= 2.11 (method on inheriting class)
      s.getMethodName != "bind" && //Scala <= 2.11 (method on inheriting class)
      s.getMethodName != "bindInterceptor" && //Scala <= 2.11 (method on inheriting class)
      s.getMethodName != "expose" //Scala <= 2.11 (method on inheriting class)

      complex
      //For testing purposes
      //val simple = s.toString.contains("configure")
      //assert( complex == simple,  s.getClassName+" :: "+s.getMethodName()+" :: "+s.getFileName()+" == "+classOf[ScalaModule].getName)
      //simple
    }).orNull
  }

  trait ScalaScopedBindingBuilder extends ScopedBindingBuilderProxy {
    def in[TAnn <: JAnnotation : ClassTag](): Unit = self.in(cls[TAnn])
  }

  trait ScalaLinkedBindingBuilder[T] extends ScalaScopedBindingBuilder with LinkedBindingBuilderProxy[T] { outer =>
    def to[TImpl <: T : TypeTag]: ScalaScopedBindingBuilder = new ScalaScopedBindingBuilder {
      val self = outer.self to typeLiteral[TImpl]
    }

    def toProvider[TProvider <: Provider[_ <: T] : TypeTag]: ScalaScopedBindingBuilder = new ScalaScopedBindingBuilder {
      val self = outer.self toProvider typeLiteral[TProvider]
    }
  }

  trait ScalaAnnotatedBindingBuilder[T] extends ScalaLinkedBindingBuilder[T] with AnnotatedBindingBuilderProxy[T] { outer =>
    def annotatedWith[TAnn <: JAnnotation : ClassTag]: ScalaLinkedBindingBuilder[T] = new ScalaLinkedBindingBuilder[T] {
      val self = outer.self annotatedWith cls[TAnn]
    }
  }

  trait ScalaAnnotatedElementBuilder[T] extends AnnotatedElementBuilderProxy[T] {
    def annotatedWith[TAnn <: JAnnotation : ClassTag](): Unit = self annotatedWith cls[TAnn]
  }
}
