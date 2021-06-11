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
import com.google.inject.{AbstractModule, Guice, Injector, Key}
import net.codingwell.scalaguice.InjectorExtensions._
import net.codingwell.scalaguice.KeyExtensions._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.reflect.runtime.universe.TypeTag

class InjectorExtensionsSpec extends AnyWordSpec with Matchers {

  val module = new AbstractModule with ScalaModule {
    override def configure(): Unit = {
      bind[A].to[B]
      bind[A].annotatedWith(named("d")).to[B]
      bind[B].annotatedWith(classOf[Named]).to[B]
      bind[Gen[String]].to[C]
    }
  }

  val injector: Injector = Guice.createInjector(module)

  /** These functionality from theses tests are at compile-time. **/
  "Injector extensions" should {

    "allow instance to be retrieved using a type parameter" in {
      injector.instance[A]
    }

    "allow generic instance to be retrieved using a type parameter" in {
      val inst = injector.instance[Gen[String]]
      inst.get should equal("String")
    }

    "allow instance to be retrieved using a type parameter and an annotation" in {
      injector.instance[A](named("d"))
    }

    "allow instance to be retrieved using a type parameter and an annotation class" in {
      injector.instance[B, Named]
    }

    "allow existing bindings to be retrieved optionally" in {
      val Some(binding) = injector.existingBinding[A]
      binding.getProvider.get() shouldBe a [B]
    }

    "allow missing bindings to be retrieved optionally" in {
      injector.existingBinding[Foo] should not be defined
    }

    "allow existing annotated bindings to be retrieved optionally" in {
      val Some(binding) = injector.existingBinding[B, Named]
      binding.getProvider.get() shouldBe a [B]
    }

    "allow missing annotated bindings to be retrieved optionally" in {
      injector.existingBinding[Foo, Named] should not be defined
    }

    "allow existing named bindings to be retrieved optionally" in {
      val Some(binding) = injector.existingBinding[A](named("d"))
      binding.getProvider.get() shouldBe a [B]
    }

    "allow missing named bindings to be retrieved optionally" in {
      injector.existingBinding[Foo](named("d")) should not be defined
      injector.existingBinding[A](named("foo")) should not be defined
    }

    def keyExistsFn[T: TypeTag]: Key[T] = typeLiteral[T].toKey
    def keyMissingFn[T: TypeTag]: Key[T] = typeLiteral[T].annotatedWithName("foo")

    "allow existing bindings to be retrieved by key optionally" in {
      val Some(binding) = injector.existingBinding[A](keyExistsFn[A])
      binding.getProvider.get() shouldBe a [B]
    }

    "allow missing bindings to be retrieved by key optionally" in {
      injector.existingBinding[A](keyMissingFn[A]) should not be defined
    }

    "allow provider to be retrieved using a type parameter" in {
      injector.provider[A]
    }

    "allow generic provider to be retrieved using a type parameter" in {
      val inst = injector.provider[Gen[String]]
      inst.get.get should equal("String")
    }

    "allow provider to be retrieved using a type parameter and an annotation" in {
      injector.provider[A](named("d"))
    }

    "allow provider to be retrieved using a type parameter and an annotation class" in {
      injector.provider[B, Named]
    }
  }
}
