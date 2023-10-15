package ua.anastasiia.finesapp

import io.nats.client.Dispatcher
import io.nats.client.Message
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineRequest
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineResponse
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CreateFineNatsControllerTest : NatsControllerTest() {

    @Test
    fun testCreatedFine() {
        val fineToCreate = getFineToSave().toProto()

        val createdEvent = connection.subscribe(NatsSubject.Fine.getCreatedEventSubject(fineToCreate.car.plate))

        val expectedResponse = CreateFineResponse
            .newBuilder()
            .apply {
                successBuilder.setFine(fineToCreate)
            }.build()
        val actualResponse = sendRequestAndParseResponse(
            subject = NatsSubject.Fine.CREATE,
            request = CreateFineRequest.newBuilder().setFine(fineToCreate).build(),
            parser = CreateFineResponse::parseFrom
        )
        assertEquals(expectedResponse, actualResponse)

        val expectedEvent = FineCreatedEvent.newBuilder().setFine(fineToCreate).build()
        val actualEvent = FineCreatedEvent.parseFrom(createdEvent.nextMessage(Duration.ofSeconds(10)).data)
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun testWithQueryGroup() {
        val fineToCreate = getFineToSave().toProto()
        val responseCounter = AtomicInteger(0)
        val numberOfDispatchersToCreate = 5
        val latch = CountDownLatch(1)
        lateinit var dispatcher: Dispatcher
        repeat(numberOfDispatchersToCreate) {
            dispatcher = connection.createDispatcher { message: Message ->
                val request = createFineNatsController.parser.parseFrom(message.data)
                val response = createFineNatsController.handle(request)
                responseCounter.incrementAndGet()
                connection.publish(message.replyTo, response.toByteArray())
                if (dispatcher.deliveredCount == 1L) {
                    latch.countDown()
                }
            }.subscribe(createFineNatsController.subject, "queue_test_group")
        }

        sendRequestAndParseResponse(
            subject = NatsSubject.Fine.CREATE,
            request = CreateFineRequest.newBuilder().setFine(fineToCreate).build(),
            parser = CreateFineResponse::parseFrom
        )
        latch.await(1L, TimeUnit.SECONDS)
        assertEquals(1, responseCounter.get(), "Only one responder should have replied")
    }

    @Test
    fun testWithoutQueryGroup() {
        val fineToCreate = getFineToSave().toProto()
        val responseCounter = AtomicInteger(0)
        val numberOfDispatchersToCreate = 5
        val latch = CountDownLatch(5)

        repeat(numberOfDispatchersToCreate) {
            connection.createDispatcher { message: Message ->
                val request = createFineNatsController.parser.parseFrom(message.data)
                val response = createFineNatsController.handle(request)
                responseCounter.incrementAndGet()
                connection.publish(message.replyTo, response.toByteArray())
                latch.countDown()
            }.subscribe(createFineNatsController.subject)
        }

        sendRequestAndParseResponse(
            subject = NatsSubject.Fine.CREATE,
            request = CreateFineRequest.newBuilder().setFine(fineToCreate).build(),
            parser = CreateFineResponse::parseFrom
        )
        latch.await()
        assertNotEquals(1, responseCounter.get(), "Many responders should have replied")
    }
}
