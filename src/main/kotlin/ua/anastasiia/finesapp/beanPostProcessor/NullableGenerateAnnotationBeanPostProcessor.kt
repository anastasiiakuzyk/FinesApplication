package ua.anastasiia.finesapp.beanPostProcessor

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.InvocationHandler
import org.springframework.cglib.proxy.Proxy
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.dto.CarRequest
import java.lang.reflect.Method
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions

@Component
class ValidationBeanPostProcessor : BeanPostProcessor {
    val savedBeans = mutableMapOf<String, KClass<*>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean::class
        if (beanClass.java.isAnnotationPresent(Service::class.java)) {
            savedBeans[beanName] = beanClass
        }
        return bean
    }

    override fun postProcessAfterInitialization(currentBean: Any, beanName: String): Any {
        return savedBeans[beanName]?.let { originalBean ->
            Proxy.newProxyInstance(
                originalBean.java.classLoader,
                originalBean.java.interfaces,
                DynamicInvocationHandler(currentBean, originalBean)
            )
        } ?: currentBean
    }
}

@Suppress("SpreadOperator")
class DynamicInvocationHandler(private val currentBean: Any, private val originalBean: KClass<*>) : InvocationHandler {

    override operator fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any {
        if (shouldInvokeDefaultMethod(args)) {
            return method.invoke(currentBean, *args)
        }
        val carRequest = args[0] as CarRequest
        val carRequestUpdated = if (carRequest.plate == null) {
            carRequest.copy(plate = generateCarPlate())
        } else {
            carRequest
        }
        return if (hasOptionalId(args)) {
            val optionalId = args[1] as Long
            method.invoke(currentBean, carRequestUpdated, optionalId)
        } else {
            method.invoke(currentBean, carRequestUpdated)
        }
    }

    private fun shouldInvokeDefaultMethod(args: Array<out Any>): Boolean {
        return !originalBean.memberFunctions.any { beanMethod ->
            beanMethod.hasAnnotation<NullableGenerate>()
        } || args.isEmpty() || args.size > 2 || args[0] !is CarRequest || (hasOptionalId(args) && args[1] !is Long)
    }

    private fun hasOptionalId(args: Array<out Any>): Boolean {
        return args.size == 2
    }
}

@Suppress("MagicNumber")
fun generateCarPlate(): String {
    val uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZАБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ"
    val numbers = "0123456789"
    // plate must have two letters, generating them
    val plate = StringBuilder().apply {
        append(uppercaseLetters.random())
        append(uppercaseLetters.random())
    }
    // plate must have 3-8 characters, need to generate amount of them
    val randomLength = Random.nextInt(1, 7)
    val allChars = "$uppercaseLetters$numbers"

    repeat(randomLength) {
        plate.append(allChars.random())
    }
    // shuffle to not have two letters at the beginning always
    plate.shuffle()

    return plate.toString()
}

fun StringBuilder.shuffle() {
    val charList = this.toString().toMutableList()
    charList.shuffle(Random.Default)
    this.clear()
    for (ch in charList) {
        this.append(ch)
    }
}
