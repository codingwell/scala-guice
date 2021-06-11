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

import com.google.common.base.Optional
import com.google.inject.multibindings.OptionalBinder
import com.google.inject.{Binder, Key, Module, Provider, TypeLiteral}
import java.lang.annotation.Annotation
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Analog to Guice's OptionalBinder
 *
 * Use ScalaOptionBinder.newOptionBinder to create an option binder that is scala friendly.
 */
trait ScalaOptionBinder[T] {
  /**
   * Returns a binding builder used to set the default value that will be injected.
   * The binding set by this method will be ignored if `setBinding` is called.
   *
   * <p>It is an error to call this method without also calling one of the `to`
   * methods on the returned binding builder.
   */
  def setDefault: ScalaLinkedBindingBuilder[T]

  /**
   * Returns a binding builder used to set the actual value that will be injected.
   * This overrides any binding set by `setDefault`.
   *
   * <p>It is an error to call this method without also calling one of the `to`
   * methods on the returned binding builder.
   */
  def setBinding: ScalaLinkedBindingBuilder[T]
}

object ScalaOptionBinder {
  /** Preferred Scala Methods */

  /**
   * Returns a new optionbinder that binds an instance of `T` in a [[scala.Option]].
   */
  def newOptionBinder[T: TypeTag](binder: Binder): ScalaOptionBinder[T] = {
    newOptionBinder(binder, typeLiteral[T])
  }

  /**
   * Returns a new optionbinder that binds an instance of `T` in a [[scala.Option]] that is
   * itself bound with a binding annotation `Ann`.
   */
  def newOptionBinder[T: TypeTag, Ann <: Annotation : ClassTag](binder: Binder): ScalaOptionBinder[T] = {
    newOptionBinder(binder, Key.get(typeLiteral[T], cls[Ann]))
  }

  /**
   * Returns a new optionbinder that binds an instance of `T` in a [[scala.Option]] that is
   * itself bound with a binding annotation.
   */
  def newOptionBinder[T: TypeTag](binder: Binder, annotation: Annotation): ScalaOptionBinder[T] = {
    newOptionBinder(binder, Key.get(typeLiteral[T], annotation))
  }

  /**
   * Returns a new optionbinder that binds `typ` in a [[scala.Option]]. Note that
   * `typ` is ignored in favor of using the TypeTag to capture type arguments.
   */
  def newOptionBinder[T: TypeTag](binder: Binder, typ: Class[T]): ScalaOptionBinder[T] = {
    newOptionBinder(binder, typeLiteral[T])
  }

  /**
   * Returns a new optionbinder that binds an instance of the type represented by `typeLiteral` in a [[scala.Option]].
   */
  def newOptionBinder[T](binder: Binder, typeLiteral: TypeLiteral[T]): ScalaOptionBinder[T] = {
    newOptionBinder(binder, Key.get(typeLiteral))
  }

  /**
   * Returns a new optionbinder that binds an instance of the type represented by `key` in a [[scala.Option]]. It may or
   * may not be bound with a binding annotation (depending on the key).
   */
  def newOptionBinder[T](parentBinder: Binder, key: Key[T]): ScalaOptionBinder[T] = {
    val binder = parentBinder.skipSources(
      ScalaOptionBinder.getClass,
      classOf[ScalaOptionBinder[T]],
      classOf[RealScalaOptionBinder[T]]
    )
    val jOptionalBinder = OptionalBinder.newOptionalBinder(binder, key)
    val result = new RealScalaOptionBinder[T](jOptionalBinder, key)
    binder.install(result)
    result
  }

  /** Guice's Optional Binder API */

  /**
   * Returns a new optionbinder that binds `typ` in a [[scala.Option]]. Note that
   * `typ` is ignored in favor of using the TypeTag to capture type arguments.
   */
  def newOptionalBinder[T: TypeTag](binder: Binder, typ: Class[T]): ScalaOptionBinder[T] = {
    newOptionBinder(binder, typ)
  }

  /**
   * Returns a new optionbinder that binds an instance of the type represented by `typeLiteral` in a [[scala.Option]].
   */
  def newOptionalBinder[T](binder: Binder, typeLiteral: TypeLiteral[T]): ScalaOptionBinder[T] = {
    newOptionBinder(binder, typeLiteral)
  }

  /**
   * Returns a new optionbinder that binds an instance of the type represented by `key` in a [[scala.Option]]. It may or
   * may not be bound with a binding annotation (depending on the key).
   */
  def newOptionalBinder[T](parentBinder: Binder, key: Key[T]): ScalaOptionBinder[T] = {
    newOptionBinder(parentBinder, key)
  }

  /**
   * Analog to the Guice's [[com.google.inject.multibindings.OptionalBinder.RealOptionalBinder]]
   *
   * As a Module, the [[RealScalaOptionBinder]] installs the binding to the option itself. As a module, this implements
   * `equals()` and `hashCode()` in order to trick Guice into executing its `configure` method only once. That makes
   * it so that multiple binders can be created for the same target option, but only one is bound. The binding maps
   * the [[com.google.common.base.Optional]] to a [[scala.Option]] for useful Scala injection.
   */
  private class RealScalaOptionBinder[T](parent: OptionalBinder[T], key: Key[T]) extends ScalaOptionBinder[T] with Module {
    private[this] val optName = nameOf(key)
    private val optKey = key.ofType(wrap[Option].around(key.getTypeLiteral))

    def setDefault: ScalaLinkedBindingBuilder[T] = new ScalaLinkedBindingBuilder[T] {
      val self = parent.setDefault()
    }

    def setBinding: ScalaLinkedBindingBuilder[T] = new ScalaLinkedBindingBuilder[T] {
      val self = parent.setBinding()
    }

    def getJavaOptionalBinder: OptionalBinder[T] = {
      parent
    }

    def configure(binder: Binder): Unit = {
      bindMapping(binder, key.getTypeLiteral)
      bindMapping(binder, wrap[Provider].around(key.getTypeLiteral))
      bindMapping(binder, wrap[javax.inject.Provider].around(key.getTypeLiteral))
    }

    private[this] def bindMapping[S](binder: Binder, typ: TypeLiteral[S]): Unit = {
      val sKey = key.ofType(wrap[Option].around(typ))
      val jKey = key.ofType(wrap[Optional].around(typ))
      binder.bind(sKey).toProvider(new OptionProvider(jKey))
    }

    /** Trick Guice into installing this Module once. */
    override def equals(o: Any): Boolean = o match {
      case o: RealScalaOptionBinder[_] => o.optKey == optKey
      case _ => false
    }

    override def hashCode: Int = {
      optKey.hashCode
    }

    override def toString: String = {
      (if (optName.isEmpty) "" else optName + " ") + "ScalaOptionBinder<" + key.getTypeLiteral + ">"
    }
  }
}
