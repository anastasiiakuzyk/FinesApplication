package ua.anastasiia.finesapp.application.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(
        value =
        [CarPlateNotFoundException::class, CarPlateDuplicateException::class, CarsNotFoundException::class]
    )
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleCarExceptions(exception: CarException, exchange: ServerWebExchange) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message,
            exchange.request.uri.path
        )

    @ExceptionHandler(
        value = [
            TrafficTicketNotFoundException::class,
            TrafficTicketWithViolationNotFoundException::class,
        ]
    )
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleTrafficTicketExceptions(exception: TrafficTicketException, exchange: ServerWebExchange) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message,
            exchange.request.uri.path
        )

    @ExceptionHandler(
        value =
        [
            FineIdNotFoundException::class,
            FinesInLocationNotFound::class,
            NoFinesFoundException::class,
            NoFinesFoundByDateException::class
        ]
    )
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleViolationExceptions(exception: FineException, exchange: ServerWebExchange) =
        ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now(),
            exception.message,
            exchange.request.uri.path
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleValidationsExceptions(
        exception: MethodArgumentNotValidException,
        exchange: ServerWebExchange
    ): ValidationErrorMessage {
        val errors: MutableList<ValidationErrorMessage.ValidationError> = mutableListOf()
        exception.bindingResult.fieldErrors.forEach { fieldError ->
            errors.add(
                ValidationErrorMessage.ValidationError(
                    fieldError.field,
                    fieldError.defaultMessage.orEmpty()
                )
            )
        }
        return ValidationErrorMessage(
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            errors,
            exchange.request.uri.path
        )
    }
}
