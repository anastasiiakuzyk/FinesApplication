package ua.anastasiia.finesapp.beanPostProcessor

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.InvocationHandler
import org.springframework.cglib.proxy.Proxy
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.dto.CarRequest
import ua.anastasiia.finesapp.exception.NullableGenerateAnnotationNotApplicableException
import java.lang.reflect.Method
import kotlin.random.Random

@Component
class NullableGenerateAnnotationBeanPostProcessor : BeanPostProcessor {
    val savedBeans = mutableMapOf<String, Class<Any>>()
    val beanMethodsToProcess = mutableMapOf<String, Set<String>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean.javaClass
        if (beanClass.isAnnotationPresent(Service::class.java)) {
            val methodsToProcess: Set<String> =
                beanClass.methods
                    .asSequence()
                    .filter { isMethodToProcess(it) }
                    .map { it.name }
                    .toSet()
            if (methodsToProcess.isNotEmpty()) {
                savedBeans[beanName] = beanClass
                beanMethodsToProcess[beanName] = methodsToProcess
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(currentBean: Any, beanName: String): Any {
        return savedBeans[beanName]?.let { originalBean ->
            Proxy.newProxyInstance(
                originalBean.classLoader,
                originalBean.interfaces,
                DynamicInvocationHandler(currentBean, beanMethodsToProcess[beanName]!!)
            )
        } ?: currentBean
    }
}

@Suppress("SpreadOperator")
class DynamicInvocationHandler(private val currentBean: Any, private val annotatedMethodNames: Set<String>) :
    InvocationHandler {

    override operator fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any {
        if (method.name !in annotatedMethodNames) {
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

    private fun hasOptionalId(args: Array<out Any>): Boolean {
        return args.size == 2
    }
}

@Suppress("ReturnCount")
fun isMethodToProcess(beanMethod: Method): Boolean {
    val args = beanMethod.parameterTypes
    // check annotation
    if (!beanMethod.isAnnotationPresent(NullableGenerate::class.java)) {
        return false
    } else if (beanMethod.isAnnotationPresent(NullableGenerate::class.java) && !checkParams(args)) {
        throw NullableGenerateAnnotationNotApplicableException()
    }
    return checkParams(args)
}

@Suppress("ReturnCount")
private fun checkParams(args: Array<out Class<*>>): Boolean {
    // check size
    if (args.isEmpty() || args.size > 2) {
        return false
    }
    // check save() variant
    if (args[0] != CarRequest::class.java) {
        return false
    }
    // check update() variant
    if (args.size == 2) {
        return args[1] == Long::class.java
    }
    return true
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
