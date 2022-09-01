package org.jetbrains.exposed.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Transactions
 *
 */
object A5Transactions {


    /**
     * 01 exposed的CRUD操作必须从事务中调用
     * 02 事务执行是同步的，如果需要异步考虑在单独的线程上运行
     * 03 transaction支持直接返回值
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // mysql
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes"
        )

        /*// transaction支持直接返回值
        val leiList = transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)

            User.new {
                firstName = "lei"
                lastName = "li"
            }

            Users.select {
                Users.firstName eq "lei"
            }.toList()
        }
        println(leiList)*/

        // ---------

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Documents)
            Document.new {
                content = "大文本!!"
            }
        }

        // 没有 eagerLoading
        val idsAndContent = transaction {
            Documents.selectAll().limit(10).map { it[Documents.id] to it[Documents.content] }
        }
        println("idsAndContent = $idsAndContent")

        val documentsWithContent = transaction {
            Documents.selectAll().limit(10)
        }
        println("documentsWithContent = $documentsWithContent")


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
