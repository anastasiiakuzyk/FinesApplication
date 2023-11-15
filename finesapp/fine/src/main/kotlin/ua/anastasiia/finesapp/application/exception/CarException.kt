package ua.anastasiia.finesapp.application.exception

open class CarException(override val message: String) : RuntimeException()

class CarPlateNotFoundException(plate: String) : CarException("Car with plate $plate not found")

class CarPlateDuplicateException(plate: String) : CarException("Car with plate $plate already exists")

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
object CarsNotFoundException : CarException("No cars where found.")
