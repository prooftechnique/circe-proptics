import sbt.Keys._
import Settings._

inThisBuild(
  List(
    organization := "ch.proofte",
    homepage     := Some(url("https://codeberg.org/prooftechnique/circe-proptics")),
    licenses := Seq(
      "Apache-2.0" -> url("https://spdx.org/licenses/Apache-2.0.html")
    ),
    developers := List(
      Developer(
        "prooftechnique",
        "Jack Henahan",
        "root@proofte.ch",
        url("https://codeberg.org/prooftechnique")
      )
    ),
    latestVersion := {
      val snapshot = (ThisBuild / isSnapshot).value
      val stable   = (ThisBuild / isVersionStable).value

      if (!snapshot && stable) {
        (ThisBuild / version).value
      } else {
        (ThisBuild / previousStableVersion).value.getOrElse("0.0.0")
      }
    }
  )
)

lazy val optics = project
  .in(file("."))
  .settings(
    moduleName := "circe-proptics-project",
    noPublishSettings,
    stdSettings,
    welcomeMessage
  )
  .aggregate(opticsJVM, opticsJS)
  .dependsOn(opticsJVM, opticsJS)
  .enablePlugins(ScalaJSPlugin)

lazy val opticsJVM = project
  .in(file(".opticsJVM"))
  .settings(noPublishSettings)
  .aggregate(library.jvm)
  .dependsOn(library.jvm)

lazy val opticsJS = project
  .in(file(".opticsJS"))
  .settings(noPublishSettings)
  .aggregate(library.js)
  .dependsOn(library.js)

lazy val library = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name       := "Circe Proptics",
    moduleName := "circe-proptics",
    stdSettings,
    libraryDependencies ++= Seq(
      "io.github.sagifogel" %%% "proptics-core"    % "0.5.2",
      "io.github.sagifogel" %%% "proptics-law"     % "0.5.2"    % Test,
      "org.typelevel"       %%% "cats-core"        % "2.9.0",
      "org.typelevel"       %%% "cats-free"        % "2.9.0",
      "io.circe"            %%% "circe-core"       % "0.14.3",
      "io.circe"            %%% "circe-testing"    % "0.14.3"   % Test,
      "io.circe"            %%% "circe-parser"     % "0.14.3",
      "org.scalameta"       %%% "munit"            % "1.0.0-M7" % Test,
      "org.scalameta"       %%% "munit-scalacheck" % "1.0.0-M7" % Test,
      "org.typelevel"       %%% "discipline-munit" % "2.0.0-M3" % Test
    ),
    crossProjectSettings
  )
//    additionalDependencies

lazy val libraryJS  = library.js
lazy val libraryJVM = library.jvm

lazy val docs = project
  .in(file("docs"))
  .dependsOn(library.jvm)
  .settings(
    moduleName := "circe-proptics-docs",
    noPublishSettings,
    stdSettings,
    mdocSettings(library.jvm)
  )
//    additionalDependencies
  .enablePlugins(
    BuildInfoPlugin,
    DocusaurusPlugin,
    MdocPlugin,
    ScalaUnidocPlugin
  )
