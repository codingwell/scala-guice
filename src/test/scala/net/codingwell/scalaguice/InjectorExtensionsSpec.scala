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
import com.google.inject.name.Names.named
import com.google.inject.{AbstractModule, Guice}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{Matchers, WordSpec}

class InjectorExtensionsSpec extends WordSpec with Matchers {

  val module = new AbstractModule with ScalaModule {
    def configure() = {
      bind[A].to[B]
      bind[A].annotatedWith(named("d")).to[B]
      bind[B].annotatedWith(classOf[Named]).to[B]
      bind[Gen[String]].to[C]
    }
  }

  val injector = Guice createInjector module

  /** These functionality from theses tests are at compile-time. **/
  "Injector extensions" should {

    "allow instance to be retrieved using a type parameter" in {
      injector.instance[A]
    }

    "allow generic instance to be retrieved using a type parameter" in {
      val inst = injector.instance[Gen[String]]
      inst.get should equal("String")
    }

    "allow instance to be retreived using a type parameter and an annotation" in {
      injector.instance[A](named("d"))
    }

    "allow instance to be retreived using a type parameter and an annotation class" in {
      injector.instance[B, Named]
    }
  }
}