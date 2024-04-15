package cli

import zio.*
import zio.test.*, Assertion.*

object CliSpec extends ZIOSpecDefault:
  val ipRegex = raw"\d{1,3}(\.\d{1,3}){3}\n"

  def spec = suite("CliSpec")(
    test("get public ip address"):
      for
        code <- App.run.exit
        output <- TestConsole.output
      yield
        assert(output)(exists(matchesRegex(ipRegex))) &&
        assert(code)(succeeds(equalTo(ExitCode.success)))
  ).provide(
    ZLayer.succeed(ZIOAppArgs(Chunk.empty))
  , Scope.default
  )
