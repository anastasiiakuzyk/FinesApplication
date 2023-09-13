package ua.anastasiia.finesapp.exception

class ViolationNotFoundException(id: Long) : RuntimeException("Violation with id $id not found")
