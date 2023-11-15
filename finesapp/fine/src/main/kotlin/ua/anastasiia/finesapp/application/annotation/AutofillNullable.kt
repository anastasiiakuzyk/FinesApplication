package ua.anastasiia.finesapp.application.annotation

import ua.anastasiia.finesapp.infrastructure.config.bpp.fieldGeneration.RandomFieldGenerator
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AutofillNullable(
    val fieldToGenerate: String,
    val valueProvider: KClass<out RandomFieldGenerator>,
)
