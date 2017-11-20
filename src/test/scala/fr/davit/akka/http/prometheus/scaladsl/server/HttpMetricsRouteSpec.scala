package fr.davit.akka.http.prometheus.scaladsl.server

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import fr.davit.akka.http.prometheus.scaladsl.server.settings.HttpMetricsSettings
import org.scalatest.{Matchers, WordSpecLike}

class HttpMetricsRouteSpec extends TestKit(ActorSystem("HttpMetricsRouteSpec")) with WordSpecLike with Matchers {

  import Directives._
  import HttpMetricsRoute._

  implicit val metricsSettings: HttpMetricsSettings = HttpMetricsSettings()
  implicit val _ = system
  implicit val materializer = ActorMaterializer()

  "The 'HttpMetricsRoute'" should {

    "compute the number of bytes handled by the server" in {
      val content = "content"
      val route = complete(StatusCodes.OK -> content)
      val flow = route.withMetricsHandler

      val (pub, sub) = TestSource.probe[HttpRequest]
        .via(flow)
        .toMat(TestSink.probe[HttpResponse])(Keep.both)
        .run()

      sub.request(1)
      pub.sendNext(HttpRequest())
      sub.expectNext()

      metricsSettings.exports.registry.getSampleValue(HttpMetricsExports.ResponseBytesSample + "_count") shouldBe 1.0
      metricsSettings.exports.registry.getSampleValue(HttpMetricsExports.ResponseBytesSample + "_sum") shouldBe content.size
    }
  }

}
