package users

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData.user
import io.mockk.every
import io.mockk.verify
import klite.Email
import org.junit.jupiter.api.Test
import users.Role.ADMIN
import java.net.URI

class UserLoginTest: BaseMocks() {
  @Test fun `creates user on first login`() {
    every { userRepository.by(User::email to user.email) } returns null
    val avatar = URI("https://avatar")
    val result = userRepository.login(user.email, user.firstName, user.lastName, avatar, "token")
    expect(result).toEqual(user.copy(id = result.id, lastLoginAt = result.lastLoginAt, avatarUrl = avatar, refreshToken = "token"))
    verify { userRepository.save(result) }
  }

  @Test fun `creates admin for own domain`() {
    val email = Email("boss@localhost")
    every { userRepository.by(User::email to email) } returns null
    val result = userRepository.login(email, "Boss", "")
    expect(result).toEqual(User("Boss", "", email, role = ADMIN).copy(id = result.id, lastLoginAt = result.lastLoginAt))
  }

  @Test fun `updates existing user on login`() {
    every { userRepository.by(User::email to user.email) } returns user
    val result = userRepository.login(user.email, "Ignored", "Ignored")
    expect(result).toEqual(user.copy(lastLoginAt = result.lastLoginAt))
    verify { userRepository.save(result) }
  }

  @Test fun `keeps existing refresh token when not provided`() {
    val existing = user.copy(refreshToken = "existing")
    every { userRepository.by(User::email to existing.email) } returns existing
    expect(userRepository.login(existing.email, "", "").refreshToken).toEqual("existing")
  }

  @Test fun `updates refresh token when provided`() {
    every { userRepository.by(User::email to user.email) } returns user
    expect(userRepository.login(user.email, "", "", refreshToken = "new").refreshToken).toEqual("new")
  }
}
