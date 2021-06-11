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

import com.google.common.collect.ImmutableSet
import com.google.inject.spi.{Dependency, ProviderWithDependencies}
import com.google.inject.{Inject, Injector, Key}
import java.util.{Map => JMap, Set => JSet}
import scala.collection.{immutable => im}
import scala.jdk.CollectionConverters._

/**
 * Provider for a Scala Immutable Map from a Java Map.
 *
 * Example:
 * {{{
 * .toProvider(new MapProvider[K, V](Key.get(typeLiteral[JMap[K, V]])))
 * }}}
 */
class MapProvider[K, V](source: Key[JMap[K, V]]) extends ProviderWithDependencies[im.Map[K, V]] {
  @Inject() private[this] val injector: Injector = null

  override def get(): im.Map[K, V] = {
    val map = injector.getInstance(source)
    map.asScala.toMap[K, V]
  }

  override def getDependencies: JSet[Dependency[_]] = {
    ImmutableSet.of(Dependency.get(source))
  }
}

/**
 * Provider for a Scala Immutable Map from a Java Map.
 *
 * Example:
 * {{{
 * .toProvider(new MapOfKToSetOfVProvider[K, V](Key.get(typeLiteral[JMap[K, JSet[V]]])))
 * }}}
 */
class MapOfKToSetOfVProvider[K, V](source: Key[JMap[K, JSet[V]]]) extends ProviderWithDependencies[im.Map[K, im.Set[V]]] {
  @Inject() private[this] val injector: Injector = null

  override def get(): im.Map[K, im.Set[V]] = {
    val map = injector.getInstance(source)
    map.asScala.view.mapValues(s => s.asScala.toSet[V]).toMap[K, im.Set[V]]
  }

  override def getDependencies: JSet[Dependency[_]] = {
    ImmutableSet.of(Dependency.get(source))
  }
}
