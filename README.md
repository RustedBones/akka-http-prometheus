# akka-http-prometheus

[![Build Status](https://travis-ci.org/RustedBones/akka-http-prometheus.svg?branch=master&style=flat)](https://travis-ci.org/RustedBones/akka-http-prometheus)
[![Software License](https://img.shields.io/badge/license-Apache%202-brightgreen.svg?style=flat)](LICENSE)

This library aims to easily expose and collect prometheus formatted metrics in your akka-http server.

For more details about promethus, please see the [official documentation](https://prometheus.io/docs/introduction/overview/)
and the [Java client library](https://github.com/prometheus/client_java)


## Versions

| Version | Release date | Akka Http version | Scala versions      |
| ------- | ------------ | ----------------- | ------------------- |
| `0.1.1` | 2018-03-30   | `10.1.0`          | `2.11.12`, `2.12.5` |
| `0.1.0` | 2018-01-03   | `10.0.11`         | `2.11.12`, `2.12.4` |

The complete list can be found in the [CHANGELOG](CHANGELOG.md) file.

## Getting Akka Http Prometheus

akka-http-prometheus is deployed to Maven Central. Add it to your `build.sbt`:

```scala
libraryDependencies += "fr.davit" %% "akka-http-prometheus" % "0.1.1"
```

**Important**: Since akka-http 10.1.0, akka-stream transitive dependency is marked as provided. You should now explicitly
include it in your build.

> [...] we changed the policy not to depend on akka-stream explicitly anymore but mark it as a provided dependency in our build. 
That means that you will always have to add a manual dependency to akka-stream. Please make sure you have chosen and 
added a dependency to akka-stream when updating to the new version

```scala
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % <version> // Only Akka 2.5 supported
```

For more details, see the akka-http 10.1.x [release notes](https://doc.akka.io/docs/akka-http/current/release-notes/10.1.x.html)

## Quick Start

`akka-http-prometheus` enables you to easily record metrics from an akka-http server into a prometheus registry, 
and expose all the registry's metrics on an HTTP endpoint.


The simplest way to add those capabilities to your server is to import content from the `HttpMetricsRoute` and define the
`HttpMetricsSettings` as implicit. Your route will have the `withMetricsHandler` capability that setup everything:

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import fr.davit.akka.http.prometheus.scaladsl.server.HttpMetricsRoute._
import fr.davit.akka.http.prometheus.scaladsl.server.settings.HttpMetricsSettings


implicit val system = ActorSystem("my-system")
implicit val materializer = ActorMaterializer()
// needed for the future flatMap/onComplete in the end
implicit val executionContext = system.dispatcher
implicit val httpMetricsSettings = HttpMetricsSettings()


val route: Route = ...


Http().bindAndHandle(route.withMetricsHandler, "localhost", 8080)
```

### Settings

`HttpMetricsSettings` allows you to parametrize:

- `resourcePath`: path where the prometheus metrics will be exposed for scrapping (default: `metrics`)
- `exports`:  the `HttpMetricsExports` that contains the prometheus registry used to collect the metrics. 
(default: `DefaultHttpMetricsExport` using the prometheus default registry)


### Using a custom prometheus collector

If your application makes use of a custom `CollectorRegistry`, you can use it in the akka-http-prometheus library by
creating a `HttpMetricsExports` and passing it to the directives:

```scala
import  io.prometheus.client.CollectorRegistry
import fr.davit.akka.http.prometheus.scaladsl.server.HttpMetricsExports._
import fr.davit.akka.http.prometheus.scaladsl.server.settings.HttpMetricsSettings

// the custom prometheus registry that you use in your app
val customCollectorRegistry = new CollectorRegistry()

val httpMetricsExports = new HttpMetricsExports {
  override val registry = customCollectorRegistry
}

implicit val httpMetricsSettings = HttpMetricsSettings(exports = httpMetricsExports)
```

## Directives (advanced)

### collectMetrics

The following snippet shows how to use the `collectMetrics` directive to expose metrics collected into the
prometheus default collector registry on the `/metrics` resource path

```scala
import fr.davit.akka.http.prometheus.scaladsl.server.HttpMetricsDirectives._


val route: Route = path("metrics") {
  get {
    collectMetrics()
  }
}
```

### withMetrics

The `withMetrics` directive allows you collect metrics from your akka-http server. You simply have to wrap your API
by the directive:

```scala
import fr.davit.akka.http.prometheus.scaladsl.server.HttpMetricsDirectives._


val route: Route = withMetrics() {
    ... (your api)
}
```
