package fr.davit.akka.http.prometheus.scaladsl.server

import akka.http.scaladsl.server._
import fr.davit.akka.http.prometheus.scaladsl.marshalling.PrometheusMarshallers

trait HttpMetricsDirectives {

  import Directives._
  import PrometheusMarshallers._

  private def recordMetrics(exports: HttpMetricsExports,
                            startTs: Long,
                            isFailure: Boolean): Unit = {
    val msElapsed = System.currentTimeMillis() - startTs

    exports.requestsActive.dec()
    if (isFailure) exports.requestsErrorCounter.inc()
    exports.requestsCounter.inc()
    exports.requestsDuration.observe(msElapsed.toDouble)
  }

  /**
    * Wrap your api with this directive to get metrics
    * on the HTTP requests: count and latency
    */
  def withMetrics(exports: HttpMetricsExports): Directive0 = Directive { inner =>
    ctx =>
      val startTs = System.currentTimeMillis()
      exports.requestsActive.inc()

      import ctx.executionContext
      val routeResultFuture = try {
        inner(())(ctx)
      } catch {
        case e: Throwable =>
          recordMetrics(exports, startTs, isFailure = true)
          throw e
      }

      routeResultFuture.onComplete { routeResult =>
        recordMetrics(exports, startTs, routeResult.isFailure)
      }
      routeResultFuture
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
