package ua.anastasiia.finesapp.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ua.anastasiia.finesapp.annotation.EnumValidator

class EnumValidatorConstraint : ConstraintValidator<EnumValidator, CharSequence> {

    private val acceptedValues: MutableList<String> = mutableListOf()
    
    override fun initialize(constraintAnnotation: EnumValidator) {
        super.initialize(constraintAnnotation)
        acceptedValues.addAll(
            constraintAnnotation.enumClass.java
                .enumConstants
                .map { it.name }
        )
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        return if (value == null) {
            true
        } else {
            acceptedValues.contains(value.toString())
        }
    }
}
