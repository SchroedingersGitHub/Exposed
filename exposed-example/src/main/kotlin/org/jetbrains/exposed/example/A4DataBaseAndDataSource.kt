package org.jetbrains.exposed.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * DataBaseAndDataSource
 *
 * 数据源
 */
object A4DataBaseAndDataSource {


    @JvmStatic
    fun main(args: Array<String>) {

        // mysql
        /*Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes"
        )*/

        // mysql+Hikari
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://localhost:3306/testdb"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = "root"
            password = "yes"
            maximumPoolSize = 10
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(Cities)
            SchemaUtils.drop(Cities)
        }


    }

    object Cities : IntIdTable() {
        val name = varchar("name", 50)
    }
}
