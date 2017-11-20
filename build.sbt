import ReleaseTransformations._

// General info
val username = "RustedBones"
val repo = "akka-http-prometheus"

// Scala versions
lazy val akkaHttpVersion = "10.0.11"
lazy val prometheusVersion = "0.1.0"
lazy val scalaTestVersion = "3.0.4"

// akka-http-prometheus
lazy val `akka-http-prometheus` = (project in file(".")).
  settings(
    organization := "fr.davit",
    scalaVersion := "2.12.4",
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
      "-Ywarn-unused"
    ),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "io.prometheus" % "simpleclient_common" % prometheusVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),

    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      //runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommand("publishSigned"),
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    ),

    homepage := Some(url(s"https://github.com/$username/$repo")),
    licenses += "APACHE" -> url(s"https://github.com/$username/$repo/blob/master/LICENSE"),
    scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
    apiURL := Some(url(s"https://$username.github.io/$repo/latest/api/")),
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
    credentials ++= (for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )