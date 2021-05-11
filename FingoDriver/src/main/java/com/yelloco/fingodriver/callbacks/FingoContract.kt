package com.yelloco.fingodriver.callbacks

import com.yelloco.fingodriver.enums.Currency
import com.yelloco.fingodriver.enums.FingoOperation
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData
import com.yelloco.fingodriver.models.fingo_operation.PaymentData
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayTextRequested
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse
import com.yelloco.fingodriver.models.networking.payment.PosData
import com.yelloco.fingodriver.models.networking.refund.TerminalData

interface FingoContract {
    interface FingoListener {
        fun onProcessingStarted()
        fun onDisplayTextRequested(displayTextRequested: DisplayTextRequested?)
        fun onIdentifyData(identifyData: IdentifyData?)
        fun onPaymentData(paymentData: PaymentData?, fingoErrorResponse: FingoErrorResponse?)
        fun onProcessingFinished(processingFinished: ProcessingFinished?)
    }

    interface Presenter {
        fun onProcessingStarted()
        fun onDisplayTextRequested(displayTextRequested: DisplayTextRequested?)
        fun onIdentifyData(identifyData: IdentifyData?)
        fun onPaymentData(paymentData: PaymentData?, fingoErrorResponse: FingoErrorResponse?)
        fun onProcessingFinished(processingFinished: ProcessingFinished?)
        fun cancel()
        val isDeviceConnected: Boolean
        val isOperationCancelled: Boolean
    }

    interface Model {
        operator fun invoke(fingoOperation: FingoOperation?)
        fun identify(timeoutInMillis: Int)
        fun enroll(timeoutInMillis: Int)
        fun payment(
            totalAmount: Int,
            currency: Currency?,
            totalDiscount: Int,
            posData: PosData?,
            timeoutInMillis: Int
        )

        fun refund(
            refundAmount: Int,
            transactionIdToRefund: String?,
            gatewayTransactionIdToRefund: String?,
            terminalData: TerminalData?,
            timeoutInMillis: Int
        )

        fun cancel()
        val isDeviceConnected: Boolean
        val isOperationCancelled: Boolean
    }
}