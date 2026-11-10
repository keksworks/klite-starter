package db

import auth.EmailCode
import klite.Email
import todos.Todo
import users.User

/** Immutable domain object samples for unit tests */
object TestData {
  val user = User("Test", "User", Email("test@example.com"), id = Id(123))
  val todo = Todo("Buy groceries")
  val emailCode = EmailCode(user.email, EmailCode.hash(user.email, "123456"), "127.0.0.1")
}
