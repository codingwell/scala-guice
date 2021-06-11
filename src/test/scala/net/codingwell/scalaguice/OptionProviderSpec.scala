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
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Guice, Key}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class OptionProviderSpec extends AnyWordSpec with Matchers {
  "An Option Provider" should {
    "allow binding an Optional" in {
      val module = new AbstractModule with ScalaModule {
        override def configure(): Unit = {
          bind[Optional[String]].toInstance(Optional.of("Hello World"))
          val key = Key.get(typeLiteral[Optional[String]])
          bind[Option[String]].toProvider(new OptionProvider[String](key))
        }
      }
      val opt = Guice.createInjector(module).instance[Option[String]]
      opt should contain("Hello World")
    }
  }

  "allow binding an Optional with an annotation" in {
    val module = new AbstractModule with ScalaModule {
      override def configure(): Unit = {
        bind[Optional[String]].annotatedWith[Named].toInstance(Optional.of("Hello World"))
        val key = Key.get(typeLiteral[Optional[String]], classOf[Named])
        bind[Option[String]].annotatedWith[Named].toProvider(new OptionProvider(key))
      }
    }
    val opt = Guice.createInjector(module).instance[Option[String], Named]
    opt should contain("Hello World")
  }

  "allow binding an absent Optional" in {
    val module = new AbstractModule with ScalaModule {
      override def configure(): Unit = {
          bind[Optional[String]].toInstance(Optional.absent())
          val key = Key.get(typeLiteral[Optional[String]])
          bind[Option[String]].toProvider(new OptionProvider[String](key))
      }
    }
    val opt = Guice.createInjector(module).instance[Option[String]]
    opt should be (None)
  }
}
