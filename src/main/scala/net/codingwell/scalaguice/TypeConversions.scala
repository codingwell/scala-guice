package net.codingwell.scalaguice

import com.google.inject.internal.MoreTypes.WildcardTypeImpl
import com.google.inject.util.Types._
import java.lang.reflect.{Type => JavaType}
import scala.annotation.tailrec
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe.{Type => ScalaType, _}

/**
  * Copyright (C) 22/04/2018 - REstore NV
  */
private [scalaguice] object TypeConversions {
  private val anyType = typeOf[Any]
  private val nothingType = typeOf[Nothing]

  object ArrayType {
    private val arraySymbol = symbolOf[Array[_]]

    def unapply(tpe: ScalaType): Option[ScalaType] = {
      tpe match {
        case TypeRef(_, sym, args) if sym == arraySymbol => args.headOption
        case _ => None
      }

    }
  }

  object ClassType {

    @tailrec
    def unapply(tpe: ScalaType): Option[(ClassSymbol, Seq[ScalaType])] = {
      tpe match {
        case TypeRef(_, sym, args) if sym.isClass => Some((sym.asClass, args))
        case RefinedType(_, _) => unapply(tpe.erasure)
        case _ => None
      }

    }
  }

  object WildcardType {
    private val bottomType = typeOf[Nothing]
    private val topType = typeOf[Any]
    @tailrec
    def unapply(tpe: ScalaType): Option[(List[ScalaType], List[ScalaType])] = {
      tpe match {
        case BoundedWildcardType(bounds) => unapply(bounds)
        case TypeRef(_, sym, _) if sym.isType && !sym.isSpecialized =>
          unapply(sym.asType.info)
        case TypeBounds(lo, hi) =>
          val lowerBounds = if(lo =:= bottomType) Nil else List(lo)
          Some((lowerBounds, List(hi)))
        case _ => None
      }

    }

  }

  //https://github.com/scala/scala/blob/98b9a1c19ba2b7b342bc9b838628b86d00276540/src/reflect/scala/reflect/internal/Types.scala#L31

  def scalaTypeToJavaType(scalaType: ScalaType, mirror: Mirror, allowPrimative: Boolean = false): JavaType = {
    scalaType.dealias match {
      case `anyType`     => classOf[java.lang.Object]
      case `nothingType` => classOf[scala.runtime.Nothing$]
      case ExistentialType(_, underlying) => scalaTypeToJavaType(underlying, mirror)
      case ArrayType(argType) => arrayOf(scalaTypeToJavaType(argType, mirror, allowPrimative=true))
      case ClassType(symbol, args) =>
        //You would think `symbol` would be sufficient but in some cases (such as (=> Unit)) instead of
        //resolving to `Function0[Unit]` You get Scala<byname> which isn't real and can't be located
        val rawType:Class[_] = try {
          mirror.runtimeClass(symbol)
        } catch {
          case _:ClassNotFoundException =>
            mirror.runtimeClass(scalaType.erasure.dealias.typeSymbol.asInstanceOf[ClassSymbol])
        }
        val ownerType = findOwnerOf(symbol, mirror)
        if(symbol == symbolOf[Unit]) {
          classOf[scala.runtime.BoxedUnit]
        } else {
          args.map(arg => scalaTypeToJavaType(arg, mirror)) match {
            case Nil => if(allowPrimative) rawType else toWrapper(rawType)
            case mappedArgs if ownerType.nonEmpty => newParameterizedTypeWithOwner(ownerType.get, rawType, mappedArgs:_*)
            case mappedArgs => newParameterizedType(rawType, mappedArgs:_*)
          }
        }
      case WildcardType(lowerBounds, upperBounds) =>
        val mappedUpperBounds = upperBounds.map(bound => scalaTypeToJavaType(bound, mirror)).toArray
        val mappedLowerBounds = lowerBounds.map(bound => scalaTypeToJavaType(bound, mirror)).toArray
        new WildcardTypeImpl(mappedUpperBounds, mappedLowerBounds)
      case SingleType(_, symbol) if symbol.isModule =>
        val rm = universe.runtimeMirror(getClass.getClassLoader)
        val moduleReflection = rm.reflectModule(symbol.asModule)
        val instanceMirror = mirror.reflect(moduleReflection.instance)
        val classMirror = rm.reflectClass(instanceMirror.symbol)

        scalaTypeToJavaType(classMirror.symbol.toType, mirror)
      case _ => throw new UnsupportedOperationException(s"Could not convert scalaType $scalaType to a javaType: " + scalaType.dealias.getClass.getName)
    }
  }

  private def findOwnerOf(symbol: universe.ClassSymbol, mirror: Mirror): Option[JavaType] = {
    val owner = symbol.owner

    if (!owner.isPackage && (owner.isModuleClass && owner.owner.isPackage)) { //workaround for when owner is a top level object
      // here we need to resolve class without the $ suffix for some reason
      val clazz = mirror.staticClass(owner.asClass.fullName)
      Some(mirror.runtimeClass(clazz))
    } else if (!owner.isPackage && owner.isClass) {
      try {
        Some(mirror.runtimeClass(owner.asClass))
      } catch {
        case _:ClassNotFoundException => None
      }
    } else {
      None
    }
  }

  private def toWrapper(c: JavaType): JavaType = c match {
    case java.lang.Byte.TYPE => classOf[java.lang.Byte]
    case java.lang.Short.TYPE => classOf[java.lang.Short]
    case java.lang.Character.TYPE => classOf[java.lang.Character]
    case java.lang.Integer.TYPE => classOf[java.lang.Integer]
    case java.lang.Long.TYPE => classOf[java.lang.Long]
    case java.lang.Float.TYPE => classOf[java.lang.Float]
    case java.lang.Double.TYPE => classOf[java.lang.Double]
    case java.lang.Boolean.TYPE => classOf[java.lang.Boolean]
    case java.lang.Void.TYPE => classOf[java.lang.Void]
    case cls => cls
  }

}
