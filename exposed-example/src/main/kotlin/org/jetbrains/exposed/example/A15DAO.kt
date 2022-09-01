package org.jetbrains.exposed.example

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.transactions.transaction

object A15DAO {

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

        val ratings by UserRating referrersOn UserRatings.film // 确保使用 val 和 referrersOn

        override fun toString(): String {
            return "StarWarsFilm(sequelId=$sequelId, name='$name', director='$director', ratings=$ratings)"
        }


    }

    object Users : IntIdTable() {
        val name = varchar("name", 50)
    }

    class User(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<User>(table = Users)

        var name by Users.name
        override fun toString(): String {
            return "User(name='$name')"
        }


    }


    /** 添加个引用表 */
    object UserRatings : IntIdTable() {
        val value = long("value")
        val film = reference("film", foreign = StarWarsFilms)
        var user = reference("user", foreign = Users)

    }

    /**
     *
     * by 持久化对象 --引用到-—> 表字段上
     */
    class UserRating(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<UserRating>(UserRatings)

        var value by UserRatings.value
        var film by StarWarsFilm referencedOn UserRatings.film // 对于普通的引用使用 referencedOn
        var user by User referencedOn UserRatings.user
        override fun toString(): String {
            return "UserRating(value=$value, film=$film, user=$user)"
        }


    }

    /** 06 many-to-one reference */
    fun main() {
        DbSettings.db

        transaction {
            /*val starWarsFilm = StarWarsFilm.findById(5)
            val user_ = User.findById(2)
            UserRating.new {
                value = 100L
                film = starWarsFilm!!
                user = user_!!
            }*/

            val findById = UserRating.findById(1)
            findById!!.film
            /*findById?.let {
                println("${it.film}")
            }*/



            /*val findById = StarWarsFilm.findById(5)
            findById?.let {
                val ratings = it.ratings
                ratings.toList().forEach(::println)
            }*/

        }
    }


}

fun main() = A15DAO.main()
