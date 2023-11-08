package ua.anastasiia.finesapp.exception

import java.time.LocalDate

open class FineException(override val message: String) : RuntimeException()

class FineIdNotFoundException(id: Any) : FineException("Fine with id $id not found")

class FinesInLocationNotFound(longitude: Double, latitude: Double) :
    FineException("No fines in location ($longitude, $latitude)")

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object NoFinesFoundException : FineException("No fines where found")

class NoFinesFoundByDateException(localDate: LocalDate) : FineException("No fines where found by date: $localDate")
