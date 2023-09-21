package ua.anastasiia.finesapp.exception

open class CarException(override val message: String) : RuntimeException()

class CarPlateNotFoundException(plate: String) : CarException("Car with plate $plate not found")

class CarIdNotFoundException(id: Long) : CarException("Car with id $id not found")

class CarPlateDuplicateException(plate: String) : CarException("Car with plate $plate already exists")
