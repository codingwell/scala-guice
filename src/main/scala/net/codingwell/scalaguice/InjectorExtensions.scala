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

import com.google.inject.{Binding, Injector, Key, Provider}
import java.lang.annotation.Annotation
import net.codingwell.scalaguice.KeyExtensions._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object InjectorExtensions {

  implicit class ScalaInjector(val self: Injector) extends AnyVal {
    def instance[T: TypeTag]: T = self.getInstance(typeLiteral[T].toKey)
    def instance[T: TypeTag](ann: Annotation): T = self.getInstance(typeLiteral[T].annotatedWith(ann))
    def instance[T: TypeTag, Ann <: Annotation : ClassTag]: T = self.getInstance(typeLiteral[T].annotatedWith[Ann])

    def existingBinding[T: TypeTag]: Option[Binding[T]] = existingBinding(typeLiteral[T].toKey)
    def existingBinding[T: TypeTag](ann: Annotation): Option[Binding[T]] = existingBinding(typeLiteral[T].annotatedWith(ann))
    def existingBinding[T: TypeTag, Ann <: Annotation : ClassTag]: Option[Binding[T]] = existingBinding(typeLiteral[T].annotatedWith[Ann])
    def existingBinding[T](key: Key[T]): Option[Binding[T]] = Option(self.getExistingBinding(key))

    def provider[T: TypeTag]: Provider[T] = self.getProvider(typeLiteral[T].toKey)
    def provider[T: TypeTag](ann: Annotation): Provider[T] = self.getProvider(typeLiteral[T].annotatedWith(ann))
    def provider[T: TypeTag, Ann <: Annotation : ClassTag]: Provider[T] = self.getProvider(typeLiteral[T].annotatedWith[Ann])
  }
}
