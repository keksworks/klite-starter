import auth.*
import db.initDB
import klite.*
import klite.RequestMethod.GET
import klite.annotations.annotated
import klite.email.FakeEmailSender
import klite.email.SmtpEmailSender
import klite.http.httpClient
import klite.jdbc.RequestTransactionHandler
import klite.json.JsonBody
import klite.oauth.AuthRoutes
import klite.oauth.GoogleOAuthClient
import klite.oauth.OAuthRoutes
import klite.oauth.OAuthUserProvider
import todos.TodoRoutes
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.days

fun main() {
  Config.useEnvFile()

  Server(
    sessionStore = CookieSessionStore(cookie = Cookie("S", "", httpOnly = true, secure = Config.isProd, maxAge = 365.days)),
    httpExchangeCreator = ::XForwardedHttpExchange
  ).apply {
    initDB()
    use<RequestTransactionHandler>()

    assets("/", AssetsHandler(Path.of("ui/public"), useIndexForUnknownPaths = true))

    register(httpClient())
    register(if (Config.optional("SMTP_HOST") != null) SmtpEmailSender::class else FakeEmailSender::class)

    context("/oauth") {
      register<OAuthUserProvider>(AuthUserProvider::class)
      register<GoogleOAuthClient>()
      annotated<OAuthRoutes>()
    }

    context("/api") {
      useOnly<JsonBody>()

      val version = Config.optional("VERSION") ?: "\$VERSION"
      before { e ->
        if (e.method == GET) e.header("x-api-version", version)
        e.header("Cache-control", "max-age=0, private")
      }

      post("/js-error") { logger("js-error").error(rawBody) }

      if (!Config.isProd) annotated<FakeAuthRoutes>()

      before<AccessChecker>()

      annotated<AuthRoutes>(annotations = listOf(Public()))
      annotated<EmailAuthRoutes>("/auth", listOf(Public()))
      annotated<TodoRoutes>("/todos")
    }

    start()

    if (getRuntimeMXBean().inputArguments.any { it.startsWith("-XX:AOTCacheOutput") })
      exitProcess(0)
  }
}
