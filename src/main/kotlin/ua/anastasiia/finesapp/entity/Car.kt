package ua.anastasiia.finesapp.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val plate: String,
    val make: String,
    val model: String,
    val color: String
) {
    enum class CarColor {
        WHITE,
        BLACK,
        SILVER,
        RED,
        BLUE,
        GREEN,
        BROWN,
        YELLOW
    }
}
