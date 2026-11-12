package auth

import klite.Cookie
import klite.HttpExchange
import klite.jdbc.DBSessionStore
import klite.jdbc.create
import klite.jdbc.getJson
import klite.jdbc.query
import users.User
import java.util.*
import javax.sql.DataSource
import kotlin.time.Duration.Companion.days

/**
 * Loads the authenticated user together with the session in a single query, so that [AccessChecker] doesn't need
 * a second roundtrip to the users table. The user is put into the exchange attributes, just like `initSession` does.
 */
class UserSessionStore(db: DataSource): DBSessionStore(db, cookie = Cookie("S", "", httpOnly = true, maxAge = 365.days)) {
  override fun load(id: UUID, exchange: HttpExchange): MutableMap<String, Any>? =
    db.query("select s.params, u.* from $table s left join users u on u.id::text = s.params->>'userId'", listOf("s.id" to id), into = ArrayList(1)) {
      val params = getJson<MutableMap<String, Any>>("params")
      if (getString("id") != null) exchange.user = create<User>()
      params
    }.firstOrNull()
}
