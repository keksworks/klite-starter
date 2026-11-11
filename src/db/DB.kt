package db

import klite.Config
import klite.info
import klite.isDev
import klite.jdbc.DBMigrator
import klite.jdbc.DockerCompose
import klite.jdbc.PooledDataSource
import klite.jdbc.useAppDBUser
import klite.logger
import javax.sql.DataSource
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun initDB(): DataSource {
  if (Config.isDev) DockerCompose.startDB()
  logger("db").info("Migrating " + Config["DB_URL"])
  DBMigrator().migrate()
  useAppDBUser()
  return PooledDataSource(timeout = 5.seconds, leakCheckThreshold = 7.minutes)
}
