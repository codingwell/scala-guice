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

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Guice, Key}
import java.util.{Map => JMap, Set => JSet}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.{immutable => im}

class MapProviderSpec extends AnyWordSpec with Matchers {
  private val testMap = newMap("1" -> 1, "2" -> 2)
  private val testMapToSet = newMap("1" -> newSet(1, 3), "2" -> newSet(2, 4))

  private def validate(map: im.Map[String, Int]): Unit = {
    map should have size 2
    map("1") should equal(1)
    map("2") should equal(2)
  }

  private def validateWithSet(map: im.Map[String, im.Set[Int]]): Unit = {
    map should have size 2

    val v1 = map("1")
    v1 should have size 2
    v1 should contain(1)
    v1 should contain(3)

    val v2 = map("2")
    v2 should have size 2
    v2 should contain(2)
    v2 should contain(4)
  }

  "A Map Provider" should {
    "allow binding a JMap[K, V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, Int]].toInstance(testMap)
          bind[im.Map[String, Int]].toProvider(new MapProvider(Key.get(typeLiteral[JMap[String, Int]])))
        }
      }
      validate(Guice.createInjector(module).instance[im.Map[String, Int]])
    }

    "allow binding a JMap[K, V] with an annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, Int]].annotatedWith[Named].toInstance(testMap)
          val provider = new MapProvider(Key.get(typeLiteral[JMap[String, Int]], classOf[Named]))
          bind[im.Map[String, Int]].annotatedWith[Named].toProvider(provider)
        }
      }
      validate(Guice.createInjector(module).instance[im.Map[String, Int], Named])
    }

    "allow binding a JMap[K, JSet[V]]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, JSet[Int]]].toInstance(testMapToSet)
          val provider = new MapOfKToSetOfVProvider(Key.get(typeLiteral[JMap[String, JSet[Int]]]))
          bind[im.Map[String, im.Set[Int]]].toProvider(provider)
        }
      }
      validateWithSet(Guice.createInjector(module).instance[im.Map[String, im.Set[Int]]])
    }

    "allow binding a JMap[K, JSet[V]] with an annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, JSet[Int]]].annotatedWith[Named].toInstance(testMapToSet)
          val provider = new MapOfKToSetOfVProvider(Key.get(typeLiteral[JMap[String, JSet[Int]]], classOf[Named]))
          bind[im.Map[String, im.Set[Int]]].annotatedWith[Named].toProvider(provider)
        }
      }
      validateWithSet(Guice.createInjector(module).instance[im.Map[String, im.Set[Int]], Named])
    }

    "allow binding an empty JMap[K, V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, Int]].toInstance(newMap())
          bind[im.Map[String, Int]].toProvider(new MapProvider(Key.get(typeLiteral[JMap[String, Int]])))
        }
      }
      Guice.createInjector(module).instance[im.Map[String, Int]] should be (Symbol("empty"))
    }

    "allow binding an empty JMap[K, JSet[V]]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[JMap[String, JSet[Int]]].toInstance(newMap())
          val provider = new MapOfKToSetOfVProvider(Key.get(typeLiteral[JMap[String, JSet[Int]]]))
          bind[im.Map[String, im.Set[Int]]].toProvider(provider)
        }
      }
      Guice.createInjector(module).instance[im.Map[String, im.Set[Int]]] should be (Symbol("empty"))
    }
  }

  private def newMap[K, V](elems: (K, V)*): JMap[K, V] = {
    val result = new java.util.HashMap[K, V]()
    for ((k, v) <- elems) {
      result.put(k, v)
    }
    result
  }

  private def newSet[T](elems: T*): JSet[T] = {
    val result = new java.util.HashSet[T]()
    for (t <- elems) {
      result.add(t)
    }
    result
  }
}
