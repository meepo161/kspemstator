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
    val insideD = varchar("insideD", 32)
    val outsideD = varchar("outsideD", 32)
    val ironLength = varchar("ironLength", 32)
    val backHeight = varchar("backHeight", 32)
    val material = varchar("material", 32)
    val insulation = varchar("insulation", 32)
    val mark = varchar("mark", 32)
    val density = varchar("density", 32)
    val losses = varchar("losses", 32)
    val intensity = varchar("intensity", 32)
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
    var insideD by ProtocolsTable.insideD
    var outsideD by ProtocolsTable.outsideD
    var ironLength by ProtocolsTable.ironLength
    var backHeight by ProtocolsTable.backHeight
    var material by ProtocolsTable.material
    var insulation by ProtocolsTable.insulation
    var mark by ProtocolsTable.mark
    var density by ProtocolsTable.density
    var losses by ProtocolsTable.losses
    var intensity by ProtocolsTable.intensity
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
