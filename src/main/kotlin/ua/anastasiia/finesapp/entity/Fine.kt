package ua.anastasiia.finesapp.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
data class Fine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val longitude: Double,
    val latitude: Double,
    @Column(name = "date_time")
    val dateTime: LocalDateTime,
    @Column(name = "photo_url")
    val photoUrl: String,
    @ManyToOne
    @JoinColumn(name = "car_id")
    var car: Car? = null,
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "fine_violation",
        joinColumns = [JoinColumn(name = "fine_id")],
        inverseJoinColumns = [JoinColumn(name = "violation_id")]
    )
    val violations: MutableList<Violation> = mutableListOf()
)
