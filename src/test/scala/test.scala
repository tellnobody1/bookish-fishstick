package appservice

import httpclient.*
import zio.*, ZIO.*
import zio.test.*, Assertion.*

object AppServiceSpec extends ZIOSpecDefault:
  def spec = suite("AppServiceSpec")(
    test("get and parse response"):
      val appService: Layer[Nothing, AppService] =
        ZLayer.succeed:
          val httpClient =
            new HttpClient:
              def get(url: Url): IO[HttpErr, ResponseBody] = succeed(ResponseBody("""{"ip":"192.168.0.1"}"""))
          AppServiceImpl(ApiUrl(Url("")), httpClient)
      (for
        res <- publicIpAddress
      yield assertTrue(res.toString == "192.168.0.1")).provideLayer(appService)

  , test("handle api error"):
      val appService: Layer[Nothing, AppService] =
        ZLayer.succeed:
          val httpClient =
            new HttpClient:
              def get(url: Url): IO[HttpErr, ResponseBody] = fail(HttpErr(404, "Error\nNot Found"))
          AppServiceImpl(ApiUrl(Url("")), httpClient)
      (for
        res <- publicIpAddress.exit
      yield assert(res)(fails(equalTo(HttpErr(404, "Error Not Found"))))).provideLayer(appService)

  , test("handle json error"):
      val appService: Layer[Nothing, AppService] =
        ZLayer.succeed:
          val httpClient =
            new HttpClient:
              def get(url: Url): IO[HttpErr, ResponseBody] = succeed(ResponseBody("{}"))
          AppServiceImpl(ApiUrl(Url("")), httpClient)
      (for
        res <- publicIpAddress.exit
      yield assert(res)(fails(equalTo(JsonErr(".ip(missing)"))))).provideLayer(appService)
  )
