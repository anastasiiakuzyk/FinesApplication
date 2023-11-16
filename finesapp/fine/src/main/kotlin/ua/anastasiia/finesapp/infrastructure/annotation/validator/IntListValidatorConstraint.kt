package ua.anastasiia.finesapp.infrastructure.annotation.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import ua.anastasiia.finesapp.infrastructure.annotation.IntListValidator

class IntListValidatorConstraint : ConstraintValidator<IntListValidator, List<Int>> {
    private var min: Int = 0
    private var max: Int = 0
    private lateinit var messageTemplate: String

    override fun initialize(constraintAnnotation: IntListValidator) {
        super.initialize(constraintAnnotation)
        min = constraintAnnotation.min
        max = constraintAnnotation.max
        messageTemplate = constraintAnnotation.message
    }

    override fun isValid(intList: List<Int>?, context: ConstraintValidatorContext): Boolean {
        if (intList == null) {
            return true
        }
        val listOfInvalid = intList.filter { i: Int -> i !in min..max }.toList()
        if (listOfInvalid.isNotEmpty()) {
            val invalidValuesStr = listOfInvalid.joinToString()
            with(context) {
                disableDefaultConstraintViolation()
                buildConstraintViolationWithTemplate(
                    messageTemplate.replace("{invalidValues}", invalidValuesStr)
                ).addConstraintViolation()
            }
        }
        return listOfInvalid.isEmpty()
    }
}
