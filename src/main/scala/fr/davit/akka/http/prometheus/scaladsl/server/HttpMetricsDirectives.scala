package fr.davit.akka.http.prometheus.scaladsl.server

import akka.http.scaladsl.server._
import fr.davit.akka.http.prometheus.scaladsl.marshalling.PrometheusMarshallers

trait HttpMetricsDirectives {

  import Directives._
  import PrometheusMarshallers._


  private def exceptionHandler(exports: HttpMetricsExports) = ExceptionHandler {
    case e: Throwable =>
      exports.requestsActive.dec()
      exports.requestsCounter.inc()
      exports.requestsErrorCounter.inc()
      throw e
  }

  /**
    * Wrap your api with this directive to get metrics
    * on the HTTP requests: count and latency
    */
  def withMetrics(exports: HttpMetricsExports): Directive0 = {
    handleExceptions(exceptionHandler(exports)).tflatMap { _ =>
      val start = System.currentTimeMillis()
      exports.requestsActive.inc()

      mapRouteResult { result =>
        val msElapsed = System.currentTimeMillis() - start

        exports.requestsActive.dec()
        exports.requestsCounter.inc()
        exports.requestsDuration.observe(msElapsed.toDouble)
        result
      }
    }
  }

  /**
    * Route to export all metrics registered to the
    * Prometheus collector
    */
  def collectMetrics(exports: HttpMetricsExports): StandardRoute = {
    complete(exports.registry)
  }
}

object HttpMetricsDirectives extends HttpMetricsDirectives
