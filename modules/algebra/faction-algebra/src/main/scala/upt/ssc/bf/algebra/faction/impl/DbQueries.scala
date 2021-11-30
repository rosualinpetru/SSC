package upt.ssc.bf.algebra.faction.impl

import doobie.implicits.toSqlInterpolator

private[faction] object DbQueries {

  def createTable(): doobie.ConnectionIO[Int] =
    sql"create table if not exists password (password VARCHAR(10) PRIMARY KEY);".update.run

  def insertPassword(password: String): doobie.ConnectionIO[Int] =
    sql"insert into password (password) VALUES ($password)".update.run

  def existsPassword(password: String): doobie.ConnectionIO[Boolean] =
    sql"select exists(select 1 from password where password=$password)"
      .query[Boolean].unique
}
