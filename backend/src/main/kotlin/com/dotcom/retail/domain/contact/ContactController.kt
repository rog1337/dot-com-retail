package com.dotcom.retail.domain.contact

import com.dotcom.retail.common.constants.ApiRoutes
import com.dotcom.retail.common.service.EmailService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(ApiRoutes.Contact.BASE)
class ContactController(private val emailService: EmailService) {
    @PostMapping
    fun sendEmail(@RequestBody request: ContactRequest): ResponseEntity<Void> {
        emailService.sendContactEmail(request)
        return ok().build()
    }
}