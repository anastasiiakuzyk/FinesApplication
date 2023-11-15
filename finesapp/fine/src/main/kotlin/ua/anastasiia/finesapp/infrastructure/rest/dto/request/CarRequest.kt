package ua.anastasiia.finesapp.infrastructure.rest.dto.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import ua.anastasiia.finesapp.infrastructure.annotation.EnumValidator

data class CarRequest(
    @field:Pattern(regexp = "^(?=(.*[A-ZА-ЯІЇҐЄ]){2,})([A-ZА-ЯІЇҐЄ0-9]{3,8})\$")
    val plate: String,
    @field:Size(min = 1, max = 50)
    val make: String,
    @field:Size(min = 1, max = 50)
    val model: String?,
    @field:EnumValidator(CarColor::class)
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
