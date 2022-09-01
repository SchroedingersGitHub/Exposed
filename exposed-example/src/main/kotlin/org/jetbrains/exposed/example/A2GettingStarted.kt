package org.jetbrains.exposed.example

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * 01 Getting Started
 *
 * DAO
 */
object A2GettingStarted {

    @JvmStatic
    fun main(args: Array<String>) {
        //an example connection to H2 DB
        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities)

            // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            val stPete = City.new {
                name = "St. Petersburg"
            }

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Cities: ${City.all()}")
        }

    }

    object Cities : IntIdTable() {
        val name = varchar("name", 50)
    }

    class City(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<City>(Cities)

        var name by Cities.name
    }


}



