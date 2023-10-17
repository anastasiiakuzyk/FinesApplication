package ua.anastasiia.finesapp

object NatsSubject {

    open class Subject(private val subdomain: String) {
        protected fun inputReqReply(action: String) = "input.reqreply.$subdomain.$action"
        protected fun outputPubSub(entity: String, event: String) = "output.pubsub.$subdomain.$entity.$event"
    }

    object Car : Subject("car") {
        val GET_ALL = inputReqReply("get_all")
    }

    object TrafficTicket : Subject("trafficticket") {
        private const val ADDED = "added"
        val ADD = inputReqReply("add")
        fun addedSubject(carPlate: String) = outputPubSub(carPlate, ADDED)
    }

    object Violation : Subject("violation") {
        private const val DELETED = "deleted"
        val DELETE = inputReqReply("delete")
        fun deletedSubject(carPlate: String) = outputPubSub(carPlate, DELETED)
    }

    object Fine : Subject("fine") {
        private const val CREATED = "created"
        val GET_BY_ID = inputReqReply("get_by_id")
        val GET_BY_CAR_PLATE = inputReqReply("get_by_car_plate")
        val CREATE = inputReqReply("create")
        fun createdSubject(carPlate: String) = outputPubSub(carPlate, CREATED)
    }
}
