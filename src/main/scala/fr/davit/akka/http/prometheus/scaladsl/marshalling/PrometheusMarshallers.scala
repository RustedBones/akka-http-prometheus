package fr.davit.akka.http.prometheus.scaladsl.marshalling

import java.io.StringWriter

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, MediaTypes}
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

trait PrometheusMarshallers {

  implicit val collectorToEntityMarshaller: ToEntityMarshaller[CollectorRegistry] = {
    val versionParam = Map("version" -> "0.0.4")
    val contentType = MediaTypes.`text/plain` withParams versionParam withCharset HttpCharsets.`UTF-8`
    Marshaller.withFixedContentType(contentType) { registry =>
      val writer = new StringWriter()
      try {
        TextFormat.write004(writer, registry.metricFamilySamples)
        HttpEntity(writer.toString)
      } finally {
        writer.close()
      }
    }
  }

}

object PrometheusMarshallers extends PrometheusMarshallers
