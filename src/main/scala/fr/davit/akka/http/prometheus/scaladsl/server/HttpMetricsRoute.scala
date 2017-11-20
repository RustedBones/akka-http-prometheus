package fr.davit.akka.http.prometheus.scaladsl.server

import akka.NotUsed
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route, RoutingLog}
import akka.http.scaladsl.settings.{ParserSettings, RoutingSettings}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import fr.davit.akka.http.prometheus.scaladsl.server.settings.HttpMetricsSettings
import fr.davit.akka.http.prometheus.scaladsl.server.HttpMetricsDirectives._

import scala.concurrent.ExecutionContextExecutor

object HttpMetricsRoute {

  implicit def apply(route: Route): HttpMetricsRoute = new HttpMetricsRoute(route)

}

/**
  * Typeclass to add the metrics capabilities to a route
  *
  */
class HttpMetricsRoute private(route: Route) {


  def withMetricsHandler(implicit settings: HttpMetricsSettings,
                         routingSettings: RoutingSettings,
                         parserSettings: ParserSettings,
                         materializer: Materializer,
                         routingLog: RoutingLog,
                         executionContext: ExecutionContextExecutor = null,
                         rejectionHandler: RejectionHandler = RejectionHandler.default,
                         exceptionHandler: ExceptionHandler = null)
  : Flow[HttpRequest, HttpResponse, NotUsed] = {

    // wrap the route with the metrics computation
    // and serve the metrics data on the wanted path
    val metricsRoute = withMetrics(settings.exports) {
      route ~ path(settings.resourcePath) {
        get {
          collectMetrics(settings.exports)
        }
      }
    }

    // As the number of bytes served by the server can't be computed in the directives
    // (rejections and errors are handles outside the route), this metric is computed
    // when the route is sealed and transformed into a flow
    Route.handlerFlow(metricsRoute).map { response =>
      val length = response.entity.contentLengthOption.getOrElse(0l)
      settings.exports.responsesBytes.observe(length.toDouble)
      response
    }
  }
}
