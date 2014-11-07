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

import java.util

import com.google.common.base.Optional
import com.google.common.collect.ImmutableSet
import com.google.inject.{Inject, Injector, Key}
import com.google.inject.spi.{Dependency, ProviderWithDependencies}

/**
 * Provider for Scala's Option from Guava's Optional.
 *
 * Example:
 * {{{
 * .toProvider(new OptionProvider[T](Key.get(typeLiteral[Optional[T]])))
 * }}}
 */
class OptionProvider[T] (source: Key[Optional[T]]) extends ProviderWithDependencies[Option[T]] {
  @Inject() private[this] val injector: Injector = null

  override def get(): Option[T] = {
    val opt = injector.getInstance(source)
    if (opt.isPresent) Some(opt.get) else None
  }

  override def getDependencies: util.Set[Dependency[_]] = {
    ImmutableSet.of(Dependency.get(source))
  }
}
