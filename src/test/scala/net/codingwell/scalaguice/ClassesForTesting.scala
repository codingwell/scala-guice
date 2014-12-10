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

import com.google.inject.TypeLiteral
import javax.inject.{Provider, Inject, Named}
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

object Outer {
  trait A
  class B extends A
  trait Gen[T] {
    def get: T
  }

  class C extends Gen[String] {
    def get = "String"
  }
}

trait A
class B extends A

class BProvider extends Provider[B] {
  def get = new B
}

trait Gen[T] {
  def get: T
}

class TypeProvider[T] @Inject() ( typ:TypeLiteral[T] ) extends Provider[String] {
  def get = typ.toString
}

class C extends Gen[String] {
  def get = "String"
}

class GenStringProvider extends Provider[Gen[String]] {
  def get = new C
}

class CProvider extends Provider[C] {
  def get = new C
}

trait Foo {
  def foo(): String
}

class FooProviderWithJavax extends javax.inject.Provider[Foo] {
  def get(): Foo = new Foo {
    def foo() = "foo"
  }
}

case class TwoStrings @Inject()(@Named("first") first: String, @Named("second") second: String)

trait Say {
  def hi(str: String): String
}
class SayHi extends Say {
  @AOP
  def hi(str: String): String = str
}
class AOPI extends MethodInterceptor {
  def invoke(invocation: MethodInvocation): AnyRef = s"""Hi ${invocation.proceed().toString}"""
}
