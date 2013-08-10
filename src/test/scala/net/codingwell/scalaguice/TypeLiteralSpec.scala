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

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

class TypeLiteralSpec extends Spec with ShouldMatchers {

  import com.google.inject._

  object Outer {
    class Inner
  }

  describe("type literal creation") {

    it("should create a type literal from a non-generic reference type") {
      typeLiteral[String] should equal (TypeLiteral.get(classOf[String]))
    }

    it("should create a type literal from a generic reference type") {
      typeLiteral[List[String]] should equal (new TypeLiteral[List[String]] {})
    }

    it("should convert type parameters to wrapper classes") {
      typeLiteral[List[Int]] should equal (new TypeLiteral[List[java.lang.Integer]] {})
    }

    it("should handle nested types") {
      typeLiteral[Outer.Inner] should equal (TypeLiteral.get(classOf[Outer.Inner]))
    }

    it("should handle type parameters that are nested types") {
      typeLiteral[List[Outer.Inner]] should equal (new TypeLiteral[List[Outer.Inner]] {})
    }

    it("should handle type parameters that are arrays") {
      typeLiteral[Array[Int]] should equal (new TypeLiteral[Array[Int]] {})
    }
  }
}
