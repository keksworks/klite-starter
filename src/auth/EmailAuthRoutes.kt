package auth

import db.Id
import klite.BadRequestException
import klite.Email
import klite.HttpExchange
import klite.StatusCode.Companion.TooManyRequests
import klite.StatusCodeException
import klite.annotations.POST
import klite.email.EmailContent
import klite.email.EmailSender
import klite.i18n.lang
import klite.jdbc.nowSec
import klite.oauth.initSession
import users.User
import users.UserRepository
import users.login
import java.time.temporal.ChronoUnit.HOURS

class EmailAuthRoutes(
  private val userRepository: UserRepository,
  private val emailCodeRepository: EmailCodeRepository,
  private val emailSender: EmailSender,
) {
  private val maxCodesPerHour = 5
  private val maxCodesPerHourFromIp = 20

  @POST("/email") fun sendCode(req: EmailRequest, e: HttpExchange) {
    val since = nowSec().minus(1, HOURS)
    if (emailCodeRepository.countSentSince(since, EmailCode::email to req.email) >= maxCodesPerHour ||
        emailCodeRepository.countSentSince(since, EmailCode::ip to e.remoteAddress) >= maxCodesPerHourFromIp)
      throw StatusCodeException(TooManyRequests, "errors.tooManyCodes")

    val code = EmailCode.generate()
    val emailCode = EmailCode(req.email, EmailCode.hash(req.email, code), e.remoteAddress)
    emailCodeRepository.save(emailCode)
    e.session["emailCodeId"] = emailCode.id.toString()
    emailSender.send(req.email, EmailContent(e.lang, "loginCode", mapOf(
      "code" to code,
      "email" to req.email.value
    )))
  }

  @POST("/email/code") fun loginWithCode(req: EmailCodeRequest, e: HttpExchange): User {
    checkCode(e, req)
    val user = userRepository.login(req.email, req.email.value.substringBefore("@").replaceFirstChar { it.uppercase() }, "")
    e.initSession(user)
    return user
  }

  /** Failures throw StatusCodeException, so that the incremented attempts are still committed */
  internal fun checkCode(e: HttpExchange, req: EmailCodeRequest) {
    val emailCode = e.session["emailCodeId"]?.let { emailCodeRepository.get(Id(it), forUpdate = true) }
    if (emailCode == null || emailCode.email != req.email || !emailCode.isUsable) throw BadRequestException("errors.invalidCode")
    val matches = emailCode.matches(req.code)
    emailCodeRepository.save(emailCode.copy(attempts = emailCode.attempts + 1, usedAt = if (matches) nowSec() else null))
    if (!matches) throw BadRequestException("errors.invalidCode")
  }
}

data class EmailRequest(val email: Email)
data class EmailCodeRequest(val email: Email, val code: String)
