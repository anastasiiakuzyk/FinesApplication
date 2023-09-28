package ua.anastasiia.finesapp.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import ua.anastasiia.finesapp.validator.IntListValidatorConstraint
import kotlin.reflect.KClass

@Constraint(validatedBy = [IntListValidatorConstraint::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class IntListValidator(
    val min: Int,
    val max: Int,
    val message: String = "values ({invalidValues}) must be between {min} and {max}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
