package com.yelloco.fingodriver.callbacks;

import com.yelloco.fingodriver.enums.Currency;
import com.yelloco.fingodriver.enums.FingoOperation;
import com.yelloco.fingodriver.models.fingo_operation.DisplayTextRequested;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse;
import com.yelloco.fingodriver.models.networking.payment.PosData;
import com.yelloco.fingodriver.models.networking.refund.TerminalData;

public interface FingoContract
{
    interface FingoListener{
        void onProcessingStarted();
        void onDisplayTextRequested(DisplayTextRequested displayTextRequested);
        void onIdentifyData(IdentifyData identifyData);
        void onPaymentData(PaymentData paymentData, FingoErrorResponse fingoErrorResponse);
        void onProcessingFinished(ProcessingFinished processingFinished);
    }

    interface Presenter{
        void onProcessingStarted();
        void onDisplayTextRequested(DisplayTextRequested displayTextRequested);
        void onIdentifyData(IdentifyData identifyData);
        void onPaymentData(PaymentData paymentData, FingoErrorResponse fingoErrorResponse);
        void onProcessingFinished(ProcessingFinished processingFinished);
        void cancel();
        boolean isDeviceConnected();
        boolean isOperationCancelled();
    }

    interface Model{
        void invoke(FingoOperation fingoOperation);
        void identify(int timeoutInMillis);
        void enroll(int timeoutInMillis);
        void payment(int totalAmount, Currency currency, int totalDiscount, PosData posData, int timeoutInMillis);
        void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, int timeoutInMillis);
        void cancel();
        boolean isDeviceConnected();
        boolean isOperationCancelled();
    }
}
