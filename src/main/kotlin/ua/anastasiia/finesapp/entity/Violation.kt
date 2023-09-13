package ua.anastasiia.finesapp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
data class Violation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val description: String,
    val price: Double,
    @ManyToMany(mappedBy = "violations")
    @JsonIgnore
    val fines: MutableList<Fine> = mutableListOf()
)
