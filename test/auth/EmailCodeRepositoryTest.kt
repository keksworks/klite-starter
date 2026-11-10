package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.Id
import db.TestData.emailCode
import klite.Email
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit.HOURS

class EmailCodeRepositoryTest: DBTest() {
  val repository = EmailCodeRepository(db)

  @Test fun `save & load`() {
    repository.save(emailCode)
    expect(repository.get(emailCode.id)).toEqual(emailCode)
    repository.save(emailCode.copy(attempts = 1))
    expect(repository.get(emailCode.id, forUpdate = true)).toEqual(emailCode.copy(attempts = 1))
  }

  @Test fun countSentSince() {
    val since = emailCode.sentAt.minus(1, HOURS)
    repository.save(emailCode)
    repository.save(emailCode.copy(id = Id(), ip = "10.0.0.1"))
    repository.save(emailCode.copy(id = Id(), sentAt = since.minusSeconds(1)))
    repository.save(emailCode.copy(id = Id(), email = Email("other@example.com")))

    expect(repository.countSentSince(since, EmailCode::email to emailCode.email)).toEqual(2)
    expect(repository.countSentSince(since, EmailCode::ip to emailCode.ip)).toEqual(2)
    expect(repository.countSentSince(since, EmailCode::ip to "10.0.0.2")).toEqual(0)
  }

  @Test fun deleteSentBefore() {
    val cutoff = emailCode.sentAt.minus(1, HOURS)
    repository.save(emailCode.copy(sentAt = cutoff))
    repository.save(emailCode.copy(id = Id(), sentAt = cutoff.minusSeconds(1)))
    repository.save(emailCode.copy(id = Id(), sentAt = cutoff.plusSeconds(1)))

    expect(repository.deleteSentBefore(cutoff)).toEqual(1)
    expect(repository.count()).toEqual(2)
  }
}
