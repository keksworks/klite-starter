package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.TestData.emailCode
import klite.Email
import klite.jdbc.nowSec
import org.junit.jupiter.api.Test

class EmailCodeTest {
  @Test fun `generates 6-digit codes`() {
    repeat(100) { expect(EmailCode.generate().matches("\\d{6}".toRegex())).toEqual(true) }
  }

  @Test fun `matches only the hashed code of the same email`() {
    expect(emailCode.matches("123456")).toEqual(true)
    expect(emailCode.matches("123457")).toEqual(false)
    expect(emailCode.copy(email = Email("other@example.com")).matches("123456")).toEqual(false)
  }

  @Test fun isUsable() {
    expect(emailCode.isUsable).toEqual(true)
    expect(emailCode.copy(attempts = EmailCode.maxAttempts - 1).isUsable).toEqual(true)
    expect(emailCode.copy(attempts = EmailCode.maxAttempts).isUsable).toEqual(false)
    expect(emailCode.copy(usedAt = nowSec()).isUsable).toEqual(false)
    expect(emailCode.copy(sentAt = nowSec() - EmailCode.validFor).isUsable).toEqual(false)
  }
}
