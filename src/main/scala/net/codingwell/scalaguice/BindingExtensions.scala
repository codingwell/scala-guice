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

/**
 * Extensions for Guice's binding DSL.
 * These allow using a type parameter instead of <code>classOf[Foo]}</code>
 * or <code>new TypeLiteral[Bar[Foo]] {}</code>. The extra methods are
 * named as those in the normal binding DSL suffixed with <code>Type</code>.
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

    class ScalaBinder(b:Binder) {
        def bindType[T : Manifest] = b bind typeLiteral[T]
    }

    implicit def enrichBinder(b:Binder) = new ScalaBinder(b)

    import com.google.inject.binder._
    import java.lang.annotation.{Annotation => JAnnotation}

    class ScalaScopedBindingBuilder(b: ScopedBindingBuilder) {
        def inType[TAnn <: JAnnotation : ClassManifest] = b in annotation[TAnn]
    }

    implicit def enrichScopedBindingBuilder(b: ScopedBindingBuilder) = new ScalaScopedBindingBuilder(b)

    class ScalaLinkedBindingBuilder[T](b: LinkedBindingBuilder[T]) {
        def toType[TImpl <: T : Manifest] = b to typeLiteral[TImpl]
        def toProviderType[TProvider <: Provider[_ <: T] : ClassManifest] = b toProvider classManifest[TProvider].erasure.asInstanceOf[Class[TProvider]]
    }

    implicit def enrichLinkedBinding[T](b: LinkedBindingBuilder[T]) =
      new ScalaLinkedBindingBuilder[T](b)

    class ScalaAnnotatedBindingBuilder[T](b: AnnotatedBindingBuilder[T]) {
        def annotatedWithType[TAnn <: JAnnotation : ClassManifest] =
          b annotatedWith annotation[TAnn]
    }

    implicit def enrichAnnotatedBinding[T](b: AnnotatedBindingBuilder[T]) =
      new ScalaAnnotatedBindingBuilder[T](b)

    class ScalaAnnotatedConstantBindingBuilder(b: AnnotatedConstantBindingBuilder) {
        def annotatedWithType[TAnn <: JAnnotation : ClassManifest] =
          b annotatedWith annotation[TAnn]
    }

    implicit def enrichAnnotatedConstantBindingBuilder(b: AnnotatedConstantBindingBuilder) =
      new ScalaAnnotatedConstantBindingBuilder(b)

    class ScalaConstantBindingBuilder(b: ConstantBindingBuilder) {
        def toType[T: ClassManifest] = b to classManifest[T].erasure
    }

    implicit def enrichConstantBinding(b: ConstantBindingBuilder) =
      new ScalaConstantBindingBuilder(b)

}

