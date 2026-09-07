import auth.*
import db.initDB
import klite.*
import klite.annotations.annotated
import klite.http.httpClient
import klite.jdbc.RequestTransactionHandler
import klite.json.JsonBody
import klite.oauth.AuthRoutes
import klite.oauth.GoogleOAuthClient
import klite.oauth.OAuthRoutes
import klite.oauth.OAuthUserProvider
import klite.smtp.FakeEmailSender
import klite.smtp.SmtpEmailSender
import todos.TodoRoutes
import java.nio.file.Path
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.days

fun main() {
  Config.useEnvFile()

  Server(
    sessionStore = CookieSessionStore(cookie = Cookie("S", "", httpOnly = true, secure = Config.isProd, maxAge = 365.days)),
    httpExchangeCreator = XForwardedHttpExchange::class.primaryConstructor!!
  ).apply {
    initDB()
    use<RequestTransactionHandler>()

    assets("/", AssetsHandler(Path.of("ui/public"), useIndexForUnknownPaths = true))

    register(httpClient())
    register(if (Config.isProd) SmtpEmailSender::class else FakeEmailSender::class)

    context("/oauth") {
      register<OAuthUserProvider>(AuthUserProvider::class)
      register<GoogleOAuthClient>()
      annotated<OAuthRoutes>()
    }

    context("/api") {
      useOnly<JsonBody>()

      post("/js-error") { logger("js-error").error(rawBody) }

      if (!Config.isProd) annotated<FakeAuthRoutes>()

      before<AccessChecker>()

      annotated<AuthRoutes>(annotations = listOf(Public()))
      annotated<EmailAuthRoutes>("/auth", listOf(Public()))
      annotated<TodoRoutes>("/todos")
    }

    start()
  }
}
