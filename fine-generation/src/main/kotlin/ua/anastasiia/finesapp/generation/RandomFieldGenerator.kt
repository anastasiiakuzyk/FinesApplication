package ua.anastasiia.finesapp.generation

interface RandomFieldGenerator<T> {

    fun generate(number: Int): List<T>
}
