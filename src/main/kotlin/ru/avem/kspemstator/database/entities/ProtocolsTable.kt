package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolsTable : IntIdTable() {
    val date = varchar("date", 128)
    val time = varchar("time", 128)
    val objectName = varchar("objectName", 256)
    val factoryNumber = varchar("factoryNumber", 256)
    val outsideDiameter = varchar("outsideDiameter", 64)
    val insideDiameter = varchar("insideDiameter", 64)
    val ironLength = varchar("ironLength", 64)
    val backHeight = varchar("backHeight", 64)
    val result = varchar("result", 64)
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(ProtocolsTable)

    var date by ProtocolsTable.date
    var time by ProtocolsTable.time
    var factoryNumber by ProtocolsTable.factoryNumber
    var objectName by ProtocolsTable.objectName
    var outsideDiameter by ProtocolsTable.outsideDiameter
    var insideDiameter by ProtocolsTable.insideDiameter
    var ironLength by ProtocolsTable.ironLength
    var backHeight by ProtocolsTable.backHeight
    var result by ProtocolsTable.result

    override fun toString(): String {
        return "$id. $factoryNumber:$objectName - $date Результат: $result"
    }
}
