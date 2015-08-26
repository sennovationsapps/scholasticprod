
import play.PlayImport.PlayKeys._

organization := "scholastic"

name := "scholastic-copy"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.10.4", "2.11.1")

version := "1.9.0-SNAPSHOT"

routesImport += "models._"

val appDependencies = Seq(
      "com.mohiva" %% "play-html-compressor" % "0.3",
      "be.objectify"  %%  "deadbolt-java"     % "2.3.2",
      // Comment this for local development of the Play Authentication core
      "com.feth"      %% "play-authenticate" % "0.6.5-SNAPSHOT",
      "commons-io" % "commons-io" % "2.4",
      "commons-collections" % "commons-collections" % "3.2.1",
      "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "r239",
      // "postgresql"    %   "postgresql"        % "9.1-901-1.jdbc4",
      "mysql" % "mysql-connector-java" % "5.1.31",
      "com.amazonaws" % "aws-java-sdk" % "1.3.11",
      "net.sf.opencsv" % "opencsv" % "2.1",
      "com.loicdescotte.coffeebean" %% "html5tags" % "1.2.1",
      "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7",
      "org.avaje.ebeanorm" % "avaje-ebeanorm" % "3.3.3",
      "com.typesafe.play" % "play-ebean-33-compat" % "1.0.0",
      javaCore,
//      "play4jpa" %% "play4jpa" % "0.1-SNAPSHOT"
      javaJpa,
      javaJdbc,
      javaEbean,
      javaWs,
      cache
)

resolvers ++= Seq(
  "Apache" at "http://repo1.maven.org/maven2/",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
  "play-authenticate (release)" at "http://joscha.github.io/play-authenticate/repo/releases/",
  "play-authenticate (snapshot)" at "http://joscha.github.io/play-authenticate/repo/snapshots/",
  "play-easymail (release)" at "http://joscha.github.io/play-easymail/repo/releases/",
  "play-easymail (snapshot)" at "http://joscha.github.io/play-easymail/repo/snapshots/",
  "Objectify Play Repository (release)" at "http://schaloner.github.com/releases/",
  "Objectify Play Repository (snapshot)"at "http://schaloner.github.com/snapshots/",
  "maven-central (release)" at "http://search.maven.org/"
  //"github repo for html5tags" at "http://loicdescotte.github.io/Play2-HTML5Tags/releases/"
)

resolvers += Resolver.url("github repo for html5tags", url("http://loicdescotte.github.io/Play2-HTML5Tags/releases/"))(Resolver.ivyStylePatterns)


unmanagedResourceDirectories in Compile += file("opt/logging")

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava)
  .settings(
    libraryDependencies ++= appDependencies
)
