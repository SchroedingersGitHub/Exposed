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
object A7Transactions {


    /**
     * 嵌套事务
     *
     * transaction块中发生的任何异常都不会回滚整个事务，
     * 而只会回滚当前transaction中的代码,
     * 使用SQL SAVEPOINT功能在transaction块开始时标记当前事务，
     * 并在退出时释放它。
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val db = Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig { // ✨
                useNestedTransactions = true
            }
        )
        // db.useNestedTransactions = true // 废弃

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
            val toList = Users.selectAll().toList()

            transaction{
                Users.insert {
                    it[firstName] = "testFirstName"
                    it[lastName] = "testLastName"
                }
            }
            println(toList)
        }

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
