name := "Scala Guice"

description := "Scala syntax for Guice"

organization := "net.codingwell"

version := "4.2.7"

licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/codingwell/scala-guice"))

libraryDependencies ++= Seq(
  scalaBinaryVersion.value match {
    case "2.11" => "com.google.inject" % "guice" % "4.2.2"
    case _ => "com.google.inject" % "guice" % "4.2.3"
  },
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"

autoAPIMappings := true

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
<scm>
   <connection>scm:git:https://github.com/codingwell/scala-guice.git</connection>
   <developerConnection>scm:git:ssh://git@github.com:codingwell/scala-guice.git</developerConnection>
   <url>https://github.com/codingwell/scala-guice</url>
</scm>
<developers>
  <developer>
    <id>tsuckow</id>
    <name>Thomas Suckow</name>
    <email>tsuckow@gmail.com</email>
    <url>http://codingwell.net</url>
    <organization>Coding Well</organization>
    <organizationUrl>http://codingwell.net</organizationUrl>
    <roles>
      <role>developer</role>
    </roles>
  </developer>
</developers>
<contributors>
  <contributor>
    <name>Ben Lings</name>
    <roles>
      <role>creator</role>
    </roles>
  </contributor>
</contributors>
