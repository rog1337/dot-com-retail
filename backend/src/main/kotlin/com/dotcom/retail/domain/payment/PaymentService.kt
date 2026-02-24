package com.dotcom.retail.domain.payment

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.TransactionError
import com.dotcom.retail.common.service.EmailService
import com.dotcom.retail.config.properties.StripeProperties
import com.dotcom.retail.domain.order.Order
import com.dotcom.retail.domain.order.OrderService
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PaymentService(
    private val stripeProperties: StripeProperties,
    private val paymentProducer: PaymentProducer,
    private val transactionRepository: TransactionRepository,
) {

    companion object {
        const val STRIPE_SIGNATURE_HEADER = "Stripe-Signature"
        const val CURRENCY = "EUR"
    }

    fun createPaymentIntent(order: Order): PaymentIntent {
        val params = PaymentIntentCreateParams.builder()
            .setAmount(toStripeMoney(order.totalAmount))
            .setCurrency(CURRENCY)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .build()

        val paymentIntent = PaymentIntent.create(params)

        val transaction = Transaction(
            order = order,
            type = TransactionType.CHARGE,
            status = TransactionStatus.PENDING,
            amount = order.totalAmount,
            externalId = paymentIntent.id,
        )

        saveTransaction(transaction)

        return paymentIntent
    }

    fun handleStripeEvent(payload: String, sigHeader: String) {

        val event: Event = Webhook.constructEvent(payload, sigHeader, stripeProperties.webhookSecret)
        if (event.type == "payment_intent.succeeded") {
            val paymentIntent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
            paymentProducer.sendPaymentStatus(
                PaymentEvent(paymentIntent.id, TransactionStatus.SUCCESS)
            )
        } else if (event.type == "payment_intent.payment_failed") {
            val paymentIntent = event.dataObjectDeserializer.`object`.get() as PaymentIntent
            paymentProducer.sendPaymentStatus(
                PaymentEvent(paymentIntent.id, TransactionStatus.FAILED)
            )
        }
    }

    fun handleSuccess(transaction: Transaction): Transaction {
        transaction.status = TransactionStatus.SUCCESS
        return saveTransaction(transaction)
    }

    fun handleFail(transaction: Transaction): Transaction {
        transaction.status = TransactionStatus.FAILED
        return saveTransaction(transaction)
    }

    private fun toStripeMoney(value: BigDecimal): Long {
        return value.multiply(BigDecimal(100)).toLong()
    }

    fun saveTransaction(transaction: Transaction): Transaction {
        return transactionRepository.save(transaction)
    }

    fun getByExternalId(externalId: String): Transaction {
        return transactionRepository.findByExternalId(externalId)
            ?: throw AppException(TransactionError.TRANSACTION_NOT_FOUND)
    }

}