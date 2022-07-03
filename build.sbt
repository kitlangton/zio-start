name        := "zio-start"
description := "A wizard for generating new ZIO applications."
version     := "0.0.1"

val animusVersion    = "0.1.15"
val boopickleVersion = "1.4.0"
val laminarVersion   = "0.14.0"
val laminextVersion  = "0.13.6"
val quillZioVersion  = "3.10.0"
val scalaMetaVersion = "4.5.9"

Global / onChangedBuildSource := ReloadOnSourceChanges

val sharedSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  libraryDependencies ++= Seq(
    "io.suzaku" %%% "boopickle" % boopickleVersion
  ),
  scalacOptions ++= Seq("-Ymacro-annotations", "-Xfatal-warnings", "-deprecation"),
  scalaVersion   := "2.13.8",
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
)

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "io.github.kitlangton" %%% "animus"          % animusVersion,
      "com.raquo"            %%% "laminar"         % laminarVersion,
      "io.github.cquiroz"    %%% "scala-java-time" % "2.4.0",
      "io.laminext"          %%% "websocket"       % laminextVersion,
      "com.raquo"            %%% "waypoint"        % "0.5.0",
      "org.scalameta"        %%% "scalameta"       % scalaMetaVersion
    )
  )
  .settings(sharedSettings)
