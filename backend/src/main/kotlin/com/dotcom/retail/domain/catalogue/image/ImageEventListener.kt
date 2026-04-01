package com.dotcom.retail.domain.catalogue.image

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ImageEventListener(val imageService: ImageService) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    fun handleImageDeletion(event: ImageDeletionEvent) {
        event.filePaths.forEach(imageService::deleteFile)
    }
}