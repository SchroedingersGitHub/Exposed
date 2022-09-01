package org.jetbrains.exposed.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Transactions
 *
 */
object A6Transactions {


    /**
     * 使用多个数据库
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val db1 = Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes"
        )
        val db2 = Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb2",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes"
        )

        transaction(db2) { // ✨
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)

            User.new {
                firstName = "lei"
                lastName = "li"
            }

            val toList = Users.select {
                Users.firstName eq "lei"
            }.toList()
            println(toList)
        }
        // ✨ 设置默认数据库
        TransactionManager.defaultDatabase = db2

    }

    // 带有针对文本字段的 eagerLoading
    object Documents : IntIdTable() {
        val content = text("content", eagerLoading = true)
    }

    class Document(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Document>(Documents)

        var content by Documents.content
    }


    object Users : IntIdTable() {
        val firstName = varchar("firstName", 50)
        val lastName = varchar("lastName", 50)
    }

    class User(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<User>(Users)

        var firstName by Users.firstName
        var lastName by Users.lastName

    }
}
