package auth

import klite.Email
import klite.HttpExchange
import klite.annotations.POST
import klite.email.EmailContent
import klite.email.EmailSender
import klite.i18n.lang
import klite.jdbc.nowSec
import klite.oauth.initSession
import users.User
import users.UserRepository
import users.login

class EmailAuthRoutes(
  private val userRepository: UserRepository,
  private val emailSender: EmailSender,
) {
  @POST("/email") fun sendCode(req: EmailRequest, e: HttpExchange) {
    val code = (1..6).joinToString("") { (0..9).random().toString() }
    e.session["emailCode"] = "${req.email}:$code"
    e.session["codeTime"] = nowSec().epochSecond
    e.session["codeTries"] = 0
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

  internal fun checkCode(e: HttpExchange, req: EmailCodeRequest) {
    require(nowSec().epochSecond - (e.session["codeTime"]?.toLong() ?: 0) < 10 * 60) { "login.codeExpired" }
    require(e.session["emailCode"] == "${req.email}:${req.code}") { onInvalidCode(e) }
  }

  /** counts failed attempts and resets the code after [maxCodeTries], so it cannot be brute-forced */
  private fun onInvalidCode(e: HttpExchange): String {
    val tries = (e.session["codeTries"]?.toInt() ?: 0) + 1
    if (tries < maxCodeTries) {
      e.session["codeTries"] = tries.toString()
      return "login.invalidCode"
    }
    e.session["emailCode"] = null
    e.session["codeTime"] = null
    e.session["codeTries"] = null
    return "login.tooManyCodeTries"
  }

  private companion object {
    const val maxCodeTries = 3
  }
}

data class EmailRequest(val email: Email)
data class EmailCodeRequest(val email: Email, val code: String)
