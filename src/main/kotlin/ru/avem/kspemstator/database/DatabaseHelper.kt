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
        SchemaUtils.create(Users, ProtocolsTable, ObjectsTable, ObjectsTypes)
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

                ExperimentObject.new {
                    mark = "2212"
                    density = "7800"
                    losses = "2.6"
                    intensity = "1.42"
                }

                ExperimentObjectsType.new {
                    objectType = "111"
                    insideD = "111"
                    outsideD = "111"
                    ironLength = "111"
                    backHeight = "111"
                    material = "Сталь"
                    mark = "2212"
                    insulation = "Лаковая"
                }
            }
        }
    }
}
