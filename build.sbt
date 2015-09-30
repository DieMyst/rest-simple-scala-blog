name := "rest-simple-scala-blog"

version := "1.0"

scalaVersion := "2.11.7"

val httpAkkaVersion = "1.0"
val phantomVersion = "1.12.2"

resolvers ++= Seq(
  "Typesafe repository snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype snapshots"               at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
  "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/",
  "Twitter Repository"               at "http://maven.twttr.com",
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-http-experimental_2.11"            % httpAkkaVersion,
  "com.typesafe.akka" % "akka-stream-experimental_2.11"          % httpAkkaVersion,
  "com.typesafe.akka" % "akka-http-core-experimental_2.11"       % httpAkkaVersion,
  "com.typesafe.akka" % "akka-http-spray-json-experimental_2.11" % httpAkkaVersion,

  "joda-time" % "joda-time" % "2.8.2",
  "org.slf4j" % "slf4j-simple" % "1.7.12",


  "com.websudos" %% "phantom-dsl" % phantomVersion,
  "com.websudos" %% "phantom-testkit" % phantomVersion % "test, provided",

  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.12" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test",
  "com.typesafe.akka" % "akka-http-testkit-experimental_2.11" % httpAkkaVersion % "test"
)
    