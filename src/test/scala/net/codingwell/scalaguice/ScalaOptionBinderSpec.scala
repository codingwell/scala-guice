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
import com.google.inject.name.{Named, Names}
import com.google.inject.{AbstractModule, Guice, Key, Module, Provider}
import java.lang.annotation.Annotation
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag

class ScalaOptionBinderSpec extends AnyWordSpec with Matchers {
  private case class W[T](t: T)
  private val annotation = Names.named("N")

  "An OptionBinder" should {
    /** New Scala Methods, Happy Path */

    "bind [T]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String](binder)
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind [T, Ann]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String, Named](binder)
          opt.setDefault.toInstance("A")
        }
      }

      validateWithAnn[String, Named](module)
    }

    "bind [T, Annotation]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String](binder, annotation)
          opt.setDefault.toInstance("A")
        }
      }

      validateWithAnnotation(module, annotation, expected = "A")
    }

    "bind [Class]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder(binder, classOf[String])
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind [TypeLiteral]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder(binder, typeLiteral[String])
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind [Key]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder(binder, Key.get(typeLiteral[String]))
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind optional without default" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String](binder)
        }
      }

      validateAbsent(module)
    }

    /** Guice's Optional Binder API, Happy Path */
    "bind optional (original API) [Class]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionalBinder(binder, classOf[String])
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind optional (original API) [TypeLiteral]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionalBinder(binder, typeLiteral[String])
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind optional (original API) [Key]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionalBinder(binder, Key.get(typeLiteral[String]))
          opt.setDefault.toInstance("A")
        }
      }

      validate(module)
    }

    "bind optional without default (original API)" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionalBinder(binder, classOf[String])
        }
      }

      validateAbsent(module)
    }

    /** Non Happy Path */
    "override default value" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String](binder)
          opt.setDefault.toInstance("B")
          opt.setBinding.toInstance("A")
        }
      }

      validate(module)
    }

    "override default value with second binder" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val opt = ScalaOptionBinder.newOptionBinder[String](binder)
          opt.setDefault.toInstance("B")
          val opt2 = ScalaOptionBinder.newOptionBinder[String](binder)
          opt2.setBinding.toInstance("A")
        }
      }

      validate(module)
    }

    "allow separately annotated bindings for the same [T]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val aOpt = ScalaOptionBinder.newOptionBinder[String](binder, Names.named("A"))
          aOpt.setBinding.toInstance("A")
          val bOpt = ScalaOptionBinder.newOptionBinder[String](binder, Names.named("B"))
          bOpt.setDefault.toInstance("B")
        }
      }

      validateWithAnnotation(module, Names.named("A"), expected = "A")
      validateWithAnnotation(module, Names.named("B"), expected = "B")
    }

    /** New Scala Methods, Parameterized Wrappers */

    "bind deep parameterization [T]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder[W[String]](binder)
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder[W[Int]](binder)
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    "bind deep parameterization in [T, Ann]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder[W[String], Named](binder)
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder[W[Int], Named](binder)
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validateWithAnn[W[String], Named](module, expected = W("A"))
      validateWithAnn[W[Int], Named](module, expected = W(1))
    }

    "bind deep parameterization in [T, Annotation]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder[W[String]](binder, annotation)
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder[W[Int]](binder, annotation)
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validateWithAnnotation(module, annotation, expected = W("A"))
      validateWithAnnotation(module, annotation, expected = W(1))
    }

    "bind deep parameterization in [Class]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder(binder, classOf[W[String]])
          sOpt.setBinding.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder(binder, classOf[W[Int]])
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    "bind deep parameterization in [TypeLiteral]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder(binder, typeLiteral[W[String]])
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder(binder, typeLiteral[W[Int]])
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    "bind deep parameterization in [Key]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionBinder(binder, Key.get(typeLiteral[W[String]]))
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionBinder(binder, Key.get(typeLiteral[W[Int]]))
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    /** Guice's Optional Binder API, Parameterized Wrappers */

    "bind optional (original API) deep parameterization in [Class]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionalBinder(binder, classOf[W[String]])
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionalBinder(binder, classOf[W[Int]])
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    "bind optional (original API) deep parameterization in [TypeLiteral]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionalBinder(binder, typeLiteral[W[String]])
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionalBinder(binder, typeLiteral[W[Int]])
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    "bind optional (original API) deep parameterization in [Key]" in {
      val module = new AbstractModule {
        override def configure(): Unit = {
          val sOpt = ScalaOptionBinder.newOptionalBinder(binder, Key.get(typeLiteral[W[String]]))
          sOpt.setDefault.toInstance(W("A"))
          val nOpt = ScalaOptionBinder.newOptionalBinder(binder, Key.get(typeLiteral[W[Int]]))
          nOpt.setDefault.toInstance(W(1))
        }
      }

      validate(module, expected = W("A"))
      validate(module, expected = W(1))
    }

    /** Indirect New Scala Methods, Parameterized Wrappers */

    "bind [T] indirectly" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          ScalaOptionBinder.newOptionBinder[String](binder)
          bind[String].toInstance("A")
        }
      }

      validate(module)
    }

  }

  private def validate[T: TypeTag](module: Module, expected: T = "A"): Unit = {
    val injector = Guice.createInjector(module)

    // Check Option
    injector.instance[Option[T]] should contain(expected)
    injector.instance[Option[Provider[T]]].get.get() should equal(expected)
    injector.instance[Option[javax.inject.Provider[T]]].get.get() should equal(expected)

    // Check Optional
    injector.instance[Optional[T]].get should equal(expected)
    injector.instance[Optional[Provider[T]]].get.get() should equal(expected)
    injector.instance[Optional[javax.inject.Provider[T]]].get.get() should equal(expected)
  }

  private def validateWithAnn[T: TypeTag, Ann <: Annotation : ClassTag](module: Module, expected: T = "A"): Unit = {
    val injector = Guice.createInjector(module)

    // Check Option
    injector.instance[Option[T], Ann] should contain(expected)
    injector.instance[Option[Provider[T]], Ann].get.get() should equal(expected)
    injector.instance[Option[javax.inject.Provider[T]], Ann].get.get() should equal(expected)

    // Check Optional
    injector.instance[Optional[T], Ann].get should equal(expected)
    injector.instance[Optional[Provider[T]], Ann].get.get() should equal(expected)
    injector.instance[Optional[javax.inject.Provider[T]], Ann].get.get() should equal(expected)
  }

  private def validateWithAnnotation[T: TypeTag](module: Module, annotation: Annotation, expected: T): Unit = {
    val injector = Guice.createInjector(module)

    // Check Option
    injector.instance[Option[T]](annotation) should contain(expected)
    injector.instance[Option[Provider[T]]](annotation).get.get() should equal(expected)
    injector.instance[Option[javax.inject.Provider[T]]](annotation).get.get() should equal(expected)

    // Check Optional
    injector.instance[Optional[T]](annotation).get should equal(expected)
    injector.instance[Optional[Provider[T]]](annotation).get.get() should equal(expected)
    injector.instance[Optional[javax.inject.Provider[T]]](annotation).get.get() should equal(expected)
  }

  private def validateAbsent[T: TypeTag](module: Module, expected: T = "A"): Unit = {
    val injector = Guice.createInjector(module)

    // Check Option
    injector.instance[Option[T]] should be (None)
    injector.instance[Option[Provider[T]]] should be (None)
    injector.instance[Option[javax.inject.Provider[T]]] should be (None)

    // Check Optional
    injector.instance[Optional[T]].isPresent should be (false)
    injector.instance[Optional[Provider[T]]].isPresent should be (false)
    injector.instance[Optional[javax.inject.Provider[T]]].isPresent should be (false)
  }
}
