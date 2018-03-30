// General info
val username = "RustedBones"
val repo = "akka-http-prometheus"

// Library versions
lazy val akkaHttpVersion    = "10.1.0"
lazy val akkaStreamVersion  = "2.5.11"
lazy val prometheusVersion  = "0.3.0"
lazy val scalaTestVersion   = "3.0.5"

// akka-http-prometheus
lazy val `akka-http-prometheus` = (project in file(".")).
  settings(
    organization := "fr.davit",
    version := "0.1.1",
    scalaVersion := "2.12.5",
    crossScalaVersions := Seq("2.11.12", "2.12.4"),
    crossVersion := CrossVersion.binary,

    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.8",
      "-encoding", "utf8",
      "-Xfuture",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused",
      "-feature",
      "-language:implicitConversions"
    ),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "io.prometheus"     %  "simpleclient_common"  % prometheusVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaStreamVersion   % Provided,
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion     % Test,
      "org.scalatest"     %% "scalatest"            % scalaTestVersion    % Test
    ),

    homepage := Some(url(s"https://github.com/$username/$repo")),
    licenses += "APACHE" -> url(s"https://github.com/$username/$repo/blob/master/LICENSE"),
    scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
    developers := List(
      Developer(id=s"$username", name="Michel Davit", email="michel@davit.fr", url=url(s"https://github.com/$username"))
    ),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
    credentials ++= (for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )