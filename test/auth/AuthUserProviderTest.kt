package auth

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.BaseMocks
import db.TestData
import io.mockk.every
import io.mockk.verify
import klite.Config
import klite.Email
import klite.oauth.OAuthTokenResponse
import klite.oauth.UserProfile
import org.junit.jupiter.api.Test
import users.User

class AuthUserProviderTest: BaseMocks() {
  init { Config.useEnvFile() }
  val provider = AuthUserProvider(userRepository)
  val profile = UserProfile("GOOGLE", "123", Email("john@gmail.com"), "John", "Doe")
  val tokenResponse = OAuthTokenResponse("accessToken", 3600, "scope", refreshToken = "refreshToken")

  @Test fun `creates new user`() {
    every { userRepository.by(User::email to profile.email) } returns null
    val user = provider.provide(profile, tokenResponse, exchange) as User
    expect(user.email).toEqual(profile.email)
    expect(user.firstName).toEqual(profile.firstName)
    expect(user.lastName).toEqual(profile.lastName)
    expect(user.refreshToken).toEqual(tokenResponse.refreshToken)
    verify { userRepository.save(user) }
  }

  @Test fun `existing user`() {
    every { userRepository.by(User::email to profile.email) } returns TestData.user
    val user = provider.provide(profile, tokenResponse, exchange) as User
    expect(user).toEqual(TestData.user.copy(lastLoginAt = user.lastLoginAt, refreshToken = tokenResponse.refreshToken))
    verify { userRepository.save(user) }
  }
}
