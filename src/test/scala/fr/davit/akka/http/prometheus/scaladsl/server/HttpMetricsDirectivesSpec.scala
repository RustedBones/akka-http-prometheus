package fr.davit.akka.http.prometheus.scaladsl.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.prometheus.client.CollectorRegistry
import org.scalatest.{Matchers, WordSpec}

class HttpMetricsDirectivesSpec extends WordSpec with Matchers with ScalatestRouteTest {

  import Directives._
  import HttpMetricsDirectives._


  trait Fixture {

    case class TestExports(registry: CollectorRegistry) extends HttpMetricsExports

    val exports: HttpMetricsExports = TestExports(new CollectorRegistry())

    def getMetricSample(sample: String): Double = exports.registry.getSampleValue(sample)

    def innerRoute: Route

    val route: Route = withMetrics(exports) {
      innerRoute
    }
  }

  "The 'HttpMetricsDirectives'" should {
    "set the metrics in case of success" in new Fixture {

      override val innerRoute: Route = complete(StatusCodes.OK)

      Get() ~> route ~> check {
        getMetricSample(HttpMetricsExports.RequestsActiveSample) shouldBe 0.0
        getMetricSample(HttpMetricsExports.RequestsCounterSample) shouldBe 1.0
        getMetricSample(s"${HttpMetricsExports.RequestsDurationSample}_count") shouldBe 1.0
      }
    }

    "set the metrics in case of rejection" in new Fixture {

      override val innerRoute: Route = reject

      Get() ~> route ~> check {
        getMetricSample(HttpMetricsExports.RequestsActiveSample) shouldBe 0.0
        getMetricSample(HttpMetricsExports.RequestsCounterSample) shouldBe 1.0
        getMetricSample(s"${HttpMetricsExports.RequestsDurationSample}_count") shouldBe 1.0
      }
    }

    "set the metrics in case of exception" in new Fixture {
      override val innerRoute: Route = _ => throw new Exception("BOOM!")

      Get() ~> route ~> check {
        getMetricSample(HttpMetricsExports.RequestsActiveSample) shouldBe 0.0
        getMetricSample(HttpMetricsExports.RequestsCounterSample) shouldBe 1.0
        getMetricSample(HttpMetricsExports.RequestsErrorCounterSample) shouldBe 1.0
        getMetricSample(s"${HttpMetricsExports.RequestsDurationSample}_count") shouldBe 1.0
      }
    }

    "set the metrics in case of error" in new Fixture {
      override val innerRoute: Route = failWith(new Exception("BOOM!"))

      Get() ~> route ~> check {
        getMetricSample(HttpMetricsExports.RequestsActiveSample) shouldBe 0.0
        getMetricSample(HttpMetricsExports.RequestsCounterSample) shouldBe 1.0
        getMetricSample(HttpMetricsExports.RequestsErrorCounterSample) shouldBe 1.0
        getMetricSample(s"${HttpMetricsExports.RequestsDurationSample}_count") shouldBe 1.0
      }
    }

    "expose the metrics in prometheus text format" in new Fixture {
      override val innerRoute: Route = pathPrefix("api") {
        complete(StatusCodes.OK)
      } ~ path("error") {
        throw new Exception("BOOM!")
      } ~ path("metrics") {
        collectMetrics(exports)
      }

      Get("/api") ~> route // trigger a success
      Get("/error") ~> route // trigger an error
      Get("/metrics") ~> route ~> check {
        val metrics = responseAs[String]
        metrics should include(HttpMetricsExports.RequestsActiveSample)
        metrics should include(HttpMetricsExports.RequestsCounterSample)
        metrics should include(HttpMetricsExports.RequestsErrorCounterSample)
        metrics should include(HttpMetricsExports.RequestsDurationSample)
      }
    }
  }
}
