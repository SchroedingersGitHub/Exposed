package org.jetbrains.exposed.example

import org.jetbrains.exposed.sql.Database

/**
 * DataBaseAndDataSource
 *
 * 使用DataBase和DataSource
 */
object A3DataBaseAndDataSource {


    @JvmStatic
    fun main(args: Array<String>) {

        // 获取数据库实例
        val db = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
        // 也可以为连接池等高级行为提供javax.sql.DataSource：
        // val db2 = Database.connect(dataSource)

        // 注意：从Exposed 0.10开始，每db执行此代码多次，将在您的应用程序中创建泄漏，因此建议将其存储以供以后使用。例如：
        /*object DbSettings {
            val db by lazy {
                Database.connect(*//* setup connection *//*)
            }
        }*/

    }
}
