package ua.anastasiia.finesapp.infrastructure.rest


import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ua.anastasiia.finesapp.infrastructure.s3.service.S3Service

@RestController
@RequestMapping("/photo")
class S3Controller(private val s3Service: S3Service) {

    @PostMapping("/upload/key/{key}")
    fun uploadFile(@RequestParam("file") file: MultipartFile, @PathVariable key: String): String {
        s3Service.uploadFile(key, file.bytes)
        return key
    }
}

