package org.jetbrains.exposed.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Transactions
 *
 */
object A8Transactions {


    /**
     * 与协程配合
     *
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val db = Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
        )

        runBlocking {

            transaction {
                SchemaUtils.create(Users) // 表将在当前线程上创建

                /*newSuspendedTransaction(Dispatchers.Default) {
                    Users.insert { it[id] = 1 } // 此插入将在默认调度程序线程之一中执行

                    suspendedTransaction {
                        // 此选择也将使用相同事务在默认调度程序的某个线程上执行
                        Users.select { Users.id eq 1 }.single()[Users.id]
                    }
                }

                *//* val result = newSuspendedTransaction(Dispatchers.IO) {
                    FooTable.select { FooTable.id eq 1 }.single()[H2Tests.Testing.id] // This select will be executed on some thread from IO dispatcher using the same transaction
                }*/
            }
        }


    }

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
