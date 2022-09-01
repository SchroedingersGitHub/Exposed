package org.jetbrains.exposed.example

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object A12DSL {

    object StarWarsFilms : IntIdTable() {
        val sequelId: Column<Int> = integer("sequelId").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
        val director: Column<String> = varchar("director", 50)
    }

    object Players : Table() {
        val sequelId: Column<Int> = integer("sequel_id").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
    }

    object DbSettings {
        val db by lazy {
            Database.connect(
                url = "jdbc:mysql://localhost:3306/testdb",
                driver = "com.mysql.cj.jdbc.Driver",
                user = "root",
                password = "yes",
                databaseConfig = DatabaseConfig {
                    sqlLogger = StdOutSqlLogger
                }
            )
        }
    }

    /** 01 count */
    fun main2() {
        DbSettings.db
        transaction {
            val result = StarWarsFilms.selectAll().count()
            println(result)
        }
    }

    /** 02 Order-by */
    fun main3() {
        DbSettings.db
        transaction {
            val result = StarWarsFilms.selectAll().orderBy(StarWarsFilms.sequelId to SortOrder.ASC)
            println(result.prepareSQL(this)) // out: ->
            @Language("SQL")
            val sql = """
                SELECT StarWarsFilms.id, StarWarsFilms.sequelId, StarWarsFilms.`name`, StarWarsFilms.director
                FROM StarWarsFilms
                ORDER BY StarWarsFilms.sequelId ASC
            """.trimIndent()

        }

    }

    /**
     * 03 Group-by
     *
     * 通过slice()方法定义字段及函数(如count)
     *
     * ```
     * count
     * sum
     * max
     * min
     * average
     * ...
     * ```
     */
    fun main4() {
        DbSettings.db
        transaction {
            val query = StarWarsFilms
                .slice(
                    StarWarsFilms.sequelId.count(),
                    StarWarsFilms.director
                )
                .selectAll()
                .groupBy(StarWarsFilms.director)

            // println(query.prepareSQL(this)) // out: ->
            @Language("SQL")
            val sql = """
                SELECT COUNT(StarWarsFilms.sequelId), StarWarsFilms.director
                FROM StarWarsFilms
                GROUP BY StarWarsFilms.director
            """.trimIndent()
        }


    }

    /** 04 Limit */
    fun main() {
        DbSettings.db
        val query = StarWarsFilms
            .selectAll()
            .limit(1, offset = 2) // offset用来分页

        transaction {
            // println(query.prepareSQL(this)) out: ->
            @Language("SQL")
            val sql = """
            SELECT StarWarsFilms.id, StarWarsFilms.sequelId, StarWarsFilms.`name`, StarWarsFilms.director
            FROM StarWarsFilms
            LIMIT 1 OFFSET 2
        """.trimIndent()
        }



    }


}

fun main() = A12DSL.main()
