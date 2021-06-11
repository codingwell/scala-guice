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

import com.google.inject.multibindings.MapBinder
import com.google.inject.{Binder, Key, Module, Provider, TypeLiteral}
import java.lang.annotation.Annotation
import java.util.{Map => JMap, Set => JSet}
import net.codingwell.scalaguice.ScalaModule.ScalaLinkedBindingBuilder
import scala.collection.{immutable => im}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

/**
 * Analog to Guice's MapBinder
 *
 * Use ScalaMapBinder.newMapBinder to create a map binder that is scala friendly.
 *
 * @tparam K The key type for the bound map
 * @tparam V The value type for the bound map
 */
trait ScalaMapBinder[K, V] {
  /**
   * Configures the [[ScalaMapBinder]] to handle duplicate entries.
   *
   * <p>When multiple equal keys are bound, the value that gets included in the map is
   * arbitrary.
   *
   * <p>In addition to the im.Map[K,V] and im.Map[K,Provider[V]]
   * maps that are normally bound, a im.Set[K,Set[V]] and im.Map[K,im.Set[Provider[V]]]
   * are <em>also</em> bound, which contain all values bound to each key.
   *
   * <p> When multiple modules contribute elements to the map, this configuration
   * option impacts all of them.
   */
  def permitDuplicates(): ScalaMapBinder[K, V]

  /**
   * Returns a binding builder used to add a new entry in the map. Each
   * key must be distinct (and non-null). Bound providers will be evaluated each
   * time the map is injected.
   *
   * <p>It is an error to call this method without also calling one of the
   * `to` methods on the returned binding builder.
   *
   * <p>Scoping elements independently is supported. Use the `in` method
   * to specify a binding scope.
   */
  def addBinding(key: K): ScalaLinkedBindingBuilder[V]
}

object ScalaMapBinder {
  /** Preferred Scala Methods */

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with no binding annotation.
   */
  def newMapBinder[K: TypeTag, V: TypeTag](binder: Binder): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V])
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `annotation`.
   */
  def newMapBinder[K: TypeTag, V: TypeTag](binder: Binder, annotation: Annotation): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V], annotation)
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `Ann`
   */
  def newMapBinder[K: TypeTag, V: TypeTag, Ann <: Annotation : ClassTag](binder: Binder): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V], cls[Ann])
  }

  /** Guice's MapBinder API */

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with no binding annotation.
   */
  def newMapBinder[K, V](parentBinder: Binder, kTyp: TypeLiteral[K], vTyp: TypeLiteral[V]): ScalaMapBinder[K, V] = {
    val binder = skipSources(parentBinder)
    val parent = MapBinder.newMapBinder(binder, kTyp, vTyp)
    newMapBinder(binder, parent, kTyp, vTyp, Key.get(this.getClass))
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with no binding annotation. Note that
   * `kTyp` and `vTyp` are ignored in favor of using the TypeTag to capture type arguments.
   */
  def newMapBinder[K: TypeTag, V: TypeTag](binder: Binder, kTyp: Class[K], vTyp: Class[V]): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V])
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `annotation`.
   */
  def newMapBinder[K, V](parentBinder: Binder, kTyp: TypeLiteral[K], vTyp: TypeLiteral[V],
                         annotation: Annotation): ScalaMapBinder[K, V] = {
    val binder = skipSources(parentBinder)
    val annotatedKey = Key.get(this.getClass, annotation)
    val parent = MapBinder.newMapBinder(binder, kTyp, vTyp, annotation)
    newMapBinder(binder, parent, kTyp, vTyp, annotatedKey)
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `annotation`. Note that
   * `kTyp` and `vTyp` are ignored in favor of using the TypeTag to capture type arguments.
   */
  def newMapBinder[K: TypeTag, V: TypeTag](binder: Binder, kTyp: Class[K], vTyp: Class[V],
                                             annotation: Annotation): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V], annotation)
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `annotationType`.
   */
  def newMapBinder[K, V](parentBinder: Binder, kTyp: TypeLiteral[K], vTyp: TypeLiteral[V],
                         annotationType: Class[_ <: Annotation]): ScalaMapBinder[K, V] = {
    val binder = skipSources(parentBinder)
    val annotatedKey = Key.get(this.getClass, annotationType)
    val parent = MapBinder.newMapBinder(binder, kTyp, vTyp, annotationType)
    newMapBinder(binder, parent, kTyp, vTyp, annotatedKey)
  }

  /**
   * Returns a new mapbinder that collects entries of `K`/`V` in a
   * [[scala.collection.immutable.Map]] that is itself bound with `annotationType`. Note that
   * `kTyp` and `vTyp` are ignored in favor of using the TypeTag to capture type arguments.
   */
  def newMapBinder[K: TypeTag, V: TypeTag](binder: Binder, kTyp: Class[K], vTyp: Class[V],
                                             annotationType: Class[_ <: Annotation]): ScalaMapBinder[K, V] = {
    newMapBinder(binder, typeLiteral[K], typeLiteral[V], annotationType)
  }

  /** Implementation details. */

  private def newMapBinder[K, V](binder: Binder, parent: MapBinder[K, V], kTyp: TypeLiteral[K], vTyp: TypeLiteral[V],
                                 annotatedKey: Key[_]): ScalaMapBinder[K, V] = {
    val result = new RealScalaMapBinder[K, V](parent, kTyp, vTyp, annotatedKey)
    binder.install(result)
    result
  }

  private def skipSources(parentBinder: Binder): Binder = {
    parentBinder.skipSources(
      ScalaMapBinder.getClass,
      classOf[ScalaMapBinder[_, _]],
      classOf[RealScalaMapBinder[_, _]]
    )
  }

  /**
   * Analog to the Guice's [[com.google.inject.multibindings.MapBinder.RealMapBinder]]
   *
   * As a Module, the [[RealScalaMapBinder]] installs the binding to the map itself. As a module, this implements
   * `equals()` and `hashCode()` in order to trick Guice into executing its `configure` method only once. That makes
   * it so that multiple binders can be created for the same target map, but only one is bound. The binding maps
   * the [[java.util.Map]] to a [[scala.collection.immutable.Map]] for useful Scala injection.
   */
  private class RealScalaMapBinder[K, V](parent: MapBinder[K, V], kTyp: TypeLiteral[K], vTyp: TypeLiteral[V],
                                         annotatedKey: Key[_]) extends ScalaMapBinder[K, V] with Module {
    val mapKey: Key[Map[K, V]] = annotatedKey.ofType(wrap2[im.Map].around(kTyp, vTyp))
    private[this] val mapName = nameOf(mapKey)
    private def scalaMapKey: Key[im.Map[K, V]] = mapKey

    def permitDuplicates(): ScalaMapBinder[K, V] = {
      parent.permitDuplicates()
      this
    }

    def addBinding(key: K): ScalaLinkedBindingBuilder[V] = new ScalaLinkedBindingBuilder[V] {
      val self = parent.addBinding(key)
    }

    def getJavaMapBinder: MapBinder[K, V] = {
      parent
    }

    def configure(binder: Binder): Unit = {
      bindMapping(binder, vTyp)
      bindMapping(binder, wrap[Provider].around(vTyp))
      bindMapping(binder, wrap[javax.inject.Provider].around(vTyp))
    }

    private[this] def bindMapping[T](binder: Binder, typ: TypeLiteral[T]): Unit = {
      // Bind singleton use-case.
      val sKey = annotatedKey.ofType(wrap2[im.Map].around(kTyp, typ))
      val jKey = annotatedKey.ofType(wrap2[JMap].around(kTyp, typ))
      binder.bind(sKey).toProvider(new MapProvider(jKey))

      // Bind multimap use-case.
      val sSetKey = annotatedKey.ofType(wrap2[im.Map].around(kTyp, wrap[im.Set].around(typ)))
      val jSetKey = annotatedKey.ofType(wrap2[JMap].around(kTyp, wrap[JSet].around(typ)))
      binder.bind(sSetKey).toProvider(new MapOfKToSetOfVProvider(jSetKey))
    }

    /** Trick Guice into installing this Module once; be careful to not use the jSetKey. */
    override def equals(o: Any): Boolean = o match {
      case o: RealScalaMapBinder[_, _] => o.scalaMapKey == scalaMapKey
      case _ => false
    }

    override def hashCode: Int = {
      scalaMapKey.hashCode
    }

    override def toString: String = {
      (if (mapName.isEmpty) "" else mapName + " ") + "ScalaMapBinder<" + kTyp + "," + vTyp + ">"
    }
  }
}
