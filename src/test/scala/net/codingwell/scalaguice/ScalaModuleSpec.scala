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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ScalaModuleSpec extends AnyWordSpec with Matchers {

  "A Scala Guice module" should {

    "allow binding source type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].to(classOf[B])
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding target type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].to[B]
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding target provider type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].toProvider[BProvider]
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding to provider of subtype using type parameter" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[Gen[String]].toProvider[CProvider]
        }
      }
      Guice.createInjector(module).getInstance(new Key[Gen[String]] {})
    }

    "allow binding to provider with injected type literal" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[String].toProvider[TypeProvider[B]]
        }
      }
      Guice.createInjector(module).getInstance(new Key[String] {})
    }

    "allow binding in scope using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].to[B].in[Singleton]()
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding a container with a generic singleton type" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[SealedTraitContainer[FinalSealedTrait.type]].toProvider[SealedTraitContainerFinalSealedTraitProvider]
        }
      }
      Guice.createInjector(module).getInstance(new Key[SealedTraitContainer[FinalSealedTrait.type]] {})
    }

    "allow binding with annotation using a type parameter" in {
      import com.google.inject.name.Named
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].annotatedWith[Named].to[B]
        }
      }
      Guice.createInjector(module).getInstance(Key.get(classOf[A],classOf[Named]))
    }

    "allow use provider form javax.inject.Provider" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[Foo].toProvider[FooProviderWithJavax]
        }
      }
      Guice.createInjector(module).getInstance(classOf[Foo])
    }

    "give a useful error when bound on itself" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[A].to[A]
        }
      }
      val thrown = intercept[CreationException] {
         Guice.createInjector(module).getInstance(classOf[A])
      }
      val messages = thrown.getErrorMessages
      assert( messages.size == 1 )
      val sources = messages.iterator.next.getSource
      assert( sources.contains("ScalaModuleSpec.scala") )
    }

    "allow use annotatedWithName" in {
      import net.codingwell.scalaguice.BindingExtensions._
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[String].annotatedWithName("first").toInstance("first")
          bindConstant().annotatedWithName("second").to("second")
        }
      }
      val twoStrings = Guice.createInjector(module).getInstance(classOf[TwoStrings])
      twoStrings.first should be ("first")
      twoStrings.second should be ("second")
    }

    "allow binding annotation interceptor" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[Say].to[SayHi]
          bindInterceptor[AOPI](methodMatcher = annotatedWith[AOP])
        }
      }
      val say = Guice.createInjector(module).getInstance(classOf[Say])
      say.hi("Bob") should be ("Hi Bob")
    }

    //This test needs work to resolve #65
    "allow binding by name to Unit" in {
      try { 
      val foo:(=> Unit) => String = (a) => "dog"
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[(=> Unit) => String].toInstance(foo)
          bindInterceptor[AOPI](methodMatcher = annotatedWith[AOP])
        }
      }
      import net.codingwell.scalaguice.InjectorExtensions._
      val injector = Guice.createInjector(module)
      val func = injector.instance[(=> Unit) => String]
      func shouldEqual foo
      } catch {
        case e: Throwable => { e.printStackTrace(); throw e }
      }
    }


    //Regression test for #43 (which hasn't been fixed yet)
    "allow binding with mixins" ignore {
      val a = new A {}
      val ad = new A with D {}

      val module = new AbstractModule with ScalaModule {
          override def configure(): Unit = {
              bind[A].toInstance(a)
              bind[A with D].toInstance(ad)
          }
      }

      import net.codingwell.scalaguice.InjectorExtensions._
      val injector = Guice.createInjector(module)
      (injector.instance[A]) shouldEqual a
      injector.instance[A with D] shouldEqual ad
    }
  }

}
