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

class ScalaModuleSpec extends WordSpec with ShouldMatchers {

  "A Scala Guice module" should {

    "allow binding source type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[A].to(classOf[B])
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding target type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[A].to[B]
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding target provider type using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[A].toProvider[BProvider]
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding to provider of subtype using type parameter" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[Gen[String]].toProvider[CProvider]
        }
      }
      Guice.createInjector(module).getInstance(new Key[Gen[String]] {})
    }

    "allow binding to provider with injected type literal" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[String].toProvider[TypeProvider[B]]
        }
      }
      Guice.createInjector(module).getInstance(new Key[String] {})
    }

    "allow binding in scope using a type parameter" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[A].to[B].in[Singleton]
        }
      }
      Guice.createInjector(module).getInstance(classOf[A])
    }

    "allow binding with annotation using a type parameter" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          bind[A].annotatedWith[Named].to[B]
        }
      }
      Guice.createInjector(module).getInstance(Key.get(classOf[A],classOf[Named]))
    }

    "give a useful error when bound on itself" in {
      val module = new AbstractModule with ScalaModule {
        def configure = {
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
  }

}
