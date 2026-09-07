package db

import klite.Email
import todos.Todo
import users.User

/** Immutable domain object samples for unit tests */
object TestData {
  val user = User("Test User", Email("test@example.com"))
  val todo = Todo("Buy groceries")
}
