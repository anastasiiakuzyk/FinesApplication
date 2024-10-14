package ua.anastasiia.finesapp.application.port.output

import ua.anastasiia.finesapp.domain.Fine

interface FineCreatedProducerOutPort {
    fun sendEvent(fine: Fine)
}
