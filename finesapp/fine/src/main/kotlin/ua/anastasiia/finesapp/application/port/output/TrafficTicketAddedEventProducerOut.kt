package ua.anastasiia.finesapp.application.port.output

import ua.anastasiia.finesapp.domain.Fine

interface TrafficTicketAddedEventProducerOut {
    fun sendEvent(fine: Fine, trafficTicketId: Fine.TrafficTicket)
}
