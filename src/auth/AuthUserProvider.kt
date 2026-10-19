package auth

import klite.HttpExchange
import klite.oauth.OAuthTokenResponse
import klite.oauth.OAuthUser
import klite.oauth.OAuthUserProvider
import klite.oauth.UserProfile
import users.UserRepository
import users.login

class AuthUserProvider(
  private val userRepository: UserRepository
): OAuthUserProvider {
  override fun provide(profile: UserProfile, tokenResponse: OAuthTokenResponse, exchange: HttpExchange): OAuthUser =
    userRepository.login(profile.email, profile.firstName, profile.lastName, profile.avatarUrl, tokenResponse.refreshToken)
}
