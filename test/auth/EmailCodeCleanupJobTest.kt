package auth

import db.BaseMocks
import io.mockk.every
import io.mockk.verify
import klite.jdbc.nowSec
import org.junit.jupiter.api.Test

class EmailCodeCleanupJobTest: BaseMocks() {
  private val job = EmailCodeCleanupJob(emailCodeRepository)

  @Test fun `deletes codes older than max age`() {
    val cutoff = nowSec() - EmailCode.maxAge
    every { emailCodeRepository.deleteSentBefore(any()) } returns 2
    job.run()
    verify { emailCodeRepository.deleteSentBefore(match { it in cutoff..nowSec() - EmailCode.maxAge }) }
  }
}
