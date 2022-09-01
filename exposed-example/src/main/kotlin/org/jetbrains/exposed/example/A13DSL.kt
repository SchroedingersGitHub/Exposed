package org.jetbrains.exposed.example

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


object A13DSL {
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
        val sequelId: Column<Int> = integer("sequelId").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
        val director: Column<String> = varchar("director", 50)
    }

    object Players : Table() {
        val sequelId: Column<Int> = integer("sequel_id").uniqueIndex()
        val name: Column<String> = varchar("name", 50)
    }

    object Cities : IntIdTable() {
        val name = varchar("name", 50)
    }


    /** 05 Join */
    fun main2() {
        DbSettings.db
        transaction {
            /*val query =
                (Players innerJoin StarWarsFilms)
                    .select { StarWarsFilms.sequelId eq Players.sequelId }
                    .groupBy { StarWarsFilms.name }*/

            // 如果有外键，则可以使用完整语法将 select{} 替换为 selectAll() 相同的示例
            val query = Players.join(
                otherTable = StarWarsFilms,
                joinType = JoinType.INNER,
                additionalConstraint = { StarWarsFilms.sequelId eq Players.sequelId }
            )
                .slice(Players.name.count(), StarWarsFilms.name)
                .selectAll()
                .groupBy(StarWarsFilms.name)

            // println(query.prepareSQL(this)) out: ->
            @Language("SQL")
            val sql = """
                SELECT COUNT(Players.`name`), StarWarsFilms.`name`
                FROM Players
                         INNER JOIN StarWarsFilms ON (StarWarsFilms.sequelId = Players.sequel_id)
                GROUP BY StarWarsFilms.`name`
            """.trimIndent()


        }
    }

    /**
     * 06 Union、UnionAll
     * 您可以使用 .union(...) 合并多个查询的结果。
     * 根据 SQL 规范，查询必须具有相同数量的列，并且不能标记为要更新。
     * 当数据库支持时，可以合并子查询
     */
    fun main3() {
        DbSettings.db
        transaction {
            val lucasDirectedQuery = StarWarsFilms
                .slice(StarWarsFilms.name)
                .select { StarWarsFilms.director eq "George Lucas" }

            val abramsDirectedQuery = StarWarsFilms
                .slice(StarWarsFilms.name)
                .select { StarWarsFilms.director eq "J.J. Abrams" }

            val query = lucasDirectedQuery
                .union(abramsDirectedQuery) // 默认返回唯一行
                .map { it[StarWarsFilms.name] } // 查询相同列
            query

            // union: out: ->
            @Language("SQL")
            val sql = """
                SELECT StarWarsFilms.`name`
                FROM StarWarsFilms
                WHERE StarWarsFilms.director = 'George Lucas'
                UNION
                SELECT StarWarsFilms.`name`
                FROM StarWarsFilms
                WHERE StarWarsFilms.director = 'J.J. Abrams'
            """.trimIndent()

            val query2 = lucasDirectedQuery
                .unionAll(abramsDirectedQuery) // 返回重复项
                .map { it[StarWarsFilms.name] } // 查询相同列
            query2

            // unionAll: out: ->
            @Language("SQL")
            val sql2 = """
                SELECT StarWarsFilms.`name`
                FROM StarWarsFilms
                WHERE StarWarsFilms.director = 'George Lucas'
                UNION ALL
                SELECT StarWarsFilms.`name`
                FROM StarWarsFilms
                WHERE StarWarsFilms.director = 'J.J. Abrams'
            """.trimIndent()


        }

    }


    /** 07 Alias */
    fun main4() {
        DbSettings.db

        /*transaction {
            val filmTable = StarWarsFilms.alias("ft1")
            // println(filmTable.selectAll().prepareSQL(this)) //out: ->
            @Language("SQL")
            val sql = """
                SELECT ft1.id, ft1.sequelId, ft1.`name`, ft1.director
                FROM StarWarsFilms ft1
            """.trimIndent()
        }*/

        /**
         * join
         *
         * 别名还允许您在联接中使用同一个表：
         */
        /*transaction {
            val sequelTable = StarWarsFilms.alias("st")

            val  query = StarWarsFilms.innerJoin(
                otherTable = sequelTable,
                onColumn = { sequelId },
                otherColumn = { sequelTable[StarWarsFilms.id] }
            )
                .slice(StarWarsFilms.name, sequelTable[StarWarsFilms.name])
                .selectAll()

            // println(query.prepareSQL(this)) // out: ->
            @Language("SQL")
            val sql = """
                SELECT StarWarsFilms.`name`, st.`name`
                FROM StarWarsFilms
                         INNER JOIN StarWarsFilms st ON StarWarsFilms.sequelId = st.id
            """.trimIndent()
        }*/

        /** 在子查询中进行选择时 */
        transaction {
            val starWarsFilms = StarWarsFilms
                .slice(StarWarsFilms.id, StarWarsFilms.name)
                .selectAll()
                .alias("swf") // 子查询定义

            // 选择子查询中的列
            val id = starWarsFilms[StarWarsFilms.id]
            val name = starWarsFilms[StarWarsFilms.name]


            val query = starWarsFilms // FROM
                .slice(id, name) //  选择子查询中的列
                .selectAll()
            println(query.prepareSQL(this))

            @Language("SQL")
            val sql = """
                SELECT swf.id, swf.`name`
                FROM (SELECT StarWarsFilms.id, StarWarsFilms.`name` FROM StarWarsFilms) swf
            """.trimIndent()
        }

    }

    /** 08 schema(数据库对象) */
    fun main5() {
        DbSettings.db

        transaction {
            val schema = Schema("my_schema")
            SchemaUtils.createSchema(schema)
            SchemaUtils.dropSchema(schema)
        }

        // out: ->
        // CREATE SCHEMA IF NOT EXISTS my_schema
        // DROP SCHEMA IF EXISTS my_schema

        // 显示指定所有者
        // val schema = Schema("my_schema", authorization = "owner")

        // 设置默认架构
        // SchemaUtils.setSchema(schema)

    }

    /**
     * 09 Sequence
     *
     * Mysql不支持
     */
    fun main6() {
        DbSettings.db
        transaction {
            // val myseq = Sequence("my_sequence") // my_sequence is the sequence name.
            val myseq = Sequence(
                name = "my_sequence",
                startWith = 4,
                incrementBy = 2,
                minValue = 1,
                maxValue = 10,
                cycle = true,
                cache = 20
            )

            // 创建一个序列
            SchemaUtils.createSequence(myseq)
            // 删除一个序列
            SchemaUtils.dropSequence(myseq)


            /** 使用 NextVal */
            val nextVal = myseq.nextVal()
            val id = StarWarsFilms.insertAndGetId {
                it[id] = nextVal
                it[name] = "The Last Jedi"
                it[sequelId] = 8
                it[director] = "Rian Johnson"
            }

            // val firstValue = StarWarsFilms.slice(nextVal).selectAll().single()[nextVal]

        }

    }

    /**
     * 10 Batch Insert
     *
     * 与数据库交互时batchInsert 函数仍将创建多个 INSERT 语句
     *
     * 可将JDBC驱动程序: rewriteBatchedStatements=true
     * 详情: [https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html]
     *
     * 如果不需要获取新生成的值（例如：自动递增的 ID），
     * 请将 shouldReturnGeneratedValues 参数设置为 false，
     * 这样可以通过在块中批处理批处理来提高批处理插入的性能，
     * 而不是始终等待数据库同步新插入的对象状态。
     *
     *
     */
    fun main7() {
        DbSettings.db
        transaction {
            val cityNames = listOf("Paris", "Moscow", "Helsinki")
            Cities.batchInsert(cityNames, shouldReturnGeneratedValues = false) { name ->
                this[Cities.name] = name
            }
        }

    }


    /** 11 选择性插入 */
    fun main() {
        DbSettings.db
        transaction {
            // 查询结果 插入另一张表
            // val substring = users.name.substring(1, 2)
            // cities.insert(users.slice(substring).selectAll().orderBy(users.id).limit(2))


            // 插入指定列
            /*val userCount = users.selectAll().count()
            users.insert(
                users.slice(
                    stringParam("Foo"),
                    Random().castTo<String>(VarCharColumnType()).substring(1, 10)
                )
                    .selectAll(), columns = listOf(users.name, users.id)
            )*/

        }

    }


}

fun main() = A13DSL.main()
