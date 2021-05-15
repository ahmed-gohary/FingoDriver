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
import com.yelloco.fingodriver.callbacks.FingoRequestLogger;
import com.yelloco.fingodriver.enums.Currency;
import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayTextRequested;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse;
import com.yelloco.fingodriver.models.networking.payment.PosData;
import com.yelloco.fingodriver.models.networking.refund.TerminalData;
import com.yelloco.fingodriver.repository.FingoPresenter;
import com.yelloco.fingodriver.utils.FingoParams;

public class MainActivity extends AppCompatActivity implements FingoContract.FingoListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TIMEOUT = 15000;

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

        FingoParams fingoParams = new FingoParams();
        fingoParams.setCloudUrl("https://sandbox.fingo.to/api/");
        fingoParams.setPartnerId("kan-dev");
        fingoParams.setMerchantId("1dd56035-d914-44bb-b806-3b85f714fa91");
        fingoParams.setTerminalId("POS-540-002");
        fingoParams.setApiKey("1761900a-bc4b-4406-a0e4-eae4df1a38cd");
        fingoParams.setTemplateKeySeed("FvCoreSample1");

        FingoSDK.INSTANCE.initialize(this, new FingoRequestLogger() {
            @Override
            public void onLogDataAvailable(String s) {
                Log.d(TAG, "onLogDataAvailable: " + s);
            }
        });

        FingoErrorCode fingoErrorCode = FingoSDK.INSTANCE.setFingoParams(fingoParams);

        Log.d(TAG, "setFingoParams() returned: " + fingoErrorCode);

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
                fingoPresenter.identify(TIMEOUT);
            }
        });

        enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fingoPresenter.enroll(TIMEOUT);
            }
        });

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PosData posData = new PosData("2", "Cairo");
                fingoPresenter.payment(200, Currency.GBP, 0,posData, TIMEOUT);
            }
        });

        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TerminalData terminalData = new TerminalData();
                terminalData.setLocation("Cairo");
                fingoPresenter.refund(100, "8c04ad1b-e1e8-4752-b50c-e3c9dc70ad11", "96577222", terminalData, TIMEOUT);
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
        Log.d(TAG, "onDisplayTextRequested: " + displayTextRequested.getType());
        Log.d(TAG, "onDisplayTextRequested: " + displayTextRequested.getText());
        Log.d(TAG, "onDisplayTextRequested: " + displayTextRequested.getCode());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                feedbackText.setText(displayTextRequested.getText());
            }
        });
    }

    @Override
    public void onIdentifyData(IdentifyData identifyData) {
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getMemberId());
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getVeinId());
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getVerificationTemplate());
        Log.d(TAG, "onOnlineIdentifyData: " + identifyData.getEnrolmentTemplate());
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
        Log.d(TAG, "onProcessingFinished: " + processingFinished.getStatus());

        runOnUiThread(() -> {
            progressBar.setVisibility(View.INVISIBLE);

            if(processingFinished.getStatus()){
                feedbackText.setText("Operation Accepted");
            }
            else{
                feedbackText.setText("Operation Declined");
            }
        });

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
        FingoSDK.INSTANCE.destroy();
        super.onDestroy();
    }
}