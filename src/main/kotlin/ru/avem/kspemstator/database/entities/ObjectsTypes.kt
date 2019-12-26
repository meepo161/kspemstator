package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ObjectsTypes:  IntIdTable() {
    val objectType = varchar("objectType", 32)
}

class ExperimentObjectsType(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<ExperimentObjectsType>(ObjectsTypes)
    var objectType by ObjectsTypes.objectType

    override fun toString(): String {
        return objectType
    }
}
