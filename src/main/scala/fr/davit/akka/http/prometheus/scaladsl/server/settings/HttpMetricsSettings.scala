package fr.davit.akka.http.prometheus.scaladsl.server.settings

import fr.davit.akka.http.prometheus.scaladsl.server.{DefaultHttpMetricsExports, HttpMetricsExports}

case class HttpMetricsSettings(resourcePath: String = "metrics",
                               exports: HttpMetricsExports = DefaultHttpMetricsExports)
