package ua.anastasiia.finesapp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(
        value =
        [CarPlateNotFoundException::class, CarIdNotFoundException::class, CarPlateDuplicateException::class]
    )
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleCarExceptions(exception: CarException, request: WebRequest) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message,
            request.getDescription(false)
        )

    @ExceptionHandler(ViolationNotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleViolationExceptions(exception: ViolationNotFoundException, request: WebRequest) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message ?: "",
            request.getDescription(false)
        )

    @ExceptionHandler(FineIdNotFoundException::class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleViolationExceptions(exception: FineIdNotFoundException, request: WebRequest) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message ?: "",
            request.getDescription(false)
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleValidationsExceptions(
        exception: MethodArgumentNotValidException,
        request: WebRequest
    ): ValidationErrorMessage {
        val errors: MutableList<ValidationErrorMessage.ValidationError> = mutableListOf()
        exception.bindingResult.fieldErrors.forEach { fieldError ->
            errors.add(
                ValidationErrorMessage.ValidationError(
                    fieldError.field,
                    fieldError.defaultMessage ?: ""
                )
            )
        }
        return ValidationErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            errors,
            request.getDescription(false)
        )
    }
}
