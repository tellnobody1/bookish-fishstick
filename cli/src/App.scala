package cli

import appservice.*
import httpclient.*
import zio.*, Console.*

object App extends ZIOAppDefault:
  def run =
    val io: ZIO[AppService, Err, Unit] =
      for
        ipAddress <- publicIpAddress
        _ <- printLine(ipAddress).orDie
      yield ()
    io.provide(
      appservice.layer
    , httpclient.layer
    )
end App
