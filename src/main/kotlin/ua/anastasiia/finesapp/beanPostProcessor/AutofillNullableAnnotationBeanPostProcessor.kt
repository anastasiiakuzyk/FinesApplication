package ua.anastasiia.finesapp.beanPostProcessor

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.cglib.proxy.InvocationHandler
import org.springframework.cglib.proxy.Proxy
import org.springframework.stereotype.Component
import org.springframework.util.ReflectionUtils
import ua.anastasiia.finesapp.annotation.AutofillNullable
import ua.anastasiia.finesapp.annotation.NullableGenerate
import java.lang.reflect.Method

@Component
class AutofillNullableAnnotationBeanPostProcessor : BeanPostProcessor {
    val savedBeans = mutableMapOf<String, Class<Any>>()
    val beanMethodsToProcess = mutableMapOf<String, Map<String, Pair<Int, String>>>()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        val beanClass = bean.javaClass
        if (beanClass.isAnnotationPresent(NullableGenerate::class.java)) {
            val methodsToProcess: Set<Method> = beanClass.methods.asSequence()
                .filter { method -> method.parameters.any { it.isAnnotationPresent(AutofillNullable::class.java) } }
                .toSet()

            if (methodsToProcess.isNotEmpty()) {
                methodsToProcess.forEach { method ->
                    method.parameters
                        .filter { it.isAnnotationPresent(AutofillNullable::class.java) }
                        .forEachIndexed { i, parameter ->
                            val param = i to parameter.getAnnotation(AutofillNullable::class.java).fieldToGenerate
                            val methodParams: MutableMap<String, Pair<Int, String>> = mutableMapOf()
                            methodParams[method.name] = param
                            beanMethodsToProcess[beanName] = methodParams
                        }
                }
                savedBeans[beanName] = beanClass
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(currentBean: Any, beanName: String): Any {
        return savedBeans[beanName]?.let { originalBean ->
            Proxy.newProxyInstance(
                originalBean.classLoader,
                originalBean.interfaces,
                InvocationHandler(currentBean, beanMethodsToProcess[beanName]!!)
            )
        } ?: currentBean
    }
}

@Suppress("SpreadOperator")
class InvocationHandler(
    private val currentBean: Any,
    private val annotatedMethodNames: Map<String, Pair<Int, String>>
) :
    InvocationHandler {

    override operator fun invoke(proxy: Any?, method: Method, args: Array<out Any>): Any {
        if (method.name !in annotatedMethodNames) {
            return method.invoke(currentBean, *args)
        }
        val param = annotatedMethodNames[method.name]
        val paramIndex = param!!.first
        val fieldToGenerateName = param.second

        val anyArg = args[paramIndex]
        val fieldToGenerate = anyArg.javaClass.getDeclaredField(fieldToGenerateName)
        fieldToGenerate.setAccessible(true)
        if (fieldToGenerate.get(anyArg) == null) {
            ReflectionUtils.setField(fieldToGenerate, anyArg, generateCarPlate())
        }
        return method.invoke(currentBean, *args)
    }
}
