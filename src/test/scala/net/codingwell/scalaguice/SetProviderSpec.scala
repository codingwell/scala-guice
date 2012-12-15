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

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

import com.google.inject._

import java.util.{Set => JSet, HashSet => JHashSet}

import scala.collection.{ immutable => im }

class SetProviderSpec extends WordSpec with ShouldMatchers {

  "A Set Provider" should {

    "allow binding a Java Set" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[JSet[B]].toInstance( new JHashSet[B]() )
          bind[im.Set[B]].toProvider( new SetProvider( Key.get( typeLiteral[JSet[B]] ) ) )
        }
      }
      Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[B]] ))
    }

    "allow binding a Java Set with a Java annotation" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[JSet[B]].annotatedWith[Named].toInstance( new JHashSet[B]() )
          bind[im.Set[B]].annotatedWith[Named].toProvider( new SetProvider( Key.get( typeLiteral[JSet[B]], classOf[Named] ) ) )
        }
      }
      Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[B]],classOf[Named]))
    }

  }

}
