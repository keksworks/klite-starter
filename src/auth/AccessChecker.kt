package auth

import klite.Before
import klite.ForbiddenException
import klite.HttpExchange
import klite.NotFoundRoute
import klite.RequestMethod.OPTIONS
import users.Role
import users.User
import users.UserRepository
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Target(FUNCTION, CLASS) annotation class Public
@Target(FUNCTION, CLASS) annotation class Access(vararg val roles: Role)

class AccessChecker(private val userRepository: UserRepository): Before {
  override fun before(exchange: HttpExchange) {
    if (exchange.method == OPTIONS) return
    exchange.session // the session store loads the authenticated user along with it, in a single query
    val user = exchange.userOrNull
    val access = exchange.route.findAnnotation<Access>()
    val isPublic = access == null && exchange.route.hasAnnotation<Public>() || exchange.route is NotFoundRoute
    if (user == null && !isPublic) throw ForbiddenException()
    if (user != null && !isPublic) {
      if (access == null) error("@Access annotation is required for non-@Public routes")
      if (access.roles.none { it == user.role }) throw ForbiddenException()
    }
    if (user != null) userRepository.setAppUser(user)
  }
}

var HttpExchange.user: User
  get() = attr("user")
  set(value) { attrPut(value); attr("user", value) }

val HttpExchange.userOrNull: User? get() = attr("user")
