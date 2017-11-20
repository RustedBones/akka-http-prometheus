package fr.davit.akka.http.prometheus.scaladsl.server

import io.prometheus.client.{CollectorRegistry, Counter, Gauge, Summary}

trait HttpMetricsExports {

  import HttpMetricsExports._

  def registry: CollectorRegistry

  // metrics
  // RequestCounter
  lazy val requestsCounter: Counter = Counter
    .build(RequestsCounterSample, "Total HTTP requests")
    .register(registry)

  // RequestsCounter
  lazy val requestsErrorCounter: Counter = Counter
    .build(RequestsErrorCounterSample, "Total HTTP error requests")
    .register(registry)

  // RequestsActive
  lazy val requestsActive: Gauge = Gauge
    .build(RequestsActiveSample, "Active HTTP requests")
    .register(registry)

  // RequestDuration
  lazy val requestsDuration: Summary = Summary
    .build(RequestsDurationSample, "HTTP request duration")
    .register(registry)

  // ResponseBytes
  lazy val responsesBytes: Summary = Summary
    .build(ResponseBytesSample, "HTTP response size")
    .register(registry)

}

object HttpMetricsExports {
  // samples
  val RequestsCounterSample = "http_requests_total"
  val RequestsErrorCounterSample = "http_requests_error_total"
  val RequestsActiveSample = "http_requests_active"
  val RequestsDurationSample = "http_requests_duration_millis"
  val ResponseBytesSample = "http_responses_bytes_total"

  // labels
  val PathLabel = "path"
  val MethodLabel = "method"
}

trait DefaultHttpMetricsExports extends HttpMetricsExports {
  override val registry: CollectorRegistry = CollectorRegistry.defaultRegistry
}
object DefaultHttpMetricsExports extends DefaultHttpMetricsExports
