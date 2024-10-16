package ua.anastasiia.finesapp.infrastructure.s3.service

import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3Service(private val s3Client: S3Client) {

    fun uploadFile(key: String, fileContent: ByteArray):String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket("fine-car-images")
            .key(key)
            .build()

        return s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent)).responseMetadata().toString()
    }
}
