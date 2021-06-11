/*
 *  Copyright 2010-2014 Benjamin Lings
 *  Author: Thomas Suckow
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

import com.google.inject.multibindings.Multibinder
import com.google.inject.{Binder, Key, Module, TypeLiteral}
import java.lang.annotation.Annotation
import java.util.{Set => JSet}
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder
import scala.collection.{immutable => im}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Analog to Guice's Multibinder
 *
 * Use ScalaMultibinder.newSetBinder to create a multibinder that is scala friendly.
 */
sealed trait ScalaMultibinder[T] {
  /**
   * Configures the bound set to silently discard duplicate elements. When multiple equal values are
   * bound, the one that gets included is arbitrary. When multiple modules contribute elements to
   * the set, this configuration option impacts all of them.
   *
   * @return this multibinder
   * @since 3.0
   */
  def permitDuplicates(): ScalaMultibinder[T]

  /**
   * Returns a binding builder used to add a new element in the set. Each
   * bound element must have a distinct value. Bound providers will be
   * evaluated each time the set is injected.
   *
   * <p>It is an error to call this method without also calling one of the
   * `to` methods on the returned binding builder.
   *
   * <p>Scoping elements independently is supported. Use the `in` method
   * to specify a binding scope.
   */
  def addBinding: ScalaLinkedBindingBuilder[T]
}

object ScalaMultibinder {
  /** Preferred Scala Methods */

  /**
   * Returns a new multibinder that collects instances of type `T` in a [[scala.collection.immutable.Set]] that is
   * itself bound with no binding annotation.
   */
  def newSetBinder[T: TypeTag](binder: Binder): ScalaMultibinder[T] = {
    newMultibinder(binder, typeLiteral[T])
  }

  /**
   * Returns a new multibinder that collects instances of type `T` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation `Ann`.
   */
  def newSetBinder[T: TypeTag, Ann <: Annotation : ClassTag](binder: Binder): ScalaMultibinder[T] = {
    newMultibinder[T, Ann](binder, typeLiteral[T], cls[Ann])
  }

  /**
   * Returns a new multibinder that collects instances of type `T` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation.
   */
  def newSetBinder[T: TypeTag](binder: Binder, annotation: Annotation): ScalaMultibinder[T] = {
    newMultibinder(binder, typeLiteral[T], annotation)
  }

  /** Methods Compatible w/Guice's API */

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with no binding annotation.
   */
  def newSetBinder[T](binder: Binder, typ: TypeLiteral[T]): ScalaMultibinder[T] = {
    newMultibinder(binder, typ)
  }

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with no binding annotation. Note that `typ` is ignored in favor of using the `T` TypeTag to capture
   * type arguments.
   */
  def newSetBinder[T: TypeTag](binder: Binder, typ: Class[T]): ScalaMultibinder[T] = {
    newMultibinder(binder, typeLiteral[T])
  }

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation.
   */
  def newSetBinder[T: TypeTag](binder: Binder, typ: TypeLiteral[T], annotation: Annotation): ScalaMultibinder[T] = {
    newMultibinder(binder, typ, annotation)
  }

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation. Note that `typ` is ignored in favor of using the TypeTag to capture
   * type arguments.
   */
  def newSetBinder[T: TypeTag](binder: Binder, typ: Class[T], annotation: Annotation): ScalaMultibinder[T] = {
    newMultibinder(binder, typeLiteral[T], annotation)
  }

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation.
   */
  def newSetBinder[T](binder: Binder, typ: TypeLiteral[T], annotation: Class[_ <: Annotation]): ScalaMultibinder[T] = {
    newMultibinder(binder, typ, annotation)
  }

  /**
   * Returns a new multibinder that collects instances of `typ` in a [[scala.collection.immutable.Set]] that is
   * itself bound with a binding annotation. Note that `typ` is ignored in favor of using the TypeTag to capture
   * type arguments.
   */
  def newSetBinder[T: TypeTag](binder: Binder, typ: Class[T], annotation: Class[_ <: Annotation]): ScalaMultibinder[T] = {
    newMultibinder(binder, typeLiteral[T], annotation)
  }

  /** Implementation Details */

  private def newMultibinder[T](parentBinder: Binder, typ: TypeLiteral[T]): ScalaMultibinder[T] = {
    val binder = skipSources(parentBinder)
    val jMultibinder = Multibinder.newSetBinder(binder, typ)
    newMultibinder(binder, jMultibinder, Key.get(typ))
  }

  private def newMultibinder[T](parentBinder: Binder, typ: TypeLiteral[T], annotation: Annotation): ScalaMultibinder[T] = {
    val binder = skipSources(parentBinder)
    val jMultibinder = Multibinder.newSetBinder(binder, typ, annotation)
    newMultibinder(binder, jMultibinder, Key.get(typ, annotation))
  }

  private def newMultibinder[T, Ann <: Annotation](parentBinder: Binder, typ: TypeLiteral[T], annotationType: Class[Ann]): ScalaMultibinder[T] = {
    val binder = skipSources(parentBinder)
    val jMultibinder = Multibinder.newSetBinder(binder, typ, annotationType)
    newMultibinder(binder, jMultibinder, Key.get(typ, annotationType))
  }

  private def newMultibinder[T](binder: Binder, parent: Multibinder[T], key: Key[T]): ScalaMultibinder[T] = {
    val result = new RealScalaMultibinder[T](parent, key)
    binder.install(result)
    result
  }

  private def skipSources(binder: Binder): Binder = {
    binder.skipSources(
      ScalaMultibinder.getClass,
      classOf[ScalaMultibinder[_]],
      classOf[RealScalaMultibinder[_]]
    )
  }

  /**
   * Analog to the Guice's [[com.google.inject.multibindings.Multibinder.RealMultibinder]]
   *
   * As a Module, the [[RealScalaMultibinder]] installs the binding to the set itself. As a module, this implements
   * `equals()` and `hashCode()` in order to trick Guice into executing its `configure` method only once. That makes
   * it so that multiple binders can be created for the same target collection, but only one is bound. The binding maps
   * the [[java.util.Set]] to a [[im.Set]] for useful Scala injection.
   */
  private class RealScalaMultibinder[T](parent: Multibinder[T], key: Key[T]) extends ScalaMultibinder[T] with Module {
    private val setKey = key.ofType(wrap[im.Set].around(key.getTypeLiteral))
    private[this] val setName = nameOf(setKey)

    def addBinding: ScalaLinkedBindingBuilder[T] = new ScalaLinkedBindingBuilder[T] {
      val self = parent.addBinding()
    }

    def permitDuplicates(): ScalaMultibinder[T] = {
      parent.permitDuplicates
      this
    }

    def getJavaMultibinder: Multibinder[T] = {
      parent
    }

    def configure(binder: Binder): Unit = {
      binder.bind(setKey).toProvider(new SetProvider(key.ofType(wrap[JSet].around(key.getTypeLiteral))))
    }

    /** Trick Guice into installing this Module once; be careful to not use the jSetKey. */
    override def equals(o: Any): Boolean = o match {
      case o: RealScalaMultibinder[_] => o.setKey == setKey
      case _ => false
    }

    override def hashCode: Int = {
      setKey.hashCode
    }

    override def toString: String = {
      (if (setName.isEmpty) "" else setName + " ") + "ScalaMultibinder<" + key.getTypeLiteral + ">"
    }
  }
}
