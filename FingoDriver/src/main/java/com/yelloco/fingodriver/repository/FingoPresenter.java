package com.yelloco.fingodriver.repository;

import android.app.Activity;

import com.yelloco.fingodriver.FingoSDK;
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
    private boolean canProceed;

    public FingoPresenter(Activity activity, FingoContract.FingoListener fingoListener){
        this.activity = activity;
        this.fingoListener = fingoListener;
        this.model = new FingoModel(this, activity);
    }

    public void identify(){
        identify(true);
    }

    public void identify(boolean forceOnline){
        execute(FingoOperation.IDENTIFY, forceOnline);
    }

    public void enroll(){
        enroll(true);
    }

    public void enroll(boolean forceOnline){
        execute(FingoOperation.ENROLLMENT, forceOnline);
    }

    public void payment(int totalAmount, FingoCurrency fingoCurrency, int totalDiscount, PosData posData){
        payment(totalAmount, fingoCurrency, totalDiscount, posData, true);
    }

    public void payment(int totalAmount, FingoCurrency fingoCurrency, int totalDiscount, PosData posData, boolean forceOnline){
        new Thread(() -> {
            this.model.invoke(FingoOperation.PAYMENT, forceOnline);
            if(! this.model.isOperationCancelled()){
                this.model.payment(totalAmount, fingoCurrency, totalDiscount, posData, forceOnline);
            }
        }).start();
    }

    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData){
        refund(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData, true);
    }

    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, boolean forceOnline){
        new Thread(() -> {
            this.model.invoke(FingoOperation.REFUND, forceOnline);
            if(! this.model.isOperationCancelled()){
                this.model.refund(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData, forceOnline);
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

    private void execute(FingoOperation fingoOperation, boolean forceOnline){
        new Thread(() -> {
            this.model.invoke(fingoOperation, forceOnline);
            if(! this.model.isOperationCancelled()){
                switch (fingoOperation){
                    case IDENTIFY:{
                        this.model.identify(forceOnline);
                        break;
                    }
                    case ENROLLMENT:{
                        this.model.enroll(forceOnline);
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
