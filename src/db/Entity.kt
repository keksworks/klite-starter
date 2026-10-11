package db

import klite.TSID
import klite.jdbc.BaseCrudRepository
import klite.jdbc.BaseEntity
import javax.sql.DataSource

/** Use TSID by default for all ids */
typealias Id<T> = TSID<T>
interface Entity<T>: BaseEntity<Id<T>> {
  val createdAt get() = id.createdAt
}

abstract class CrudRepository<T: Entity<T>>(db: DataSource, table: String): BaseCrudRepository<T, Id<T>>(db, table) {
  override val orderAsc get() = "order by id"
}
