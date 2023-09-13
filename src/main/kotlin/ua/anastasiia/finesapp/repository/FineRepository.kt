package ua.anastasiia.finesapp.repository

import org.springframework.data.jpa.repository.JpaRepository
import ua.anastasiia.finesapp.entity.Fine

interface FineRepository : JpaRepository<Fine, Long> {
    fun findByCarPlate(plateString: String): List<Fine>
}
