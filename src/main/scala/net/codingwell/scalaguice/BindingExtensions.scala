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

import com.google.inject.Binder
import com.google.inject.binder._
import com.google.inject.name.Names
import java.lang.annotation.{Annotation => JAnnotation}
import javax.inject.Provider
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Extensions for Guice's binding DSL.
 * These allow using a type parameter instead of `classOf[Foo]`
 * or `new TypeLiteral[Bar[Foo]] {}`. The extra methods are
 * named as those in the normal binding DSL suffixed with `Type`.
 *
 * For example, instead of
 * {{{
 * binder.bind(new TypeLiteral[Bar[Foo]]{}).to(classOf[FooBarImpl])
 * }}}
 * use
 * {{{
 * import BindingExtensions._
 * binder.bindType[Bar[Foo]].toType[FooImpl]
 * }}}
 *
 * '''Note''' This syntax allows binding to and from generic types.
 * It doesn't currently allow bindings between wildcard types because the
 * manifests for wildcard types don't provide access to type bounds.
 */
object BindingExtensions {

  implicit class ScalaBinder(val self: Binder) extends AnyVal {
    def bindType[T: TypeTag]: AnnotatedBindingBuilder[T] = self.bind(typeLiteral[T])
  }

  implicit class ScalaScopedBindingBuilder(val self: ScopedBindingBuilder) extends AnyVal {
    def inType[TAnn <: JAnnotation : ClassTag](): Unit = self.in(cls[TAnn])
  }

  implicit class ScalaLinkedBindingBuilder[T](val self: LinkedBindingBuilder[T]) extends AnyVal {
    def toType[TImpl <: T : TypeTag]: ScopedBindingBuilder = self.to(typeLiteral[TImpl])

    def toProviderType[TProvider <: Provider[_ <: T] : ClassTag]: ScopedBindingBuilder = self.toProvider(cls[TProvider])
  }

  implicit class ScalaAnnotatedBindingBuilder[T](val self: AnnotatedBindingBuilder[T]) extends AnyVal {
    def annotatedWithType[TAnn <: JAnnotation : ClassTag]: LinkedBindingBuilder[T] = self.annotatedWith(cls[TAnn])
  }

  implicit class ScalaAnnotatedConstantBindingBuilder(val self: AnnotatedConstantBindingBuilder) extends AnyVal {
    def annotatedWithType[TAnn <: JAnnotation : ClassTag]: ConstantBindingBuilder = self.annotatedWith(cls[TAnn])
    def annotatedWithName(name: String): ConstantBindingBuilder = self.annotatedWith(Names.named(name))
  }

  implicit class ScalaConstantBindingBuilder(val self: ConstantBindingBuilder) extends AnyVal {
    def to[T: ClassTag](): Unit = self.to(cls[T])
  }
}

