package ua.anastasiia.finesapp.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ua.anastasiia.finesapp.annotation.EnumValidator

class EnumValidatorConstraint : ConstraintValidator<EnumValidator, CharSequence> {

    private val acceptedValues: MutableList<String> = mutableListOf()
    private lateinit var messageTemplate: String

    override fun initialize(constraintAnnotation: EnumValidator) {
        super.initialize(constraintAnnotation)
        acceptedValues.addAll(
            constraintAnnotation.enumClass.java
                .enumConstants
                .map { it.name }
        )
        messageTemplate = constraintAnnotation.message
    }

    override fun isValid(value: CharSequence?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        val isValid = value.toString().uppercase() in acceptedValues
        if (!isValid) {
            val enumValuesStr = acceptedValues.joinToString()
            with(context) {
                disableDefaultConstraintViolation()
                buildConstraintViolationWithTemplate(
                    messageTemplate.replace("{enumValues}", enumValuesStr)
                ).addConstraintViolation()
            }
        }
        return isValid
    }
}
