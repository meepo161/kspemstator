package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTable:  IntIdTable() {
    val material = varchar("material", 32)
    val mark = varchar("mark", 32)
    val density = varchar("density", 32)
    val losses = varchar("losses", 32)
    val intensity = varchar("intensity", 32)
    val insulation = varchar("insulation", 32)

}

class ExperimentObject(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ExperimentObject>(ObjectsTable)

    var material by ObjectsTable.material
    var mark by ObjectsTable.mark
    var density by ObjectsTable.density
    var losses by ObjectsTable.losses
    var intensity by ObjectsTable.intensity
    var insulation by ObjectsTable.insulation

}
