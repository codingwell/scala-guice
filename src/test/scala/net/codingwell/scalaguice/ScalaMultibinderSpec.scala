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
import com.google.inject.{AbstractModule, Guice}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{Matchers, WordSpec}

import scala.collection.{immutable => im}

class ScalaMultibinderSpec extends WordSpec with Matchers {
  private case class W[T](t: T)
  private val annotation = Names.named("N")

  "A multibinder" should {
    "bind empty [T]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder[String](binder)
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]])
    }

    "bind [TypeLiteral]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, typeLiteral[String])
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]], "A", "B")
    }

    "bind [Class]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, classOf[String])
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]], "A", "B")
    }

    "bind [TypeLiteral, Annotation]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, typeLiteral[String], annotation)
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]](annotation), "A", "B")
    }

    "bind [Class, Annotation]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, classOf[String], annotation)
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]](annotation), "A", "B")
    }

    "bind [TypeLiteral, ClassAnnotation]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, typeLiteral[String], classOf[Named])
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String], Named], "A", "B")
    }

    "bind [Class, ClassAnnotation]" in {
      import com.google.inject.name.Named
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, classOf[String], classOf[Named])
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String], Named], "A", "B")
    }

    "deduplicate" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, typeLiteral[Symbol])
          multi.addBinding.toInstance('A)
          multi.addBinding.toInstance('A)
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[Symbol]], 'A)
    }

    "permit duplicates" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder(binder, typeLiteral[Symbol]).permitDuplicates()
          multi.addBinding.toInstance('A)
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[Symbol]], 'A)
    }

    "bind from multiple modules" in {
      def newModule(i: Int) = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          val multi = ScalaMultibinder.newSetBinder[Int](binder)
          multi.addBinding.toInstance(i)
        }
      }
      validate(Guice.createInjector(newModule(1), newModule(2)).instance[im.Set[Int]], 1, 2)
    }

    "bind deep parameterization in [Class]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, classOf[W[String]])
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, classOf[W[Int]])
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]], W("A"))
      validate(injector.instance[im.Set[W[Int]]], W(1))
    }

    "bind deep parameterization in [Class, Annotation]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, classOf[W[String]], annotation)
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, classOf[W[Int]], annotation)
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]](annotation), W("A"))
      validate(injector.instance[im.Set[W[Int]]](annotation), W(1))
    }

    "bind deep parameterization in [Class, ClassAnnotation]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, classOf[W[String]], classOf[Named])
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, classOf[W[Int]], classOf[Named])
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]], Named], W("A"))
      validate(injector.instance[im.Set[W[Int]], Named], W(1))
    }

    "bind deep parameterization in [TypeLiteral]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[String]])
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[Int]])
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]], W("A"))
      validate(injector.instance[im.Set[W[Int]]], W(1))
    }

    "bind deep parameterization in [typeLiteral, Annotation]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[String]], annotation)
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[Int]], annotation)
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]](annotation), W("A"))
      validate(injector.instance[im.Set[W[Int]]](annotation), W(1))
    }

    "bind deep parameterization in [TypeLiteral, ClassAnnotation]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[String]], classOf[Named])
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder(binder, typeLiteral[W[Int]], classOf[Named])
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]], Named], W("A"))
      validate(injector.instance[im.Set[W[Int]], Named], W(1))
    }

    /** Scala Addons */

    "bind [T]" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder[String](binder)
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]], "A", "B")
    }

    "bind [T, Ann]" in {
      import com.google.inject.name.Named
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder[String, Named](binder)
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String], Named], "A", "B")
    }

    "bind [T](Ann)" in {
      val module = new AbstractModule with ScalaModule {
        def configure() = {
          val multi = ScalaMultibinder.newSetBinder[String](binder, annotation)
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      validate(Guice.createInjector(module).instance[im.Set[String]](annotation), "A", "B")
    }

    "bind deep parameterization in [T]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder[W[String]](binder)
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder[W[Int]](binder)
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]], W("A"))
      validate(injector.instance[im.Set[W[Int]]], W(1))
    }

    "bind deep parameterization in [T, Ann]" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder[W[String], Named](binder)
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder[W[Int], Named](binder)
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]], Named], W("A"))
      validate(injector.instance[im.Set[W[Int]], Named], W(1))
    }

    "bind deep parameterization in [T](annotation)" in {
      val module = new AbstractModule with ScalaModule {
        override def configure() = {
          val mbStrings = ScalaMultibinder.newSetBinder[W[String]](binder, annotation)
          mbStrings.addBinding.toInstance(W("A"))
          val mbInts = ScalaMultibinder.newSetBinder[W[Int]](binder, annotation)
          mbInts.addBinding.toInstance(W(1))
        }
      }

      val injector = Guice.createInjector(module)
      validate(injector.instance[im.Set[W[String]]](annotation), W("A"))
      validate(injector.instance[im.Set[W[Int]]](annotation), W(1))
    }
  }

  private def validate[T](set: Set[T], expected: T*) {
    set should have size expected.length
    for (e <- expected) {
      set should contain(e)
    }
  }
}
