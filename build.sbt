name := "Scala Guice"

description := "Scala syntax for Guice"

organization := "net.codingwell"

version := "4.2.4"

licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/codingwell/scala-guice"))

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.2.2",
  "com.google.guava" % "guava" % "25.1-android",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

libraryDependencies += { if(scalaVersion.value.startsWith("2.13.")) "org.scalatest" %% "scalatest" % "3.1.0-SNAP13" % "test"
  else "org.scalatest" %% "scalatest" % "3.0.1" % "test" }

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "3.0.2" % "compile"

autoAPIMappings := true

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0")

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
