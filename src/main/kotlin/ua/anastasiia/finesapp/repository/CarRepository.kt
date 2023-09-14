package ua.anastasiia.finesapp.repository

import org.springframework.data.jpa.repository.JpaRepository
import ua.anastasiia.finesapp.entity.Car

interface CarRepository : JpaRepository<Car, Long> {
    fun findByPlate(plate: String): Car?
}
