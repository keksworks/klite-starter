package auth

import klite.info
import klite.jdbc.nowSec
import klite.jobs.Job
import klite.logger

/** Deletes email codes that are too old to be used or counted for rate limiting */
class EmailCodeCleanupJob(private val repository: EmailCodeRepository): Job {
  override fun run() {
    val deleted = repository.deleteSentBefore(nowSec() - EmailCode.maxAge)
    if (deleted > 0) logger().info("Deleted $deleted old email codes")
  }
}
