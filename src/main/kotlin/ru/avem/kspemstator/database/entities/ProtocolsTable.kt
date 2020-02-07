package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolsTable : IntIdTable() {
    val date = varchar("date", 256)
    val time = varchar("time", 256)
    val objectType = varchar("objectType", 32)
    val factoryNumber = varchar("factoryNumber", 32)
    val power = varchar("power", 32)
    val frequency = varchar("frequency", 32)
    val mark = varchar("mark", 32)
    val density = varchar("density", 32)
    val losses = varchar("losses", 32)
    val intensity = varchar("intensity", 32)
    val pos1 = varchar("pos1", 256)
    val u1 = varchar("u1", 32)
    val i1 = varchar("i1", 32)
    val p1 = varchar("p1", 32)
    val bf = varchar("bf", 32)
    val pf = varchar("pf", 32)
    val pt = varchar("pt", 32)
    val hf = varchar("hf", 32)
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(ProtocolsTable)

    var date by ProtocolsTable.date
    var time by ProtocolsTable.time
    var objectType by ProtocolsTable.objectType
    var factoryNumber by ProtocolsTable.factoryNumber
    var power by ProtocolsTable.power
    var frequency by ProtocolsTable.frequency
    var mark by ProtocolsTable.mark
    var density by ProtocolsTable.density
    var losses by ProtocolsTable.losses
    var intensity by ProtocolsTable.intensity
    var pos1 by ProtocolsTable.pos1
    var u1 by ProtocolsTable.u1
    var i1 by ProtocolsTable.i1
    var p1 by ProtocolsTable.p1
    var bf by ProtocolsTable.bf
    var pf by ProtocolsTable.pf
    var pt by ProtocolsTable.pt
    var hf by ProtocolsTable.hf

    override fun toString(): String {
        return "$id. $factoryNumber:$objectType - $date"
    }
}
