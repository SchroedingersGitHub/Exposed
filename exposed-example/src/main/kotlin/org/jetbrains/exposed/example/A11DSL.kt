package org.jetbrains.exposed.example

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object A11DSL {

    object StarWarsFilms : IntIdTable() {
        val sequelId: Column<Int> = integer("sequelId").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
        val director: Column<String> = varchar("director", 50)
    }

    object Players : Table() {
        val sequelId: Column<Int> = integer("sequel_id").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
    }

    /**
     * 为空不拼接条件
     *
     * 之前where条件
     */
    fun main2() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        val directorName: String? = ""
        val sequelId: Int? = 0

        transaction {
            val condition = when {
                directorName != null && sequelId != null ->
                    Op.build {
                        StarWarsFilms.director eq directorName and (StarWarsFilms.sequelId eq sequelId)
                    }

                directorName != null ->
                    Op.build { StarWarsFilms.director eq directorName }

                sequelId != null ->
                    Op.build { StarWarsFilms.sequelId eq sequelId }

                else -> null
            }
            // val query = condition?.let { StarWarsFilms.select(condition) } ?: StarWarsFilms.selectAll()

            assert("(StarWarsFilms.director = '') AND (StarWarsFilms.sequelId = 0)" == condition.toString())

        }
    }

    /** 或 */
    fun main3() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        val directorName: String? = ""
        val sequelId: Int? = 0

        transaction {
            val query = when {
                directorName != null && sequelId != null ->
                    StarWarsFilms.select { StarWarsFilms.director eq directorName and (StarWarsFilms.sequelId eq sequelId) }

                directorName != null ->
                    StarWarsFilms.select { StarWarsFilms.director eq directorName }

                sequelId != null ->
                    StarWarsFilms.select { StarWarsFilms.sequelId eq sequelId }

                else -> StarWarsFilms.selectAll()
            }
            assert("(StarWarsFilms.director = '') AND (StarWarsFilms.sequelId = 0)" == query.where.toString())

        }

    }


    /** andWhere */
    fun main4() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        val directorName: String? = ""
        val sequelId: Int? = 0

        transaction {
            val query = StarWarsFilms.selectAll()
            directorName?.let {
                query.andWhere { StarWarsFilms.director eq it }
            }
            sequelId?.let {
                query.andWhere { StarWarsFilms.sequelId eq it }
            }
            assert("(StarWarsFilms.director = '') AND (StarWarsFilms.sequelId = 0)" == query.where.toString())

        }

    }

    /**
     * 如果我们有条件从另一个表中选择，并且只想在条件为真时才联接它呢？
     * 您必须使用 adjustColumnSet 和 adjustSlice 函数
     * 这些函数允许扩展和修改join和slice部分
     */
    fun main() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        val playerName: String? = null

        transaction {
            val query = StarWarsFilms.selectAll()

            playerName?.let {
                query.adjustColumnSet {
                    innerJoin(
                        otherTable = Players,
                        onColumn = { StarWarsFilms.sequelId },
                        otherColumn = { Players.sequelId }
                    ) // out: INNER JOIN Players ON StarWarsFilms.sequelId = Players.sequel_id
                }.adjustSlice {
                    slice(fields + Players.columns) // 在StarWarsFilms字段基础上再加上Players的字段
                }.andWhere {
                    Players.name eq it
                }

            }
            // println(query.prepareSQL(this)) // out: ->

            // playerName != null out: ->
            @Language("SQL")
            val sql = """
                SELECT StarWarsFilms.id,
                       StarWarsFilms.sequelId,
                       StarWarsFilms.`name`,
                       StarWarsFilms.director,
                       Players.sequel_id,
                       Players.`name`,
                       Players.sequel_id,
                       Players.`name`
                FROM StarWarsFilms
                         INNER JOIN Players ON StarWarsFilms.sequelId = Players.sequel_id
                WHERE Players.`name` = ?
            """.trimIndent()

            // playerName == null out: ->
            @Language("SQL")
            val sql2 = """
                SELECT StarWarsFilms.id, StarWarsFilms.sequelId, StarWarsFilms.`name`, StarWarsFilms.director
                FROM StarWarsFilms
            """.trimIndent()


        }

    }

}

fun main() = A11DSL.main()
