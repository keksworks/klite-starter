package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.emailCode
import db.TestData.user
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import klite.BadRequestException
import klite.Email
import klite.Session
import klite.StatusCode.Companion.TooManyRequests
import klite.StatusCodeException
import klite.jdbc.nowSec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.User

class EmailAuthRoutesTest: BaseMocks() {
  val routes = EmailAuthRoutes(userRepository, emailCodeRepository, emailSender)
  val email = Email("jane.doe@example.com")
  val session = Session()
  val request = EmailCodeRequest(user.email, "123456")

  init {
    every { exchange.session } returns session
    every { exchange.remoteAddress } returns emailCode.ip
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode
    every { userRepository.by(User::email to user.email) } returns user
    session["emailCodeId"] = emailCode.id.toString()
  }

  @Test fun `sendCode stores hash of emailed 6-digit code`() {
    val saved = slot<EmailCode>()
    every { emailCodeRepository.save(capture(saved)) } returns 1
    routes.sendCode(EmailRequest(email), exchange)

    expect(saved.captured).toEqual(EmailCode(email, saved.captured.codeHash, emailCode.ip, sentAt = saved.captured.sentAt, id = saved.captured.id))
    expect(session["emailCodeId"]).toEqual(saved.captured.id.toString())
    verify { emailSender.send(email, match { (it.substitutions["code"] as String).length == 6 && saved.captured.matches(it.substitutions["code"] as String) }) }
  }

  @Test fun `sendCode limits codes per email`() {
    every { emailCodeRepository.countSentSince(any(), EmailCode::email to email) } returns 5
    expect(assertThrows<StatusCodeException> { routes.sendCode(EmailRequest(email), exchange) }.statusCode).toEqual(TooManyRequests)
    verify(exactly = 0) { emailSender.send(any(), any<klite.email.EmailContent>()) }
  }

  @Test fun `sendCode limits codes per ip`() {
    every { emailCodeRepository.countSentSince(any(), EmailCode::ip to emailCode.ip) } returns 20
    expect(assertThrows<StatusCodeException> { routes.sendCode(EmailRequest(email), exchange) }.statusCode).toEqual(TooManyRequests)
    verify(exactly = 0) { emailCodeRepository.save(any()) }
  }

  @Test fun `loginWithCode logs in existing user and uses up the code`() {
    val result = routes.loginWithCode(request, exchange)
    expect(result).toEqual(user.copy(lastLoginAt = result.lastLoginAt))
    verify {
      emailCodeRepository.save(match { it == emailCode.copy(attempts = 1, usedAt = it.usedAt) && it.usedAt != null })
      userRepository.save(result)
    }
    expect(session["userId"]).toEqual(user.id.toString())
  }

  @Test fun `loginWithCode creates user when missing`() {
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode.copy(email = email, codeHash = EmailCode.hash(email, "123456"))
    every { userRepository.by(User::email to email) } returns null

    val result = routes.loginWithCode(request.copy(email = email), exchange)
    expect(result).toEqual(User("Jane.doe", "", email).copy(id = result.id, lastLoginAt = result.lastLoginAt))
    verify { userRepository.save(result) }
    expect(session["userId"]).toEqual(result.id.toString())
  }

  @Test fun `loginWithCode counts wrong code as an attempt`() {
    assertThrows<BadRequestException> { routes.loginWithCode(request.copy(code = "000000"), exchange) }
    verify { emailCodeRepository.save(emailCode.copy(attempts = 1)) }
    expectNoLogin()
  }

  @Test fun `loginWithCode rejects correct code after too many attempts`() {
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode.copy(attempts = EmailCode.maxAttempts)
    assertThrows<BadRequestException> { routes.loginWithCode(request, exchange) }
    expectNoLogin()
  }

  @Test fun `loginWithCode rejects used code`() {
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode.copy(usedAt = nowSec())
    assertThrows<BadRequestException> { routes.loginWithCode(request, exchange) }
    expectNoLogin()
  }

  @Test fun `loginWithCode rejects expired code`() {
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode.copy(sentAt = nowSec() - EmailCode.validFor)
    assertThrows<BadRequestException> { routes.loginWithCode(request, exchange) }
    expectNoLogin()
  }

  @Test fun `loginWithCode rejects code sent to another email`() {
    every { emailCodeRepository.get(emailCode.id, forUpdate = true) } returns emailCode.copy(email = email)
    assertThrows<BadRequestException> { routes.loginWithCode(request, exchange) }
    expectNoLogin()
  }

  @Test fun `loginWithCode rejects session without requested code`() {
    session.clear()
    assertThrows<BadRequestException> { routes.loginWithCode(request, exchange) }
    expectNoLogin()
  }

  private fun expectNoLogin() {
    verify(exactly = 0) { userRepository.save(any()) }
    expect(session["userId"]).toEqual(null)
  }
}
