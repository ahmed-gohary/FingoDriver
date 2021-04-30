package com.yelloco.fingodriver.repository;

import android.app.Activity;

import com.yelloco.fingodriver.callbacks.FingoContract;
import com.yelloco.fingodriver.enums.FingoCurrency;
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

    public void identify(){
        executeIdentify();
    }

    public void enroll(int delayBetweenScanningInMilliSeconds){
        executeEnrollment(delayBetweenScanningInMilliSeconds);
    }

    public void payment(int totalAmount, FingoCurrency fingoCurrency, int totalDiscount, PosData posData){
        new Thread(() -> {
            this.model.invoke(FingoOperation.PAYMENT);
            if(! this.model.isOperationCancelled()){
                this.model.payment(totalAmount, fingoCurrency, totalDiscount, posData);
            }
        }).start();
    }

    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData){
        new Thread(() -> {
            this.model.invoke(FingoOperation.REFUND);
            if(! this.model.isOperationCancelled()){
                this.model.refund(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData);
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

    private void executeIdentify(){
        new Thread(() -> {
            this.model.invoke(FingoOperation.IDENTIFY);
            if(! this.model.isOperationCancelled()){
                this.model.identify();
            }
        }).start();
    }

    private void executeEnrollment(int delayBetweenScanningInMilliSeconds){
        new Thread(() -> {
            this.model.invoke(FingoOperation.ENROLLMENT);
            if(! this.model.isOperationCancelled()){
                this.model.enroll(delayBetweenScanningInMilliSeconds);
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
