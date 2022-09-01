package org.jetbrains.exposed.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * 01 Getting Started
 * DSL
 */
object A1GettingStarted {

    @JvmStatic
    fun main(args: Array<String>) {
        // DSL
        //an example connection to H2 DB
        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities)

            // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
            val stPeteId = Cities.insert {
                it[name] = "St. Petersburg"
            } get Cities.id

            // 'select *' SQL: SELECT Cities.id, Cities.name FROM Cities
            println("Cities: ${Cities.selectAll()}")
        }

    }

    object Cities : IntIdTable() {
        val name = varchar("name", 50)
    }

}



