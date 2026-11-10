package auth

import db.CrudRepository
import db.Entity
import db.Id
import klite.Email
import klite.PropValue
import klite.jdbc.delete
import klite.jdbc.gte
import klite.jdbc.lt
import klite.jdbc.nowSec
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import javax.sql.DataSource
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class EmailCodeRepository(db: DataSource): CrudRepository<EmailCode>(db, "email_codes") {
  fun countSentSince(since: Instant, by: PropValue<EmailCode, *>) = count(by, EmailCode::sentAt gte since)
  fun deleteSentBefore(cutoff: Instant) = db.delete(table, EmailCode::sentAt lt cutoff)
}

/** One-time login code sent by email; only the hash is stored, attempts are counted server-side */
data class EmailCode(
  val email: Email,
  val codeHash: String,
  val ip: String,
  val attempts: Int = 0,
  val sentAt: Instant = nowSec(),
  val usedAt: Instant? = null,
  override val id: Id<EmailCode> = Id()
): Entity<EmailCode> {
  companion object {
    const val maxAttempts = 5
    val validFor = 10.minutes.toJavaDuration()

    val maxAge = 1.days.toJavaDuration()
    private val random = SecureRandom()

    fun generate() = "%06d".format(random.nextInt(1_000_000))
    fun hash(email: Email, code: String) = MessageDigest.getInstance("SHA-256").digest("$email:$code".toByteArray()).toHexString()
  }

  val isUsable get() = usedAt == null && attempts < maxAttempts && sentAt + validFor > nowSec()
  fun matches(code: String) = MessageDigest.isEqual(codeHash.toByteArray(), hash(email, code).toByteArray())
}
