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
import scala.util.Try

class BindingExtensionsSpec extends AnyWordSpec with Matchers {

  import BindingExtensions._

  def module(body: Binder => Unit): Module = new Module {
    def configure(binder: Binder): Unit = body(binder)
  }

  "Binding extensions" should {

    "allow binding source type using a type parameter" in {
      Guice createInjector module { binder =>
        binder.bindType[A].to(classOf[B])
      } getInstance classOf[A]
    }

    "allow binding target type using a type parameter" in {
      Guice createInjector module { binder =>
          binder.bindType[A].toType[B]
      } getInstance classOf[A]
    }

    "allow binding from a generic type" in {
      val inst = Guice createInjector module { binder =>
          binder.bindType[Gen[String]].toType[C]
      } getInstance new Key[Gen[String]] {}
      inst.get should equal ("String")
    }

    "allow binding between nested types" in {
      val inst = Guice createInjector module { binder =>
          binder.bindType[Outer.Gen[String]].toType[Outer.C]
      } getInstance new Key[Outer.Gen[String]] {}
      inst.get should equal ("String")
    }

    "allow binding to provider using type parameter" in {
      val inst = Guice createInjector module { binder =>
          binder.bindType[Gen[String]].toProviderType[GenStringProvider]
      } getInstance new Key[Gen[String]] {}
      inst.get should equal ("String")
    }

    "allow binding to provider of subtype using type parameter" in {
      val inst = Guice createInjector module { binder =>
          binder.bindType[Gen[String]].toProviderType[CProvider]
      } getInstance new Key[Gen[String]] {}
      inst.get should equal ("String")
    }

    "allow binding complex type" in {
      val inst = Guice createInjector module { binder =>
        binder.bindType[SomeClazz with Augmentation].toInstance(new SomeClazz with Augmentation)
      } getInstance classOf[SomeClazz]
      inst.get should equal("String with trait augmentation")
    }

    "allow binding complex type with type alias" in {
      val inst = Guice createInjector module { binder =>
        binder.bindType[Testing.SomeClazzWithAugmentation].toInstance(new SomeClazz with Augmentation)
      } getInstance classOf[SomeClazz]
      inst.get should equal("String with trait augmentation")
    }

    "allow binding with Nothing type" in {
      import net.codingwell.scalaguice.KeyExtensions._

      val e: Either[Try[Nothing], Nothing] = Left(Try(throw new Exception))

      val inst = Guice createInjector module { binder =>
        binder
          .bindType[Either[Try[Nothing], Nothing]]
          .toInstance(e)
      } getInstance typeLiteral[Either[Try[Nothing], Nothing]].toKey
      inst should equal(e)
    }
  }
}
