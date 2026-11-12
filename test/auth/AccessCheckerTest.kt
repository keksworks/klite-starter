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
import users.User

class AccessCheckerTest: BaseMocks() {
  val session = Session()
  val checker = create<AccessChecker>()

  init {
    every { exchange.session } returns session
    every { exchange.attr<User?>("user") } returns null
  }

  @Test fun `allows public access`() {
    every { exchange.route.annotations } returns listOf(Public())
    checker.before(exchange)
  }

  @Test fun `forbids unauthorized access`() {
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `access granted`() {
    every { exchange.attr<User?>("user") } returns user
    every { exchange.route.annotations } returns listOf(Access(USER))
    checker.before(exchange)
    verify { userRepository.setAppUser(user) }
  }

  @Test fun `forbids access without matching role`() {
    every { exchange.attr<User?>("user") } returns user
    every { exchange.route.annotations } returns listOf(Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `Access annotation overrides Public (eg on class)`() {
    every { exchange.attr<User?>("user") } returns user
    every { exchange.route.annotations } returns listOf(Public(), Access(ADMIN))
    assertThrows<ForbiddenException> { checker.before(exchange) }
  }

  @Test fun `allows access for route with multiple roles`() {
    every { exchange.attr<User?>("user") } returns user
    every { exchange.route.annotations } returns listOf(Access(ADMIN, USER))
    checker.before(exchange)
  }

  @Test fun `requires @Access annotation`() {
    every { exchange.attr<User?>("user") } returns user
    assertThrows<IllegalStateException> { checker.before(exchange) }
  }
}
