package com.yelloco.fingodriver.repository

import android.content.Context
import android.util.Log
import com.yelloco.fingodriver.FingoFactory
import com.yelloco.fingodriver.callbacks.FingoContract
import com.yelloco.fingodriver.enums.Currency
import com.yelloco.fingodriver.enums.FingoOperation
import com.yelloco.fingodriver.models.FingoModel
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData
import com.yelloco.fingodriver.models.fingo_operation.PaymentData
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayTextRequested
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse
import com.yelloco.fingodriver.models.networking.payment.PosData
import com.yelloco.fingodriver.models.networking.refund.TerminalData
import kotlinx.coroutines.launch

class FingoPresenter(
    context: Context, // Members
    private val fingoListener: FingoContract.FingoListener
) : FingoContract.Presenter
{
    private val model: FingoContract.Model

    init {
        model = FingoModel(this, context)
    }

    fun identify(timeoutInMillis: Long) {
        execute(FingoOperation.IDENTIFY, timeoutInMillis)
    }

    fun enroll(timeoutInMillis: Long) {
        execute(FingoOperation.ENROLLMENT, timeoutInMillis)
    }

    fun payment(
        totalAmount: Int,
        currency: Currency?,
        totalDiscount: Int,
        posData: PosData?,
        timeoutInMillis: Long
    ) {
        FingoFactory.IO_SCOPE.launch {
            launch {
                model.invoke(FingoOperation.PAYMENT)
                if (!model.isOperationCancelled) {
                    model.payment(
                        totalAmount,
                        currency,
                        totalDiscount,
                        posData,
                        timeoutInMillis
                    )
                }
            }.invokeOnCompletion {
                if(it == null){
                    Log.d(TAG, "Payment Job completed successfully")
                }
                else{
                    Log.d(TAG, "Payment Job Failed")
                }
            }
        }
    }

    fun refund(
        refundAmount: Int,
        transactionIdToRefund: String?,
        gatewayTransactionIdToRefund: String?,
        terminalData: TerminalData?,
        timeoutInMillis: Long
    ) {
        FingoFactory.IO_SCOPE.launch {
            launch {
                model.invoke(FingoOperation.REFUND)
                if (!model.isOperationCancelled) {
                    model.refund(
                        refundAmount,
                        transactionIdToRefund,
                        gatewayTransactionIdToRefund,
                        terminalData,
                        timeoutInMillis
                    )
                }
            }.invokeOnCompletion {
                if(it == null){
                    Log.d(TAG, "Refund Job completed successfully")
                }
                else{
                    Log.d(TAG, "Refund Job Failed")
                }
            }
        }
    }

    override fun cancel() {
        Thread { model.cancel() }.start()
    }

    override val isDeviceConnected: Boolean
        get() = model.isDeviceConnected
    override val isOperationCancelled: Boolean
        get() = model.isOperationCancelled

    private fun execute(fingoOperation: FingoOperation, timeoutInMillis: Long) {
        FingoFactory.IO_SCOPE.launch {
            launch {
                model.invoke(fingoOperation)
                if (!model.isOperationCancelled) {
                    when (fingoOperation) {
                        FingoOperation.IDENTIFY -> {
                            model.identify(timeoutInMillis)
                        }
                        FingoOperation.ENROLLMENT -> {
                            model.enroll(timeoutInMillis)
                        }
                    }
                }
            }.invokeOnCompletion {
                if(it == null){
                    Log.d(TAG, "${fingoOperation.name} Job completed successfully")
                }
                else{
                    Log.d(TAG, "${fingoOperation.name} Job Failed")
                }
            }
        }
    }

    override fun onProcessingStarted() {
        fingoListener.onProcessingStarted()
    }

    override fun onDisplayTextRequested(displayTextRequested: DisplayTextRequested?) {
        fingoListener.onDisplayTextRequested(displayTextRequested)
    }

    override fun onIdentifyData(identifyData: IdentifyData?) {
        fingoListener.onIdentifyData(identifyData)
    }

    override fun onPaymentData(paymentData: PaymentData?, fingoErrorResponse: FingoErrorResponse?) {
        fingoListener.onPaymentData(paymentData, fingoErrorResponse)
    }

    override fun onProcessingFinished(processingFinished: ProcessingFinished?) {
        fingoListener.onProcessingFinished(processingFinished)
    }

    companion object {
        private const val TAG = "FingoPresenter"
    }
}