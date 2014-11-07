name := "Scala Guice"

description := "Scala syntax for Guice"

organization := "net.codingwell"

version := "4.0.0-beta5"

licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/codingwell/scala-guice"))

useGpg := true

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.0-beta5",
  "com.google.inject.extensions" % "guice-multibindings" % "4.0-beta5",
  "com.google.guava" % "guava" % "17.0"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.+" % "compile"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

testListeners <<= target.map(t => Seq(new eu.henkelmann.sbt.JUnitXmlTestsListener(t.getAbsolutePath)))

scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
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
