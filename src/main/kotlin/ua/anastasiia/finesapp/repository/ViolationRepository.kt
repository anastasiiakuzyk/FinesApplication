package ua.anastasiia.finesapp.repository

import org.springframework.data.jpa.repository.JpaRepository
import ua.anastasiia.finesapp.entity.Violation

interface ViolationRepository : JpaRepository<Violation, Long>
