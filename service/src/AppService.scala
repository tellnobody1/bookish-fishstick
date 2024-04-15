package appservice

import httpclient.*
import zio.*, ZIO.*, json.*

opaque type IpAddress = String
opaque type ApiUrl = Url
opaque type JsonErr = String
type Err = HttpErr | JsonErr
case class ApiBody(ip: IpAddress)

trait AppService:
  def publicIpAddress: IO[Err, IpAddress]
end AppService

class AppServiceImpl(apiUrl: ApiUrl, httpClient: HttpClient) extends AppService:
  def publicIpAddress: IO[Err, IpAddress] =
    for
      body <- httpClient.get(apiUrl.toUrl)
      ipAddress <- fromEither(body.toString.fromJson[ApiBody]).mapError(JsonErr.apply)
    yield ipAddress.ip

def publicIpAddress: ZIO[AppService, Err, IpAddress] =
  serviceWithZIO[AppService](_.publicIpAddress)

val layer: ZLayer[HttpClient, Nothing, AppService] =
  ZLayer:
    for
      apiUrl <- succeed("https://api.ipify.org/?format=json")
      httpClient <- service[HttpClient]
    yield AppServiceImpl(ApiUrl(Url(apiUrl)), httpClient)

object ApiUrl:
  def apply(x: Url): ApiUrl = x

object JsonErr:
  def apply(x: String): JsonErr = x

extension (x: ApiUrl)
  def toUrl: Url = x

given JsonDecoder[ApiBody] =
  given JsonDecoder[IpAddress] = JsonDecoder[String].map(identity)
  DeriveJsonDecoder.gen
