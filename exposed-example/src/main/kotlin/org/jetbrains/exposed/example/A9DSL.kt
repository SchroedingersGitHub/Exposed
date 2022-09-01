package org.jetbrains.exposed.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object A9DSL {

    object StarWarsFilms2 : Table() {
        val id: Column<Int> = integer("id").autoIncrement() // 自动递增
        val sequelId: Column<Int> = integer("sequelId").uniqueIndex() // 唯一索引
        val name: Column<String> = varchar("name", 50)
        val director: Column<String> = varchar("director", 50)

        // 配置主键
        override val primaryKey = PrimaryKey(id, name = "PK_StarWarsFilms_Id") // name: 返回主键的名称  PK_StarWarsFilms_Id 在这里是可选的
    }

    /**
     * 上面简化版本
     * 包含名称为 id 的 Int id 的表可以这样声明：
     */
    object StarWarsFilms : IntIdTable() {
        val sequelId: Column<Int> = integer("sequelId").uniqueIndex() // 唯一索引
        val name: Column<String> = varchar("name", 50)
        val director: Column<String> = varchar("director", 50)
    }

    /** 01 Create */
    fun main2() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        /*// 创建表结构
        transaction {
            SchemaUtils.create(StarWarsFilms)
        }*/

        transaction {
            val id = StarWarsFilms.insertAndGetId {
                it[name] = "The Last Jedi"
                it[sequelId] = 9
                it[director] = "Rian Johnson"
            }
            println("自增主键为: $id")
        }


    }

    /** 02 Read */
    fun main3() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )
        /** Query */
        /*transaction {
            val query: Query = StarWarsFilms.select {
                StarWarsFilms.sequelId eq 8
            }
            // Query继承Iterable，因此可以使用地图/foreach等遍历它。例如：
            query.forEach {
                println(it[StarWarsFilms.name])
            }
        }*/

        /** slice选择特定的列 or/and 表达式 */
        /*transaction {
            val filmAndDirector = StarWarsFilms
                .slice(StarWarsFilms.name, StarWarsFilms.director)
                .selectAll().map {
                    it[StarWarsFilms.name] to it[StarWarsFilms.director]
                }
            println(filmAndDirector)
        }*/

        /** withDistinct 去重 */
        transaction {
            val directors = StarWarsFilms
                .slice(StarWarsFilms.director)
                .select { StarWarsFilms.sequelId less 5 } // 小于10
                .withDistinct().map {
                    it[StarWarsFilms.director]
                }
            println(directors)
        }


    }

    /** 03 Update */
    fun main4() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )

        /*transaction {
//            StarWarsFilms.update(
//                where = { StarWarsFilms.sequelId eq 8 },
//                body = { it[name] = "Episode VIII – The Last Jedi" }
//            )
            // 简写成:
            StarWarsFilms.update({ StarWarsFilms.sequelId eq 8 }) {
                it[name] = "Episode VIII – The Last Jedi"
            }
        }*/

        /** 使用表达式增量更新值 */
        transaction {
            StarWarsFilms.update({ StarWarsFilms.sequelId eq 9 }) {
                with(SqlExpressionBuilder) {
                    it.update(sequelId, sequelId + 1) // 在原有结果上加一
                    // or
                    // it[sequelId] = sequelId + 1
                }
            }
        }
    }

    /** 04 Delete */
    fun main() {
        Database.connect(
            url = "jdbc:mysql://localhost:3306/testdb",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "yes",
            databaseConfig = DatabaseConfig {
                sqlLogger = StdOutSqlLogger
            }
        )

        transaction {
            StarWarsFilms.deleteWhere {
                StarWarsFilms.sequelId eq 8
            }
        }
    }
}

fun main() = A9DSL.main()
