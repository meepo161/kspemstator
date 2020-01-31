package ru.avem.kspemstator.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.kspemstator.database.entities.*
import ru.avem.kspemstator.database.entities.Users.login
import java.sql.Connection

fun validateDB() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
//        SchemaUtils.drop(Users, ProtocolsTable, ObjectsTable)
        SchemaUtils.create(Users, ProtocolsTable, ObjectsTypes, MarksTypes)
    }

    transaction {
        if (User.all().count() < 2) {
            val admin = User.find() {
                login eq "admin"
            }

            if (admin.empty()) {
                User.new {
                    login = "admin"
                    password = "avem"
                    fullName = "admin"
                }


                ExperimentObjectsType.new {
                    objectType = "111"
                    outsideD = "222"
                    insideD = "333"
                    ironLength = "444"
                    backHeight = "555"
                    material = "Сталь"
                    insulation = "Лак"
                    mark = "2222"
                }

                ExperimentObjectsType.new {
                    objectType = "2222"
                    outsideD = "3333"
                    insideD = "4444"
                    ironLength = "5555"
                    backHeight = "6666"
                    material = "Алюминий"
                    insulation = "Оксидирование"
                    mark = "2222"
                }

                MarksObjects.new {
                    mark = "2222"
                    density = "3333"
                    losses = "4444"
                    intensity = "5555"
                }
            }
        }
    }
}
