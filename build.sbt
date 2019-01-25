name := "transaction-manager"

resolvers += Resolver.mavenLocal
resolvers ++= List("today nexus" at "http://nexus.today36524.td/repository/maven-public/")


lazy val commonSettings = Seq(
  organization := "com.github.dapeng",
  version := "2.1.1-RELEASE",
  scalaVersion := "2.12.2"
)

lazy val api = (project in file("transaction-manager-api"))
  .settings(
    commonSettings,
    name := "transaction-manager-api",
    publishTo := Some("today-snapshots" at "http://nexus.today36524.td/repository/maven-releases/"),
    credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.today36524.td", "central-services", "E@Z.nrW3"),
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    libraryDependencies ++= Seq(
      "com.github.dapeng-soa" % "dapeng-client-netty" % "2.1.2-SNAPSHOT"
    )
  ).enablePlugins(ThriftGeneratorPlugin)


lazy val service = (project in file("transaction-manager-service"))
  .dependsOn( api )
  .settings(
    commonSettings,
    name := "transaction-manager_service",
    crossPaths := false,
    libraryDependencies ++= Seq(
      "com.github.dapeng-soa" % "dapeng-spring" % "2.1.2-SNAPSHOT",
      "com.github.wangzaixiang" %% "scala-sql" % "2.0.6",
      "org.slf4j" % "slf4j-api" % "1.7.13",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.25",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "ch.qos.logback" % "logback-core" % "1.1.3",
      "org.codehaus.janino" % "janino" % "2.7.8", //logback (use if condition in logBack config file need this dependency)
      "mysql" % "mysql-connector-java" % "5.1.36",
      "com.alibaba" % "druid" % "1.1.9",
      "org.springframework" % "spring-context" % "4.3.5.RELEASE",
      "com.today" %% "service-commons" % "1.7-RELEASE",
      "com.today" %% "idgen-api" % "2.1.1",
      "org.scalaj" % "scalaj-http_2.12" % "2.3.0",
      "org.springframework" % "spring-tx" % "4.3.5.RELEASE",
      "org.springframework" % "spring-jdbc" % "4.3.5.RELEASE"
    )).enablePlugins(ImageGeneratorPlugin)
  .enablePlugins(DbGeneratePlugin)
  .enablePlugins(RunContainerPlugin)
