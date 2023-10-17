package ua.anastasiia.finesapp

import io.nats.client.Dispatcher
import io.nats.client.Message
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ua.anastasiia.finesapp.dto.toProto
import ua.anastasiia.finesapp.exception.CarPlateDuplicateException
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineRequest
import ua.anastasiia.finesapp.input.reqreply.fine.CreateFineResponse
import ua.anastasiia.finesapp.output.pubsub.fine.FineCreatedEvent
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CreateFineNatsControllerTest : NatsControllerTest() {

    @Test
    fun `verify fine creation and corresponding event publication`() {
        val fineToCreate = getFineToSaveGeneratedCarPlate().toProto()

        val createdEvent = connection.subscribe(NatsSubject.Fine.createdSubject(fineToCreate.car.plate))

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
    fun `verify failure when creating fine with existing car plate`() {
        val fine = getFineToSave()
        val fineToCreate = fine.toProto()
        fineRepository.saveFine(fine)
        val request = CreateFineRequest.newBuilder().setFine(fineToCreate).build()
        val response = createFineNatsController.handle(request)
        assertTrue(response.hasFailure())
        assertEquals(
            CarPlateDuplicateException(fine.car.plate).message,
            response.failure.carPlateDuplicateError.message
        )
    }

    @Test
    fun `ensure single dispatcher response in queue group`() {
        val fineToCreate = getFineToSaveGeneratedCarPlate().toProto()
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
    fun `ensure multiple dispatcher responses without queue group`() {
        val fineToCreate = getFineToSaveGeneratedCarPlate().toProto()
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
