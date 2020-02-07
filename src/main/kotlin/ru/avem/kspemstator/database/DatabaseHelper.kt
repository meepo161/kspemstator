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
                    objectType = "ТЕСТ"
                    power = "260"
                    frequency = "160"
                    outsideD = "260"
                    insideD = "160"
                    ironLength = "140"
                    backHeight = "35"
                    material = "Сталь"
                    insulation = "Оксидирование"
                    mark = "2212"
                }

                MarksObjects.new {
                    mark = "2212"
                    density = "7800"
                    losses = "2.6"
                    intensity = "240"
                }

                MarksObjects.new {
                    mark = "2013"
                    density = "7850"
                    losses = "2.5"
                    intensity = "185"
                }

                Protocol.new {
                    date = "2222"
                    time = "3333"
                    objectType = "4444"
                    factoryNumber = "5555"
                    power = "5555"
                    frequency = "5555"
                    mark = "5555"
                    density = "5555"
                    losses = "5555"
                    intensity = "5555"
                    pos1 = "5555"
                    u1 = "5555"
                    i1 = "5555"
                    p1 = "5555"
                    bf = "5555"
                    pf = "5555"
                    pt = "5555"
                    hf = "5555"
                }
            }
        }
    }
}
