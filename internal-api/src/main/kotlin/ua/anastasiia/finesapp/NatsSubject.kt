package ua.anastasiia.finesapp

object NatsSubject {
    private const val INPUT_PORT = "input"
    private const val OUTPUT_PORT = "output"
    private const val REQREPLY_COMMUNICATION_TYPE = "reqreply"
    private const val PUBSUB_COMMUNICATION_TYPE = "pubsub"

    open class Subject(private val subdomain: String) {
        protected fun inputReqReply(action: String) = "$INPUT_PORT.$REQREPLY_COMMUNICATION_TYPE.$subdomain.$action"
        protected fun outputPubSub(action: String) = "$OUTPUT_PORT.$PUBSUB_COMMUNICATION_TYPE.$subdomain.$action"
    }

    object Car : Subject("car") {
        val GET_ALL = inputReqReply("get_all")
    }

    object TrafficTicket : Subject("trafficticket") {
        private const val ADDED = ".added"

        val ADD = inputReqReply("add")

        fun getAddedEventSubject(carPlate: String): String {
            return "${outputPubSub(carPlate)}$ADDED"
        }
    }

    object Violation : Subject("violation") {
        private const val DELETED = ".deleted"

        val DELETE = inputReqReply("delete")

        fun getDeletedEventSubject(carPlate: String): String {
            return "${outputPubSub(carPlate)}$DELETED"
        }
    }

    object Fine : Subject("fine") {
        private const val CREATED = ".created"

        val GET_BY_ID = inputReqReply("get_by_id")
        val GET_BY_CAR_PLATE = inputReqReply("get_by_car_plate")
        val CREATE = inputReqReply("create")

        fun getCreatedEventSubject(carPlate: String): String {
            return "${outputPubSub(carPlate)}$CREATED"
        }
    }
}
