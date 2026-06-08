import auth.AccessChecker
import db.initDB
import klite.*
import klite.annotations.annotated
import klite.jdbc.RequestTransactionHandler
import klite.json.JsonBody
import todos.TodoRoutes
import java.nio.file.Path
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.days

fun main() {
  if (!Config.isProd) Config.useEnvFile()

  Server(
    sessionStore = CookieSessionStore(cookie = Cookie("S", "", httpOnly = true, secure = Config.isProd, maxAge = 365.days)),
    httpExchangeCreator = XForwardedHttpExchange::class.primaryConstructor!!
  ).apply {
    initDB()
    use<RequestTransactionHandler>()

    assets("/", AssetsHandler(Path.of("ui/public"), useIndexForUnknownPaths = true))

    context("/api") {
      useOnly<JsonBody>()

      post("/js-error") { logger("js-error").error(rawBody) }

      before<AccessChecker>()

      annotated<TodoRoutes>("/todos")
    }

    start()
  }
}
