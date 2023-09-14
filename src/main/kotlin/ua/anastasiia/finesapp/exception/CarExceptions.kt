package ua.anastasiia.finesapp.exception

class CarPlateNotFoundException(plate: String) : RuntimeException("Car with plate $plate not found")
class CarIdNotFoundException(id: Long) : RuntimeException("Car with id $id not found")
class CarPlateDuplicateException(plate: String) : RuntimeException("Car with plate $plate already exists")
