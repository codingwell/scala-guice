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
import com.google.inject.name.Names.named

class InjectorExtensionsSpec extends WordSpec with ShouldMatchers {

  import InjectorExtensions._

  val module = new AbstractModule {
    def configure = {
      bind(classOf[A]).to(classOf[B])
      bind(classOf[A]).annotatedWith(named("d")).to(classOf[B])
      bind(new TypeLiteral[Gen[String]]{}).to(classOf[C])
    }
  }

  val injector = Guice createInjector module

  "Injector extensions" should {

    "allow instance to be retrieved using a type parameter" in {
      injector.instance[A]
    }

    "allow generic instance to be retrieved using a type parameter" in {
      val inst = injector.instance[Gen[String]]
      inst.get should equal ("String")
    }
  }


}