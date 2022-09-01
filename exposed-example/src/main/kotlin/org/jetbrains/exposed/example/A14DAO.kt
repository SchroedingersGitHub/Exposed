package org.jetbrains.exposed.example

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.transaction

object A14DAO {

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

    object StarWarsFilms : IntIdTable() {
        val sequelId = integer("sequel_id").uniqueIndex()
        val name = varchar("name", 50)
        val director = varchar("director", 50)
    }

    /** 将表实例 定义为一个类实例  */
    class StarWarsFilm(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<StarWarsFilm>(StarWarsFilms)

        var sequelId by StarWarsFilms.sequelId
        var name by StarWarsFilms.name
        var director by StarWarsFilms.director
    }

    /** 01 Create */
    fun main2() {
        DbSettings.db
        transaction {
            StarWarsFilm.new {
                name = "The Last Jedi"
                sequelId = 8
                director = "Rian Johnson"
            }
            // sql: ->
            @Language("SQL")
            val sql = """
                INSERT INTO StarWarsFilms (director, `name`, sequel_id)
                VALUES ('Rian Johnson', 'The Last Jedi', 8)
            """.trimIndent()
        }

    }

    /** 02 Read */
    fun main3() {
        DbSettings.db
        transaction {
            val movies = StarWarsFilm.all()
            val movies2 = StarWarsFilm.find(StarWarsFilms.sequelId eq 8)
            val movie = StarWarsFilm.findById(1)

            /* movies.toList().forEach {
                 println(it.name)
             }
             movies2.toList().forEach {
                 println(it.name)
             }
             println(movie?.name)*/
        }

    }

    /**
     * 03 Order-by
     *
     * 注意: 该排序是查询完毕后排序，而不是用SQL排序
     */
    fun main4() {
        DbSettings.db
        transaction {
            // 升序
            val movies = StarWarsFilm.all().sortedBy { it.sequelId }
            movies.toList().forEach {
                println("${it.name} - ${it.sequelId}")
            }

            // 降序
            val movies2 = StarWarsFilm.all().sortedByDescending { it.sequelId }
            movies2.toList().forEach {
                println("${it.name} - ${it.sequelId}")
            }
        }

    }

    /** 04 Update */
    fun main5() {
        DbSettings.db
        transaction {
            val movie = StarWarsFilm.findById(1)
            movie?.name = "Episode VIII – The Last Jedi" // ✨
        }
    }

    /** 05 Delete */
    fun main() {
        DbSettings.db
        transaction {
            val movie = StarWarsFilm.findById(1)
            movie?.delete()
        }
    }


}

fun main() = A14DAO.main()
