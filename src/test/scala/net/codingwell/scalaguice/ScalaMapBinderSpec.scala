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

import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, CreationException, Guice, Module, Provider}
import java.lang.annotation.Annotation
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.{immutable => im}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class ScalaMapBinderSpec extends AnyWordSpec with Matchers {
  private case class W[T](t: T)
  private val annotation = Names.named("N")

  "A MapBinder" should {
    /** New Scala Methods, Happy Path */

    "bind empty [K,V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
        }
      }
      validate[String,Int](module)
    }

    "bind [K,V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validate(module, "1" -> 1, "2" -> 2)
    }

    "bind [K,V](annotation)" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder, annotation)
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnnotation(module, annotation, "1" -> 1, "2" -> 2)
    }

    "bind [K,V,Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int, Named](binder)
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnn[String, Int, Named](module, "1" -> 1, "2" -> 2)
    }

    /** Guice's MapBinder API, Happy Path */

    "bind typeLiteral[K], typeLiteral[V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[String], typeLiteral[Int])
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validate(module, "1" -> 1, "2" -> 2)
    }

    "bind class[K], class[V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, classOf[String], classOf[Int])
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validate(module, "1" -> 1, "2" -> 2)
    }

    "bind typeLiteral[K], typeLiteral[V], annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[String], typeLiteral[Int], annotation)
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnnotation[String, Int](module, annotation, "1" -> 1, "2" -> 2)
    }

    "bind class[K], class[V], annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, classOf[String], classOf[Int], annotation)
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnnotation(module, annotation, "1" -> 1, "2" -> 2)
    }

    "bind typeLiteral[K], typeLiteral[V], class[Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[String], typeLiteral[Int], classOf[Named])
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnn[String, Int, Named](module, "1" -> 1, "2" -> 2)
    }

    "bind class[K], class[V], class[Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder(binder, classOf[String], classOf[Int], classOf[Named])
          mBinder.addBinding("1").toInstance(1)
          mBinder.addBinding("2").toInstance(2)
        }
      }
      validateWithAnn[String, Int, Named](module, "1" -> 1, "2" -> 2)
    }

    /** New Scala Methods, Parameterized Wrappers */

    "bind deep parameterization in [K,V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder[W[String], W[Int]](binder)
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder[W[String], W[String]](binder)
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validate(module, W("1") -> W(1), W("2") -> W(2))
      validate(module, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in [K,V](annotation)" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder[W[String], W[Int]](binder, annotation)
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder[W[String], W[String]](binder, annotation)
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnnotation(module, annotation, W("1") -> W(1), W("2") -> W(2))
      validateWithAnnotation(module, annotation, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in [K,V,Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder[W[String], W[Int], Named](binder)
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder[W[String], W[String], Named](binder)
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnn[W[String], W[Int], Named](module, W("1") -> W(1), W("2") -> W(2))
      validateWithAnn[W[String], W[String], Named](module, W("1") -> W("1"), W("2") -> W("2"))
    }

    /** Guice's MapBinder API, Parameterized Wrappers */

    "bind deep parameterization in typeLiteral[K], typeLiteral[V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[Int]])
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[String]])
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validate(module, W("1") -> W(1), W("2") -> W(2))
      validate(module, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in class[K], class[V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[Int]])
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[String]])
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validate(module, W("1") -> W(1), W("2") -> W(2))
      validate(module, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in typeLiteral[K], typeLiteral[V], annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[Int]], annotation)
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[String]], annotation)
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnnotation[W[String], W[Int]](module, annotation, W("1") -> W(1), W("2") -> W(2))
      validateWithAnnotation[W[String], W[String]](module, annotation, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in class[K], class[V], annotation" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[Int]], annotation)
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[String]], annotation)
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnnotation(module, annotation, W("1") -> W(1), W("2") -> W(2))
      validateWithAnnotation(module, annotation, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in typeLiteral[K], typeLiteral[V], class[Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[Int]], classOf[Named])
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, typeLiteral[W[String]], typeLiteral[W[String]], classOf[Named])
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnn[W[String], W[Int], Named](module, W("1") -> W(1), W("2") -> W(2))
      validateWithAnn[W[String], W[String], Named](module, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind deep parameterization in class[K], class[V], class[Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val iBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[Int]], classOf[Named])
          iBinder.addBinding(W("1")).toInstance(W(1))
          iBinder.addBinding(W("2")).toInstance(W(2))
          val sBinder = ScalaMapBinder.newMapBinder(binder, classOf[W[String]], classOf[W[String]], classOf[Named])
          sBinder.addBinding(W("1")).toInstance(W("1"))
          sBinder.addBinding(W("2")).toInstance(W("2"))
        }
      }
      validateWithAnn[W[String], W[Int], Named](module, W("1") -> W(1), W("2") -> W(2))
      validateWithAnn[W[String], W[String], Named](module, W("1") -> W("1"), W("2") -> W("2"))
    }

    "bind from multiple instances of the same MapBinder" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          mBinder.addBinding("1").toInstance(1)
          val bBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          bBinder.addBinding("2").toInstance(2)
        }
      }
      validate(module, "1" -> 1, "2" -> 2)
    }

    "bind duplicate keys not permitted by default" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          mBinder.addBinding("1").toInstance(1)
          val bBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          bBinder.addBinding("1").toInstance(2)
        }
      }
      intercept[CreationException] {
        validate(module, "1" -> 1)
      }
    }

    "permit duplicate keys in empty [K,V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          mBinder.permitDuplicates()
        }
      }
      validateMultiMap[String,Int](module)
    }

    "permit duplicate keys in [K,V]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          mBinder.permitDuplicates()
          mBinder.addBinding("1").toInstance(1)
          val bBinder = ScalaMapBinder.newMapBinder[String, Int](binder)
          bBinder.addBinding("1").toInstance(2)
        }
      }
      validateMultiMap(module, "1" -> im.Set(1, 2))
    }

    "permit duplicate keys in [K,V](annotation)" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int](binder, annotation)
          mBinder.permitDuplicates()
          mBinder.addBinding("1").toInstance(1)
          val bBinder = ScalaMapBinder.newMapBinder[String, Int](binder, annotation)
          bBinder.addBinding("1").toInstance(2)
        }
      }
      validateMultiMapWithAnnotation(module, annotation, "1" -> im.Set(1, 2))
    }

    "permit duplicate keys in [K,V,Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val mBinder = ScalaMapBinder.newMapBinder[String, Int, Named](binder)
          mBinder.permitDuplicates()
          mBinder.addBinding("1").toInstance(1)
          val bBinder = ScalaMapBinder.newMapBinder[String, Int, Named](binder)
          bBinder.addBinding("1").toInstance(2)
        }
      }
      validateMultiMapWithAnn[String, Int, Named](module, "1" -> im.Set(1, 2))
    }
  }

  private def validate[K, V](map: PartialFunction[K, V] with Iterable[(K, V)], expected: (K, V)*): Unit = {
    map should have size expected.length
    for ((k, v) <- expected) {
      map(k) should equal(v)
    }
  }

  private def validate[K: TypeTag, V: TypeTag](module: Module, expected: (K, V)*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, V]], expected: _*)
    validate(injector.instance[im.Map[K, Provider[V]]].transform { case (_, provider) => provider.get() }, expected: _*)
    validate(injector.instance[im.Map[K, javax.inject.Provider[V]]].transform { case (_, provider) => provider.get() }, expected: _*)
  }

  private def validateWithAnnotation[K: TypeTag, V: TypeTag](module: Module, annotation: Annotation, expected: (K, V)*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, V]](annotation), expected: _*)
    validate(injector.instance[im.Map[K, Provider[V]]](annotation).transform { case (_, provider) => provider.get() }, expected: _*)
    validate(injector.instance[im.Map[K, javax.inject.Provider[V]]](annotation).transform { case (_, provider) => provider.get() }, expected: _*)
  }

  private def validateWithAnn[K: TypeTag, V: TypeTag, Ann <: Annotation : ClassTag](module: Module, expected: (K, V)*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, V], Ann], expected: _*)
    validate(injector.instance[im.Map[K, Provider[V]], Ann].transform { case (_, provider) => provider.get() }, expected: _*)
    validate(injector.instance[im.Map[K, javax.inject.Provider[V]], Ann].transform { case (_, provider) => provider.get() }, expected: _*)
  }

  private def validateMultiMap[K: TypeTag, V: TypeTag](module: Module, expected: (K, im.Set[V])*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, im.Set[V]]], expected: _*)
    validate(injector.instance[im.Map[K, im.Set[Provider[V]]]].transform { case (_, providers) => providers.map(_.get) }, expected: _*)
  }

  private def validateMultiMapWithAnnotation[K: TypeTag, V: TypeTag](module: Module, annotation: Annotation, expected: (K, im.Set[V])*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, im.Set[V]]](annotation), expected: _*)
    validate(injector.instance[im.Map[K, im.Set[Provider[V]]]](annotation).transform { case (_, providers) => providers.map(_.get) }, expected: _*)
  }

  private def validateMultiMapWithAnn[K: TypeTag, V: TypeTag, Ann <: Annotation : ClassTag](module: Module, expected: (K, im.Set[V])*): Unit = {
    val injector = Guice.createInjector(module)

    validate(injector.instance[im.Map[K, im.Set[V]], Ann], expected: _*)
    validate(injector.instance[im.Map[K, im.Set[Provider[V]]], Ann].transform { case (_, providers) => providers.map(_.get) }, expected: _*)
  }
}
