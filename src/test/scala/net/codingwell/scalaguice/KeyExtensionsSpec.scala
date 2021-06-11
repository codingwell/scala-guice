package net.codingwell.scalaguice

import com.google.inject.Key
import com.google.inject.name.{Named, Names}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class KeyExtensionsSpec extends AnyWordSpec with Matchers {
  import KeyExtensions._

  private final val TestAnnotation = Names.named("foo")
  private final val TestTypeLiteral = typeLiteral[String]

  "Key extensions" should {

    "build key from type literal " in {
      TestTypeLiteral.toKey should equal(Key.get(TestTypeLiteral))
    }

    "build key from type literal and annotation" in {
      TestTypeLiteral.annotatedWith(TestAnnotation) should equal(Key.get(TestTypeLiteral, TestAnnotation))
    }

    "build key from type literal and class of annotation" in {
      TestTypeLiteral.annotatedWith(classOf[Named]) should equal(Key.get(TestTypeLiteral, classOf[Named]))
    }

    "build key from type literal and annotation type" in {
      TestTypeLiteral.annotatedWith[Named] should equal(Key.get(TestTypeLiteral, cls[Named]))
    }

    "build key from type literal and name String" in {
      TestTypeLiteral.annotatedWithName("foo") should equal(Key.get(TestTypeLiteral, TestAnnotation))
    }
  }
}
