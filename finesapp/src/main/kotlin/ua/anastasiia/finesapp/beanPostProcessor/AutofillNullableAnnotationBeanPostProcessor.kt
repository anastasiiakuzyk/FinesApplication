package ua.anastasiia.finesapp.beanPostProcessor

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.InvocationHandler
import org.springframework.cglib.proxy.Proxy
import org.springframework.stereotype.Component
import ua.anastasiia.finesapp.annotation.AutofillNullable
import ua.anastasiia.finesapp.annotation.NullableGenerate
import ua.anastasiia.finesapp.beanPostProcessor.fieldGeneration.RandomFieldGenerator
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KClass

@Component
class AutofillNullableAnnotationBeanPostProcessor : BeanPostProcessor {
    val savedBeans = mutableMapOf<String, Class<Any>>()
    val beanMethodsToProcess = mutableMapOf<String, Map<String, Pair<Int, AutofillNullable>>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean.javaClass
        if (beanClass.isAnnotationPresent(NullableGenerate::class.java)) {
            val methodsToProcess: Set<Method> = beanClass.methods.asSequence()
                .filter { method -> method.parameters.any { it.isAnnotationPresent(AutofillNullable::class.java) } }
                .toSet()

            if (methodsToProcess.isNotEmpty()) {
                beanMethodsToProcess[beanName] = extractMethodParamsToProcess(methodsToProcess)
                savedBeans[beanName] = beanClass
            }
        }
        return bean
    }

    private fun extractMethodParamsToProcess(methods: Set<Method>): Map<String, Pair<Int, AutofillNullable>> {
        val methodParams = mutableMapOf<String, Pair<Int, AutofillNullable>>()
        methods.forEach { method ->
            method.parameters.forEachIndexed { index, parameter ->
                if (parameter.isAnnotationPresent(AutofillNullable::class.java)) {
                    val annotation = parameter.getAnnotation(AutofillNullable::class.java)
                    methodParams[method.name] = index to annotation
                }
            }
        }
        return methodParams
    }

    override fun postProcessAfterInitialization(currentBean: Any, beanName: String): Any {
        return savedBeans[beanName]?.let { originalBean ->
            beanMethodsToProcess[beanName]?.let { methodsWithAnnotatedParams ->
                Proxy.newProxyInstance(
                    originalBean.classLoader,
                    originalBean.interfaces,
                    InvocationHandler(currentBean, methodsWithAnnotatedParams)
                )
            }
        } ?: currentBean
    }
}

@Suppress("SpreadOperator")
class InvocationHandler(
    private val currentBean: Any,
    private val methodsWithAnnotatedParams: Map<String, Pair<Int, AutofillNullable>>
) : InvocationHandler {

    override operator fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any? = runCatching {
        generateValueForAnnotatedParams(method, args)
        method.invoke(currentBean, *args)
    }.getOrElse { exception -> throw (exception as InvocationTargetException).targetException }

    private fun generateValueForAnnotatedParams(method: Method, args: Array<out Any>) {
        methodsWithAnnotatedParams[method.name]?.let { (paramIndex, annotationDetails) ->
            val fieldToGenerate = annotationDetails.fieldToGenerate
            val valueProviderClass = annotationDetails.valueProvider
            val targetMethodParameter = args[paramIndex]
            updateFieldIfNull(targetMethodParameter, fieldToGenerate, valueProviderClass)
        }
    }

    private fun updateFieldIfNull(
        targetMethodParameter: Any,
        fieldName: String,
        valueProviderClass: KClass<out RandomFieldGenerator>
    ) {
        targetMethodParameter.javaClass.getDeclaredField(fieldName).apply {
            isAccessible = true
            if (get(targetMethodParameter) == null) {
                set(targetMethodParameter, generateValue(valueProviderClass))
            }
        }
    }

    private fun generateValue(valueProviderClass: KClass<out RandomFieldGenerator>): Any {
        return valueProviderClass.java.getDeclaredConstructor().newInstance().generate()
    }
}
