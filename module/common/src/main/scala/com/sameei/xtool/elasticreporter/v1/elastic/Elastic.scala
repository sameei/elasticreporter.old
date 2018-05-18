package com.sameei.xtool.elasticreporter.v1.elastic

import java.io._

import org.slf4j.LoggerFactory
import java.net.{HttpURLConnection, URL}

import com.sameei.xtool.elasticreporter.v1.common

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

case class Elastic(name: String, host: String) {

    private val logger = LoggerFactory.getLogger(name)

    logger.debug(s"Init ..., Class: ${getClass.getName}")

    Try {
        require(host != null, "'host' can't be null!")
        require(!host.isEmpty, "'host' can't be empty")
        new URL(host).asInstanceOf[HttpURLConnection]
    }.map { http =>
        http.connect()
        val body = readStream(http.getInputStream)
        (http, body)
    } match {
        case Success((http, body)) =>
            logger.info(s"Init, Host: ${host}, Elastic: ${body}")
        case Failure(cause) =>
            logger.warn(s"Init, Host: ${host}, Failure: ${cause.getMessage}", cause)
            throw Elastic.InitException("Init Failure", host, Option(cause))
    }

    def put(report: Reporter.Report): Try[Unit] = Try {

        val url = s"${host}/${report.index}/doc/${report.doc}"

        val http = new URL(url).asInstanceOf[HttpURLConnection]

        http setUseCaches false
        http setDoOutput true
        http setRequestMethod "PUT"
        http setRequestProperty("Content-Type", "application/json")

        val output = new DataOutputStream(http.getOutputStream);
        val bytes = report.body.getBytes()

        output.write(bytes)

        output.flush()
        output.close()

        http.connect()

        http.getResponseCode match {
            case 200 | 201 =>
                if (logger.isDebugEnabled()) {
                    val body = readStream(http.getInputStream)
                    logger.debug(s"PUT, URL: ${url}, Req(${bytes.length}), Res(${body.getBytes.length})")
                    logger.trace(s"PUT, URL: ${url}, Res(${body.getBytes.length}): ${body}")
                }
            case unexp =>
                if (logger.isWarnEnabled()) {
                    val body = readStream(http.getErrorStream)
                    logger.warn(s"PUT, URL: ${url}, Req(${bytes.length}): ${report.body}, Res(${body.getBytes.length}): ${body}")
                }
                throw Elastic.PutException(s"Put Failure, Unexpected ResponseCode: ${unexp}", report)
        }

    }

    protected def readStream(input: InputStream): String = {
        val in = new BufferedReader(new InputStreamReader(input))
        val buf = new StringBuilder

        var line: String = in.readLine()
        while(line != null) {
            buf.append(line)
            line = in.readLine()
        }

        buf.result()
    }

}

object Elastic {

    case class PutException(
        desc: String,
        report: Reporter.Report
    ) extends RuntimeException(desc) with common.data.BaseException

    case class InitException(
        desc: String, host: String, cause: Option[Throwable]
    ) extends RuntimeException(desc, cause.orNull) with common.data.BaseException
}
