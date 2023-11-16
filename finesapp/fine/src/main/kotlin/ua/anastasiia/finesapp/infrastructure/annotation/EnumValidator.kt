package ua.anastasiia.finesapp.infrastructure.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import ua.anastasiia.finesapp.infrastructure.annotation.validator.EnumValidatorConstraint
import kotlin.reflect.KClass

@Constraint(validatedBy = [EnumValidatorConstraint::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class EnumValidator(
    val enumClass: KClass<out Enum<*>>,
    val message: String = "must be any of {enumValues}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
