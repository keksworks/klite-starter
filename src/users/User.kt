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
  val name: String,
  override val email: Email,
  val avatarUrl: URI? = null,
  val lastLoginAt: Instant? = null,
  val lang: String = "en",
  val role: Role = Role.USER,
  override var updatedAt: Instant? = null,
  val createdAt: Instant = nowSec(),
  override val id: Id<User> = Id(),
): Entity<User>, OAuthUser, UpdatableEntity {
  override val firstName get() = name.substringBefore(" ")
  override val lastName get() = name.substringAfter(" ")
}
