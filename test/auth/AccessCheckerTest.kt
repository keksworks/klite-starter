package auth

import db.BaseMocks
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.ForbiddenException
import klite.Session
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import users.Role.ADMIN
import users.Role.USER

class AccessCheckerTest: BaseMocks() {
  val session = Session()
  val checker = create<AccessChecker>()

  init { every { exchange.session } returns session }

  @Test fun `allows public access`() {
    every { exchange.route.annotations } returns listOf(Public())
    checker.before(exchange)
  }

  @Test fun `forbids unauthorized access`() {
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `access granted`() {
    session["userId"] = user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(USER))
    checker.before(exchange)
    verify {
      exchange.attr("user", user)
      userRepository.setAppUser(user)
    }
  }

  @Test fun `forbids access without matching role`() {
    session["userId"] = user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
    verify { exchange.attr("user", user) }
  }

  @Test fun `Access annotation overrides Public (eg on class)`() {
    session["userId"] = user.id.toString()
    every { exchange.route.annotations } returns listOf(Public(), Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `allows access for route with multiple roles`() {
    session["userId"] = user.id.toString()
    every { exchange.route.annotations } returns listOf(Access(ADMIN, USER))
    checker.before(exchange)
    verify { exchange.attr("user", user) }
  }

  @Test fun `requires @Access annotation`() {
    session["userId"] = user.id.toString()
    assertThrows<IllegalStateException> { checker.before(exchange) }
  }
}
