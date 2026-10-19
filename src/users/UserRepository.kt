package users

import db.CrudRepository
import klite.Config
import klite.Email
import klite.jdbc.exec
import klite.jdbc.nowSec
import users.Role.ADMIN
import users.Role.USER
import java.net.URI
import javax.sql.DataSource

class UserRepository(db: DataSource): CrudRepository<User>(db, "users") {
  fun setAppUser(user: User) { db.exec("call set_app_user(?)", user.id) }
}

fun UserRepository.login(email: Email, firstName: String, lastName: String,
                         avatarUrl: URI? = null, refreshToken: String? = null): User {
  val existing = by(User::email to email)
  val user = existing?.copy(lastLoginAt = nowSec(), refreshToken = refreshToken ?: existing.refreshToken)
    ?: User(firstName, lastName, email, avatarUrl, lastLoginAt = nowSec(), refreshToken = refreshToken,
      role = if (email.domain == Config["OWN_DOMAIN"]) ADMIN else USER)
  save(user)
  return user
}
