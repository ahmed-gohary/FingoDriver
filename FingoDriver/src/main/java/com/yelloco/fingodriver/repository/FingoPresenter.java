package com.yelloco.fingodriver.repository;

import android.app.Activity;

import com.yelloco.fingodriver.callbacks.FingoContract;
import com.yelloco.fingodriver.enums.Currency;
import com.yelloco.fingodriver.enums.FingoOperation;
import com.yelloco.fingodriver.models.FingoModel;
import com.yelloco.fingodriver.models.fingo_operation.DisplayTextRequested;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse;
import com.yelloco.fingodriver.models.networking.payment.PosData;
import com.yelloco.fingodriver.models.networking.refund.TerminalData;

public class FingoPresenter implements FingoContract.Presenter
{
    // Members
    private FingoContract.FingoListener fingoListener;
    private FingoContract.Model model;
    private Activity activity;

    public FingoPresenter(Activity activity, FingoContract.FingoListener fingoListener){
        this.activity = activity;
        this.fingoListener = fingoListener;
        this.model = new FingoModel(this, activity);
    }

    public void identify(int timeoutInMillis){
        execute(FingoOperation.IDENTIFY, timeoutInMillis);
    }

    public void enroll(int timeoutInMillis) {
        execute(FingoOperation.ENROLLMENT, timeoutInMillis);
    }

    public void payment(int totalAmount, Currency currency, int totalDiscount, PosData posData, int timeoutInMillis){
        new Thread(() -> {
            this.model.invoke(FingoOperation.PAYMENT);
            if(! this.model.isOperationCancelled()){
                this.model.payment(totalAmount, currency, totalDiscount, posData, timeoutInMillis);
            }
        }).start();
    }

    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, int timeoutInMillis){
        new Thread(() -> {
            this.model.invoke(FingoOperation.REFUND);
            if(! this.model.isOperationCancelled()){
                this.model.refund(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData, timeoutInMillis);
            }
        }).start();
    }

    @Override
    public void cancel() {
        new Thread(() -> {
            this.model.cancel();
        }).start();
    }

    @Override
    public boolean isDeviceConnected(){
        return this.model.isDeviceConnected();
    }

    @Override
    public boolean isOperationCancelled() {
        return this.model.isOperationCancelled();
    }

    private void execute(FingoOperation fingoOperation, int timeoutInMillis){
        new Thread(() -> {
            this.model.invoke(fingoOperation);
            if(! this.model.isOperationCancelled()){
                switch (fingoOperation){
                    case IDENTIFY:{
                        this.model.identify(timeoutInMillis);
                        break;
                    }
                    case ENROLLMENT:{
                        this.model.enroll(timeoutInMillis);
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onProcessingStarted() {
        this.fingoListener.onProcessingStarted();
    }

    @Override
    public void onDisplayTextRequested(DisplayTextRequested displayTextRequested){
        this.fingoListener.onDisplayTextRequested(displayTextRequested);
    }

    @Override
    public void onIdentifyData(IdentifyData identifyData) {
        this.fingoListener.onIdentifyData(identifyData);
    }

    @Override
    public void onPaymentData(PaymentData paymentData, FingoErrorResponse fingoErrorResponse) {
        this.fingoListener.onPaymentData(paymentData, fingoErrorResponse);
    }

    @Override
    public void onProcessingFinished(ProcessingFinished processingFinished) {
        activity.runOnUiThread(() -> {
            this.fingoListener.onProcessingFinished(processingFinished);
        });
    }
}
