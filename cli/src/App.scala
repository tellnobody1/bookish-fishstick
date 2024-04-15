package cli

import appservice.*
import httpclient.*
import zio.*, Console.*

object App extends ZIOAppDefault:
  def run =
    val io: ZIO[AppService, Err, ExitCode] =
      for
        ipAddress <- publicIpAddress
        _ <- printLine(ipAddress).orDie
      yield ExitCode.success
    io.provide(
      appservice.layer
    , httpclient.layer
    )
end App
