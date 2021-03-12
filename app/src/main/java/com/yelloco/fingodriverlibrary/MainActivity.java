package com.yelloco.fingodriverlibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yelloco.fingodriver.FingoPayDriver;
import com.yelloco.fingodriver.FingoSDK;
import com.yelloco.fingodriver.callbacks.FingoContract;
import com.yelloco.fingodriver.enums.FingoCurrency;
import com.yelloco.fingodriver.models.fingo_operation.DisplayTextRequested;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse;
import com.yelloco.fingodriver.models.networking.payment.PosData;
import com.yelloco.fingodriver.models.networking.refund.TerminalData;
import com.yelloco.fingodriver.repository.FingoPresenter;

public class MainActivity extends AppCompatActivity implements FingoContract.FingoListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private LinearLayout loadingLayout;
    private LinearLayout buttonsLayout;
    private TextView feedbackText;
    private ProgressBar progressBar;
    private Button identify;
    private Button enroll;
    private Button payment;
    private Button refund;

    private FingoPresenter fingoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FingoSDK.initialize(this);

        initViews();
        setupListeners();
    }

    private void initViews(){
        loadingLayout = findViewById(R.id.loadingLayout);
        feedbackText = findViewById(R.id.feedbackText);
        progressBar = findViewById(R.id.progressBar);
        buttonsLayout = findViewById(R.id.buttonsLayout);
        identify = findViewById(R.id.identify);
        enroll = findViewById(R.id.enroll);
        payment = findViewById(R.id.payment);
        refund = findViewById(R.id.refund);
        fingoPresenter = new FingoPresenter(this, this);
    }

    private void setupListeners(){
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fingoPresenter.identify(true);
            }
        });

        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fingoPresenter.enroll(true);
            }
        });

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PosData posData = new PosData("2", "Cairo");
                fingoPresenter.payment(200, FingoCurrency.GBP, 0,posData, true);
            }
        });

        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TerminalData terminalData = new TerminalData();
                terminalData.setLocation("Cairo");
                fingoPresenter.refund(100, "8c04ad1b-e1e8-4752-b50c-e3c9dc70ad11", "96577222", terminalData, true);
            }
        });
    }

    @Override
    public void onProcessingStarted() {
        Log.i(TAG, "onProcessingStarted");
        runOnUiThread(() -> {
            feedbackText.setText("");
            progressBar.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
            buttonsLayout.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onDisplayTextRequested(DisplayTextRequested displayTextRequested) {
        Log.d(TAG, "onDisplayTextRequested: " + displayTextRequested.getText());
        runOnUiThread(() -> {
            feedbackText.setText(displayTextRequested.getText());
        });
    }

    @Override
    public void onIdentifyData(IdentifyData identifyData) {
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.isOnlineData());
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getMemberId());
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getVeinId());
    }

    @Override
    public void onPaymentData(PaymentData paymentData, FingoErrorResponse fingoErrorResponse) {
        Log.d(TAG, "onOnlinePaymentData: " + paymentData.getTransactionId());
        Log.d(TAG, "onOnlinePaymentData: " + paymentData.getGatewayAuthCode());
        Log.d(TAG, "onOnlinePaymentData: " + paymentData.getGatewayTransactionId());
        Log.d(TAG, "onOnlinePaymentData: " + paymentData.getMaskedCardNumber());
        Log.d(TAG, "onOnlinePaymentData: " + paymentData.getTimestamp());
        Log.d(TAG, "onOnlinePaymentData: " + fingoErrorResponse.getFingoErrorList().get(0).getErrorCode());
        Log.d(TAG, "onOnlinePaymentData: " + fingoErrorResponse.getFingoErrorList().get(0).getErrorMessage());
    }

    @Override
    public void onProcessingFinished(ProcessingFinished processingFinished) {
        Log.d(TAG, "onProcessingFinished: " + processingFinished.getText());
        Log.d(TAG, "onProcessingFinished: " + processingFinished.getErrorName());
        Log.d(TAG, "onProcessingFinished: " + processingFinished.getErrorCode());
        Log.d(TAG, "onProcessingFinished: " + processingFinished.isStatus());

        progressBar.setVisibility(View.INVISIBLE);

        if(processingFinished.isStatus()){
            feedbackText.setText("Operation Accepted");
        }
        else{
            feedbackText.setText("Operation Declined");
        }

        new Thread(() -> {
            SystemClock.sleep(3000);
            runOnUiThread(() -> {
                loadingLayout.setVisibility(View.INVISIBLE);
                buttonsLayout.setVisibility(View.VISIBLE);
            });
        }).start();

    }

    @Override
    protected void onDestroy() {
        FingoSDK.destroy();
        super.onDestroy();
    }
}