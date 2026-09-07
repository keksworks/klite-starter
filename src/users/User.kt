package users

import db.Entity
import db.Id
import klite.Email
import klite.jdbc.UpdatableEntity
import klite.jdbc.nowSec
import klite.oauth.OAuthUser
import java.net.URI
import java.time.Instant

enum class Role {
  ADMIN, USER
}

data class User(
  override val firstName: String,
  override val lastName: String,
  override val email: Email,
  val avatarUrl: URI? = null,
  val lastLoginAt: Instant? = null,
  val lang: String = "en",
  val refreshToken: String? = null,
  val role: Role = Role.USER,
  override var updatedAt: Instant? = null,
  val createdAt: Instant = nowSec(),
  override val id: Id<User> = Id(),
): Entity<User>, OAuthUser, UpdatableEntity {
  val name get() = "$firstName $lastName"
}
