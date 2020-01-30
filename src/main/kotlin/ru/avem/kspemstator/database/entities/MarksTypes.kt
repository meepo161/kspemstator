package ru.avem.kspemstator.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object MarksTypes:  IntIdTable() {
    val mark = varchar("mark", 32)
    val density = varchar("density", 32)
    val losses = varchar("losses", 32)
    val intensity = varchar("intensity", 32)
}

class MarksObjects(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<MarksObjects>(MarksTypes)
    var mark by MarksTypes.mark
    var density by MarksTypes.density
    var losses by MarksTypes.losses
    var intensity by MarksTypes.intensity

    override fun toString(): String {
        return mark
    }
}
