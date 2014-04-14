name := "play-hubs"

organization := "play-infra"

version := "0.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test"
)

play.Project.playScalaSettings

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

publishTo := Some(Resolver.file("file",  new File( "/mvn-repo" )) )

testOptions in Test += Tests.Argument("junitxml")
