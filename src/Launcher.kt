import auth.*
import db.initDB
import klite.*
import klite.RequestMethod.GET
import klite.annotations.annotated
import klite.email.FakeEmailSender
import klite.email.SmtpEmailSender
import klite.http.httpClient
import klite.jdbc.DBModule
import klite.jdbc.RequestTransactionHandler
import klite.jobs.JobRunner
import klite.json.JsonBody
import klite.oauth.AuthRoutes
import klite.oauth.GoogleOAuthClient
import klite.oauth.OAuthRoutes
import klite.oauth.OAuthUserProvider
import todos.TodoRoutes
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.nio.file.Path
import java.time.LocalTime
import javax.sql.DataSource
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes

fun main() {
  Config.useEnvFile()

  val db = initDB()
  Server(
    sessionStore = UserSessionStore(db),
    httpExchangeCreator = ::XForwardedHttpExchange
  ).apply {
    use(DBModule(db))
    use<RequestTransactionHandler>()

    use(JobRunner(require<DataSource>(), requestIdGenerator).apply {
      scheduleDaily(require<EmailCodeCleanupJob>(), LocalTime.of(4, 0))
    })

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

      annotated<TodoRoutes>("/todos")

      rateLimit(10, 1.minutes)
      annotated<AuthRoutes>(annotations = listOf(Public()))
      annotated<EmailAuthRoutes>("/auth", listOf(Public()))
    }

    start()

    if (getRuntimeMXBean().inputArguments.any { it.startsWith("-XX:AOTCacheOutput") })
      exitProcess(0)
  }
}
