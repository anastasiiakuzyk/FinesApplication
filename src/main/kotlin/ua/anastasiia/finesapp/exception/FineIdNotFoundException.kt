package ua.anastasiia.finesapp.exception

class FineIdNotFoundException(id: Long) : RuntimeException("Fine with id $id not found")
