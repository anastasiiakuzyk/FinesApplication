package ua.anastasiia.finesapp.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class AutofillNullable(val fieldToGenerate: String)
