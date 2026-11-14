package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.Email
import klite.Session
import klite.jdbc.nowSec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.User

class EmailAuthRoutesTest: BaseMocks() {
  val routes = EmailAuthRoutes(userRepository, emailSender)
  val email = Email("jane.doe@example.com")
  val session = Session()
  init { every { exchange.session } returns session }

  @Test fun `sendCode emails a 6-digit code`() {
    session["codeTries"] = 2
    routes.sendCode(EmailRequest(email), exchange)
    val code = session["emailCode"]!!.substringAfter(':')
    expect(code.length).toEqual(6)
    expect(session["codeTime"]).toEqual(nowSec().epochSecond.toString())
    expect(session["codeTries"]).toEqual("0")
    verify { emailSender.send(email, match { it.substitutions["code"] == code }) }
  }

  @Test fun `loginWithCode logs in existing user`() {
    every { userRepository.by(User::email to user.email) } returns user
    session["emailCode"] = "${user.email}:123456"
    session["codeTime"] = nowSec().epochSecond.toString()

    val result = routes.loginWithCode(EmailCodeRequest(user.email, "123456"), exchange)
    expect(result.copy(lastLoginAt = null)).toEqual(user.copy(lastLoginAt = null))
    verify { userRepository.save(match { it.id == user.id && it.lastLoginAt != null }) }
    expect(session["userId"]).toEqual(user.id.toString())
  }

  @Test fun `loginWithCode creates user when missing`() {
    every { userRepository.by(User::email to email) } returns null
    session["emailCode"] = "$email:123456"
    session["codeTime"] = nowSec().epochSecond.toString()

    val result = routes.loginWithCode(EmailCodeRequest(email, "123456"), exchange)
    expect(result).toEqual(User("Jane.doe", "", email).copy(id = result.id, lastLoginAt = result.lastLoginAt))
    verify { userRepository.save(result) }
    expect(session["userId"]).toEqual(result.id.toString())
  }

  @Test fun `loginWithCode rejects wrong code`() {
    session["emailCode"] = "$email:123456"
    session["codeTime"] = nowSec().epochSecond.toString()

    val e = assertThrows<IllegalArgumentException> { routes.loginWithCode(EmailCodeRequest(email, "000000"), exchange) }
    expect(e.message).toEqual("login.invalidCode")
    expect(session["emailCode"]).toEqual("$email:123456")
    expect(session["codeTries"]).toEqual("1")
  }

  @Test fun `loginWithCode resets code after max tries`() {
    session["emailCode"] = "$email:123456"
    session["codeTime"] = nowSec().epochSecond.toString()
    repeat(2) { assertThrows<IllegalArgumentException> { routes.loginWithCode(EmailCodeRequest(email, "000000"), exchange) } }

    val e = assertThrows<IllegalArgumentException> { routes.loginWithCode(EmailCodeRequest(email, "000000"), exchange) }
    expect(e.message).toEqual("login.tooManyCodeTries")
    expect(session["emailCode"]).toEqual(null)
    expect(session["codeTime"]).toEqual(null)
    expect(session["codeTries"]).toEqual(null)
  }

  @Test fun `loginWithCode rejects expired code`() {
    session["emailCode"] = "$email:123456"
    session["codeTime"] = (nowSec().epochSecond - 11 * 60).toString()
    assertThrows<IllegalArgumentException> { routes.loginWithCode(EmailCodeRequest(email, "123456"), exchange) }
  }
}
