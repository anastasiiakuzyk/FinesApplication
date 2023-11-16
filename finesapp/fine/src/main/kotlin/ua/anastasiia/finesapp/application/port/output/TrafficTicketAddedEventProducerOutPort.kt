package ua.anastasiia.finesapp.application.port.output

import ua.anastasiia.finesapp.domain.Fine

interface TrafficTicketAddedEventProducerOutPort {
    fun sendEvent(fine: Fine, trafficTicketId: Fine.TrafficTicket)
}
