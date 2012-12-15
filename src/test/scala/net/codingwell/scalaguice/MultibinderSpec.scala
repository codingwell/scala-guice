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
import com.google.inject.name._

import java.util.{Set => JSet, HashSet => JHashSet}

import scala.collection.{ immutable => im }

class MultibinderSpec extends WordSpec with ShouldMatchers {

  "A multibinder" should {
//  def newSetBinder[T : Manifest]( binder:Binder, settype:TypeLiteral[T], annotation:Annotation ) = {
//  def newSetBinder[T : Manifest]( binder:Binder, settype:Class[T], annotation:Annotation ) = {
//  def newSetBinder[T : Manifest]( binder:Binder, settype:TypeLiteral[T], annotation:Class[_ <: Annotation] ) = {
//  def newSetBinder[T : Manifest]( binder:Binder, settype:Class[T], annotation:Class[_ <: Annotation] ) = {

    "bind [TypeLiteral]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, typeLiteral[String] )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [Class]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, classOf[String] )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [TypeLiteral, Annotation]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, typeLiteral[String], Names.named("bla") )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], Names.named("bla") ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [Class, Annotation]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, classOf[String], Names.named("bla") )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], Names.named("bla") ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [TypeLiteral, ClassAnotation]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, typeLiteral[String], classOf[Named] )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], classOf[Named] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [Class, ClassAnnotation]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, classOf[String], classOf[Named] )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], classOf[Named] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "not permit duplicates" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, typeLiteral[Symbol] )
          multi.addBinding.toInstance('A)
          multi.addBinding.toInstance('A)
        }
      }
      intercept[ProvisionException] {
        val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[Symbol]] ))
      }
    }

    "permit duplicates" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder( binder, typeLiteral[Symbol] ).permitDuplicates
          multi.addBinding.toInstance('A)
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[Symbol]] ))
      set should have size (1)
      set should contain ('A)
    }

    //Scala Addons

    "bind [T]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder[String]( binder )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [T, Ann]" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder[String,Named]( binder )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], classOf[Named] ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }

    "bind [T](Ann)" in {
      import name.Named
      val module = new AbstractModule with ScalaModule {
        def configure = {
          val multi = ScalaMultibinder.newSetBinder[String]( binder, Names.named("bla") )
          multi.addBinding.toInstance("A")
          multi.addBinding.toInstance("B")
        }
      }
      val set = Guice.createInjector(module).getInstance( Key.get( typeLiteral[im.Set[String]], Names.named("bla") ))
      set should have size (2)
      set should contain ("A")
      set should contain ("B")
    }
  }
}

