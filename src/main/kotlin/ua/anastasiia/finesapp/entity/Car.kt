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
    var plate: String,
    var mark: String,
    var model: String,
    var color: CarColor
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
