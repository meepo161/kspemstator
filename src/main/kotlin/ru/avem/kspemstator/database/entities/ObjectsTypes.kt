package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTypes:  IntIdTable() {
    val objectType = varchar("objectType", 32)
    val insideD = varchar("insideD", 32)
    val outsideD = varchar("outsideD", 32)
    val ironLength = varchar("ironLength", 32)
    val backHeight = varchar("backHeight", 32)
    val material = varchar("material", 32)
    val insulation = varchar("insulation", 32)
    val mark = varchar("mark", 32)

}

class ExperimentObjectsType(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ExperimentObjectsType>(ObjectsTypes)
    var objectType by ObjectsTypes.objectType
    var insideD by ObjectsTypes.insideD
    var outsideD by ObjectsTypes.outsideD
    var ironLength by ObjectsTypes.ironLength
    var backHeight by ObjectsTypes.backHeight
    var material by ObjectsTypes.material
    var insulation by ObjectsTypes.insulation
    var mark by ObjectsTypes.mark

    override fun toString(): String {
        return objectType
    }
}
