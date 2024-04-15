package httpclient

import annotation.*
import java.net.*, http.*
import zio.*, ZIO.*

opaque type ResponseBody = String
opaque type HttpErr = String
opaque type Url = String

trait HttpClient:
  def get(url: Url): IO[HttpErr, ResponseBody]
end HttpClient

class HttpClientImpl extends HttpClient:
  def get(url: Url): IO[HttpErr, ResponseBody] =
    for
      client <- attempt(HttpClient.newHttpClient.nn).orDie
      uri <- attempt(URI.create(url.toString)).orDie
      request <- attempt(HttpRequest.newBuilder(uri).nn.build).orDie
      ofString <- succeed(HttpResponse.BodyHandlers.ofString)
      response <- attempt(client.send(request, ofString).nn).orDie
      code <- succeed(response.statusCode)
      body <- succeed(response.body.nn)
      res <-
        cond(
          code == 200
        , ResponseBody(body)
        , HttpErr(code, body)
        )
    yield res
end HttpClientImpl

val layer: Layer[Nothing, HttpClient] =
  ZLayer.succeed:
    HttpClientImpl()

object ResponseBody:
  def apply(x: String): ResponseBody = x

extension (x: ResponseBody)
  @targetName("toStringResponseBody")
  def toString: String = x

object HttpErr:
  def apply(code: Int, body: String): HttpErr = s"$code: ${replaceWhitespaces(body)}"

  private def replaceWhitespaces(x: String): String =
    x.replaceAll(raw"\s+", " ").nn
end HttpErr

object Url:
  def apply(x: String): Url = x

extension (x: Url)
  @targetName("toStringUrl")
  def toString: String = x
