package todos

import auth.Access
import klite.annotations.GET
import users.Role.USER

class TodoRoutes(private val repository: TodoRepository) {
  @GET("/todos") @Access(USER) fun all() = repository.list()
}
