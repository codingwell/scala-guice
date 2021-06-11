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

import com.google.inject._
import java.util.{HashSet => JHashSet, Set => JSet}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.{immutable => im}

class SetProviderSpec extends AnyWordSpec with Matchers {

  private val testSet = newSet(1, 3)

  "A Set Provider" should {

    "allow binding a Java Set" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JSet[B]].toInstance( new JHashSet[B]() )
          bind[im.Set[B]].toProvider( new SetProvider( Key.get( typeLiteral[JSet[B]] ) ) )
        }
      }
      Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[B]] )) should be (Symbol("empty"))
    }

    "allow binding a Java Set with a Java annotation" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JSet[B]].annotatedWith[Named].toInstance( new JHashSet[B]() )
          bind[im.Set[B]].annotatedWith[Named].toProvider( new SetProvider( Key.get( typeLiteral[JSet[B]], classOf[Named] ) ) )
        }
      }
      Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[B]],classOf[Named])) should be (Symbol("empty"))
    }

    "allow binding a Java Set with data" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JSet[Int]].toInstance( testSet )
          bind[im.Set[Int]].toProvider( new SetProvider( Key.get( typeLiteral[JSet[Int]] ) ) )
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[Int]] ))
      set should have size 2
      set should contain (1)
      set should contain (3)
    }
  }

  private def newSet[T](elems: T*): JSet[T] = {
    val result = new java.util.HashSet[T]()
    for (t <- elems) {
      result.add(t)
    }
    result
  }
}
