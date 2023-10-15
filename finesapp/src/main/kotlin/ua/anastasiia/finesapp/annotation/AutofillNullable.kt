package ua.anastasiia.finesapp.annotation

import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomFieldGenerator
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AutofillNullable(
    val fieldToGenerate: String,
    val valueProvider: KClass<out RandomFieldGenerator>,
)
