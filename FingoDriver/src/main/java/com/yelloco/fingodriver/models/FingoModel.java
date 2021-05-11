package com.yelloco.fingodriver.models;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yelloco.fingodriver.FingoConstants;
import com.yelloco.fingodriver.FingoPayDriver;
import com.yelloco.fingodriver.FingoSDK;
import com.yelloco.fingodriver.R;
import com.yelloco.fingodriver.callbacks.FingoContract;
import com.yelloco.fingodriver.enums.Currency;
import com.yelloco.fingodriver.enums.FingoKeys;
import com.yelloco.fingodriver.enums.FingoOperation;
import com.yelloco.fingodriver.enums.StorageKey;
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayMsgCode;
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayTextRequested;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentApi;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentRequest;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentResponse;
import com.yelloco.fingodriver.models.networking.FingoRequestHelper;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorObject;
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse;
import com.yelloco.fingodriver.models.networking.identify.IdentifyApi;
import com.yelloco.fingodriver.models.networking.identify.IdentifyRequest;
import com.yelloco.fingodriver.models.networking.identify.IdentifyResponse;
import com.yelloco.fingodriver.enums.FingoErrorCode;
import com.yelloco.fingodriver.models.networking.payment.PaymentApi;
import com.yelloco.fingodriver.models.networking.payment.PaymentRequest;
import com.yelloco.fingodriver.models.networking.payment.PaymentResponse;
import com.yelloco.fingodriver.models.networking.payment.PosData;
import com.yelloco.fingodriver.models.networking.refund.RefundApi;
import com.yelloco.fingodriver.models.networking.refund.RefundRequest;
import com.yelloco.fingodriver.models.networking.refund.RefundResponse;
import com.yelloco.fingodriver.models.networking.refund.TerminalData;
import com.yelloco.fingodriver.utils.Storage;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FingoModel implements FingoContract.Model
{
    private static final String TAG = FingoModel.class.getSimpleName();

    // Members
    private Context context;
    private FingoContract.Presenter presenter;
    private boolean canProceed;
    private boolean operationCancelled;
    private FingoPayDriver fingoPayDriver;
    private FingoRequestHelper fingoRequestHelper;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private boolean retrofitInitialized;
    private EnrollmentApi enrollmentApi;
    private Call<EnrollmentResponse> enrollmentResponseCall;
    private IdentifyApi identifyApi;
    private Call<IdentifyResponse> identifyResponseCall;
    private PaymentApi paymentApi;
    private Call<PaymentResponse> paymentResponseCall;
    private RefundApi refundApi;
    private Call<RefundResponse> refundResponseCall;

    public FingoModel(FingoContract.Presenter presenter, Context context){
        this.context = context;
        this.presenter = presenter;
        this.canProceed = (FingoSDK.isSdkInitialized() && Storage.getInstance().getBoolean(StorageKey.PARAMS_STATUS.name(), false));
        this.fingoPayDriver = FingoPayDriver.getInstance();
        this.fingoRequestHelper = new FingoRequestHelper();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NotNull String data) {
                if(FingoSDK.fingoRequestLogger != null){
                    FingoSDK.fingoRequestLogger.onLogDataAvailable(data);
                }
                else{
                    Log.d(TAG, data);
                }
            }
        });
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.HEADERS);
        httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
    }

    public void invoke(FingoOperation fingoOperation){
        Log.i(TAG, "Starting FingoOperation: " + fingoOperation.name());

        operationCancelled = false;
        this.presenter.onProcessingStarted();
        this.canProceed = (FingoSDK.isSdkInitialized() && Storage.getInstance().getBoolean(StorageKey.PARAMS_STATUS.name(), false));

        if(canProceed && !retrofitInitialized){
            retrofitInitialized = true;
            this.retrofit = new Retrofit.Builder()
                    .baseUrl(fingoRequestHelper.getFingoCloudBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            enrollmentApi = retrofit.create(EnrollmentApi.class);
            identifyApi = retrofit.create(IdentifyApi.class);
            paymentApi = retrofit.create(PaymentApi.class);
            refundApi = retrofit.create(RefundApi.class);
        }
        else{
            Log.e(TAG, "Can't Proceed, Not Initializing Networking: " + canProceed
            + " " + retrofitInitialized);
        }

        if(! this.canProceed){
            if(!Storage.getInstance().getBoolean(StorageKey.PARAMS_STATUS.name(), false)){
                this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, FingoErrorCode.H1_SDK_PARAMS_NOT_SET));
            }
            else{
                this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, FingoErrorCode.H1_SDK_INIT_FAILED_BLOCKED));
            }
            this.operationCancelled = true;
        }
        else{
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER));
        }
    }

    /**
     *  captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     */
    @Override
    public void identify(int timeoutInMillis){
        Log.d(TAG, "Starting identification process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture(timeoutInMillis);

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.ONE_SECOND);
        }
        else{
            Log.e(TAG, "Identification error happened: " + captureSession.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
            return;
        }

        Pair<FingoErrorCode, String> verificationTemplate = this.fingoPayDriver.createVerificationTemplate(captureSession.second);
        if(verificationTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "VerificationTemplate generation completed:\n" + verificationTemplate.second);
            if(context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                validateVeinIdAtFingoCloud(verificationTemplate.second);
            }
            else{
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR));
            }
        }
        else {
            Log.e(TAG, "Verification Template generation failed: " + verificationTemplate.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
        }
    }

    /**
     * captures a vein biometric template by invoking 3 finger scans and then creates an enrollmentTemplate of the 3 combined scans.
     */
    @Override
    public void enroll(int timeoutInMillis){
        Log.d(TAG, "Starting enrollment process");

        Pair<FingoErrorCode, byte[]> firstCaptureSession = this.fingoPayDriver.capture(timeoutInMillis, () -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER));
        });

        if(firstCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: First capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_FIRST_SCAN_SUCCESS));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: First capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        // scan second time
        Pair<FingoErrorCode, byte[]> secondCaptureSession = this.fingoPayDriver.capture(timeoutInMillis, () -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER));
        });

        if(secondCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Second capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_SECOND_SCAN_SUCCESS));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: Second capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(secondCaptureSession.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, secondCaptureSession.first));
            return;
        }

        // scan third time
        Pair<FingoErrorCode, byte[]> thirdCaptureSession = this.fingoPayDriver.capture(timeoutInMillis, () -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER));
        });

        if(thirdCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Third capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_THIRD_SCAN_SUCCESS));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: Third capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(secondCaptureSession.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, secondCaptureSession.first));
            return;
        }

        byte[][] enrollmentSession = {firstCaptureSession.second, secondCaptureSession.second, thirdCaptureSession.second};

        Pair<FingoErrorCode, String> enrollmentTemplate = this.fingoPayDriver.createEnrolmentTemplate(enrollmentSession);

        if(enrollmentTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Enrollment Template generated OK");
            Log.d(TAG, "enroll: " + enrollmentTemplate.second);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_TEMPLATE_SUCCESS));
        }
        else{
            Log.w(TAG, "enroll: Enrollment template generation Failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(enrollmentTemplate.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, enrollmentTemplate.first));
            return;
        }

        Pair<FingoErrorCode, byte[]> verificationCaptureSession = this.fingoPayDriver.capture(timeoutInMillis, () -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER));
        });

        if(verificationCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: creating verification template:");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(verificationCaptureSession.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, verificationCaptureSession.first));
            return;
        }

        Pair<FingoErrorCode, String> verificationTemplate = this.fingoPayDriver.createVerificationTemplate(verificationCaptureSession.second);

        if(verificationTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: verification template created:\n" + verificationTemplate.second);
            if(context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                enrollAtFingoCloud(enrollmentTemplate.second, verificationTemplate.second);
            }
            else{
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
            }
        }
        else{
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(verificationTemplate.first));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, verificationTemplate.first));
        }
    }

    /**
     *  captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     *  to be used in the payment operation
     */
    @Override
    public void payment(int totalAmount, Currency currency, int totalDiscount, PosData posData, int timeoutInMillis){
        Log.d(TAG, "Starting payment process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture(timeoutInMillis);

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.ONE_SECOND);
        }
        else{
            Log.e(TAG, "Identification error happened: " + captureSession.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
            return;
        }

        Pair<FingoErrorCode, String> verificationTemplate = this.fingoPayDriver.createVerificationTemplate(captureSession.second);
        if(verificationTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "VerificationTemplate generation completed:\n" + verificationTemplate.second);
            if(context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                payWithVeinIdAtFingoCloud(totalAmount, currency, totalDiscount, posData, verificationTemplate.second);
            }
            else{
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
            }
        }
        else {
            Log.e(TAG, "Verification Template generation failed: " + verificationTemplate.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
        }
    }

    /**
     *  captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     *  to be used in the refund operation
     */
    @Override
    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, int timeoutInMillis){
        Log.d(TAG, "Starting refund process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture(timeoutInMillis);

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT));
            SystemClock.sleep(FingoConstants.ONE_SECOND);
        }
        else{
            Log.e(TAG, "Identification error happened: " + captureSession.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
            return;
        }

        Pair<FingoErrorCode, String> verificationTemplate = this.fingoPayDriver.createVerificationTemplate(captureSession.second);
        if(verificationTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "VerificationTemplate generation completed:\n" + verificationTemplate.second);
            if(context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                refundWithVeinIdAtFingoCloud(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData, verificationTemplate.second);
            }
            else{
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_REFUND_ERROR));
            }
        }
        else {
            Log.e(TAG, "Verification Template generation failed: " + verificationTemplate.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
        }
    }

    @Override
    public boolean isOperationCancelled() {
        return operationCancelled;
    }

    @Override
    public boolean isDeviceConnected() {
        return FingoPayDriver.getInstance().getFingoDevice().isDeviceOpened();
    }

    private void validateVeinIdAtFingoCloud(String verificationTemplate){
        IdentifyRequest identifyRequest = new IdentifyRequest();
        identifyRequest.setVerificationTemplate(verificationTemplate);

        Log.d(TAG, "IdentifyRequest:\n" + identifyRequest.toString());

        identifyResponseCall = identifyApi.identify(fingoRequestHelper.getHeaders(), identifyRequest);

        identifyResponseCall.enqueue(new Callback<IdentifyResponse>() {
            @Override
            public void onResponse(Call<IdentifyResponse> call, Response<IdentifyResponse> response) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful());

                IdentifyResponse identifyResponse = response.body();
                if(! response.isSuccessful() || identifyResponse == null){
                    Log.e(TAG, "onResponse: Identify Response is NULL");
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR));
                }
                else {
                    Log.i(TAG, "onResponse: " + identifyResponse.getVeinId());
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.ONLINE_VEIN_IDENTIFY_SUCCESS));
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(identifyResponse));
                    SystemClock.sleep(FingoConstants.HALF_SECOND);
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
                }
            }

            @Override
            public void onFailure(Call<IdentifyResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR));
            }
        });
    }

    private void enrollAtFingoCloud(String enrollmentTemplate, String verificationTemplate) {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setHand(Integer.parseInt(FingoKeys.HAND_VALUE.getValue()));
        enrollmentRequest.setFinger(FingoKeys.FINGER_VALUE.getValue());
        enrollmentRequest.setEnrolmentTemplate(enrollmentTemplate);
        enrollmentRequest.setVerificationTemplate(verificationTemplate);

        Log.d(TAG, "EnrollmentRequest:\n" + enrollmentRequest.toString());

        enrollmentResponseCall = enrollmentApi.enrol(fingoRequestHelper.getHeaders(), enrollmentRequest);

        enrollmentResponseCall.enqueue(new Callback<EnrollmentResponse>() {
            @Override
            public void onResponse(Call<EnrollmentResponse> call, Response<EnrollmentResponse> response) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful());

                EnrollmentResponse enrollmentResponse = response.body();
                if(! response.isSuccessful() || enrollmentResponse == null){
                    Log.e(TAG, "onResponse: Enrollment Response is NULL");
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
                }
                else{
                    Log.i(TAG, "onResponse: " + enrollmentResponse.getVeinId());
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.ONLINE_VEIN_ENROLL_SUCCESS));
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(enrollmentResponse));
                    SystemClock.sleep(FingoConstants.HALF_SECOND);
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
                }

            }

            @Override
            public void onFailure(Call<EnrollmentResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
            }
        });
    }

    private void payWithVeinIdAtFingoCloud(int totalAmount, Currency currency, int totalDiscount, PosData posData, String verificationTemplate){
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setMerchantId(fingoRequestHelper.getMerchantId());
        paymentRequest.setVerificationTemplate(verificationTemplate);
        paymentRequest.setTotalAmount(totalAmount);
        paymentRequest.setTotalDiscount(totalDiscount);
        paymentRequest.setPosData(posData);
        paymentRequest.setCurrency(currency.name());

        Log.d(TAG, "PaymentRequest:\n" + paymentRequest.toString());

        paymentResponseCall = paymentApi.pay(fingoRequestHelper.getHeaders(), paymentRequest);

        paymentResponseCall.enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful());
                Log.d(TAG, "onResponse: " + response.toString());
                handlePaymentResponse(response);
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
            }
        });
    }

    private void refundWithVeinIdAtFingoCloud(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, String verificationTemplate){
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setMerchantId(fingoRequestHelper.getMerchantId());
        refundRequest.setVerificationTemplate(verificationTemplate);
        refundRequest.setRefundAmount(refundAmount);
        refundRequest.setTransactionIdToRefund(transactionIdToRefund);
        refundRequest.setGatewayTransactionIdToRefund(gatewayTransactionIdToRefund);
        refundRequest.setTerminalData(terminalData);

        Log.d(TAG, "RefundRequest:\n" + refundRequest.toString());

        refundResponseCall = refundApi.refund(fingoRequestHelper.getHeaders(), refundRequest);

        refundResponseCall.enqueue(new Callback<RefundResponse>() {
            @Override
            public void onResponse(Call<RefundResponse> call, Response<RefundResponse> response) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful());
                Log.d(TAG, "onResponse: " + response.toString());
                handleRefundResponse(response);
            }

            @Override
            public void onFailure(Call<RefundResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
            }
        });
    }

    private void handlePaymentResponse(Response<PaymentResponse> response){
        int responseCode = response.code();

        if(responseCode == 200){
            handlePaymentResponseOK(response);
        }
        else if(responseCode == 400){
            handlePaymentResponseKO(response);
        }
        else{
            Log.d(TAG, "UNEXPECTED RESPONSE CODE: " + responseCode);
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED));
            presenter.onPaymentData(null, null);
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
        }
    }

    private void handlePaymentResponseOK(Response<PaymentResponse> response){
        PaymentResponse paymentResponse = response.body();
        if(! response.isSuccessful() || paymentResponse == null){
            Log.e(TAG, "onResponse: Payment Response is NULL");
            presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
        }
        else {
            Log.i(TAG, "onResponse: " + paymentResponse.toString());
            if(paymentResponse.getTransactionId() != null && paymentResponse.getGatewayAuthCode() != null){
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_ACCEPTED));

                // create OK object
                FingoErrorObject fingoOKObject = new FingoErrorObject(0, "Success");
                FingoErrorResponse fingoErrorResponse = new FingoErrorResponse();
                fingoErrorResponse.getFingoErrorList().add(fingoOKObject);

                presenter.onPaymentData(buildOnlinePaymentResponse(paymentResponse), fingoErrorResponse);
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED));
            }
        }
    }

    private void handlePaymentResponseKO(Response<PaymentResponse> response){
        FingoErrorResponse fingoErrorResponse;

        if(response.errorBody() == null){
            FingoErrorObject fingoErrorObject = new FingoErrorObject(-1, "UNEXPECTED ERROR REFER TO KAN");
            fingoErrorResponse = new FingoErrorResponse();
            fingoErrorResponse.getFingoErrorList().add(fingoErrorObject);
        }
        else{
            Type collectionType = new TypeToken<List<FingoErrorObject>>(){}.getType();
            Gson gson = new Gson();
            List<FingoErrorObject> fingoErrorObjectList = gson.fromJson(response.errorBody().charStream(), collectionType);
            fingoErrorResponse = new FingoErrorResponse();
            fingoErrorResponse.setFingoErrorList(fingoErrorObjectList);
        }

        presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED));
        presenter.onPaymentData(new PaymentData(), fingoErrorResponse);
        SystemClock.sleep(FingoConstants.HALF_SECOND);
        presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
    }

    private void handleRefundResponse(Response<RefundResponse> response){
        int responseCode = response.code();

        if(responseCode == 200){
            handleRefundResponseOK(response);
        }
        else if(responseCode == 400){
            handleRefundResponseKO(response);
        }
        else{
            Log.d(TAG, "UNEXPECTED RESPONSE CODE: " + responseCode);
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED));
            presenter.onPaymentData(null, null);
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
        }
    }

    private void handleRefundResponseOK(Response<RefundResponse> response){
        RefundResponse refundResponse = response.body();
        if(! response.isSuccessful() || refundResponse == null){
            Log.e(TAG, "onResponse: Refund Response is NULL");
            presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_REFUND_ERROR));
        }
        else {
            Log.i(TAG, "onResponse: " + refundResponse.toString());
            if(refundResponse.getTransactionId() != null && refundResponse.getGatewayAuthCode() != null){
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_ACCEPTED));

                // create OK object
                FingoErrorObject fingoOKObject = new FingoErrorObject(0, "Success");
                FingoErrorResponse fingoErrorResponse = new FingoErrorResponse();
                fingoErrorResponse.getFingoErrorList().add(fingoOKObject);

                presenter.onPaymentData(buildOnlineRefundResponse(refundResponse), fingoErrorResponse);
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED));
            }
        }
    }

    private void handleRefundResponseKO(Response<RefundResponse> response){
        FingoErrorResponse fingoErrorResponse;

        if(response.errorBody() == null){
            FingoErrorObject fingoErrorObject = new FingoErrorObject(-1, "UNEXPECTED ERROR REFER TO KAN");
            fingoErrorResponse = new FingoErrorResponse();
            fingoErrorResponse.getFingoErrorList().add(fingoErrorObject);
        }
        else{
            Type collectionType = new TypeToken<List<FingoErrorObject>>(){}.getType();
            Gson gson = new Gson();
            List<FingoErrorObject> fingoErrorObjectList = gson.fromJson(response.errorBody().charStream(), collectionType);
            fingoErrorResponse = new FingoErrorResponse();
            fingoErrorResponse.setFingoErrorList(fingoErrorObjectList);
        }

        presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED));
        presenter.onPaymentData(new PaymentData(), fingoErrorResponse);
        SystemClock.sleep(FingoConstants.HALF_SECOND);
        presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_REFUND_ERROR));
    }

    @Override
    public void cancel(){
        operationCancelled = true;
        cancelAllNetworkRequests();

        FingoErrorCode cancelErrorCode = FingoPayDriver.getInstance().cancelCaptureSession();
        Log.d(TAG, "Canceling Result: " + cancelErrorCode.name());
        if(cancelErrorCode.equals(FingoErrorCode.H1_CANCELLED)){
            Log.d(TAG, "Internal Cancel Function DONE");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.CANCELLED));
        }
    }

    public void cancelAllNetworkRequests(){
        if(enrollmentResponseCall != null){
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Enrollment API call");
            enrollmentResponseCall.cancel();
        }

        if(identifyResponseCall != null){
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call");
            identifyResponseCall.cancel();
        }

        if(paymentResponseCall != null){
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call");
            paymentResponseCall.cancel();
        }

        if(refundResponseCall != null){
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call");
            refundResponseCall.cancel();
        }
    }

    private DisplayTextRequested buildDisplayTextRequested(DisplayMsgCode displayMsgCode){
        DisplayTextRequested displayTextRequested = new DisplayTextRequested(context, displayMsgCode);
        return displayTextRequested;
    }

    private DisplayTextRequested buildDisplayTextRequested(FingoErrorCode fingoErrorCode){
        DisplayTextRequested displayTextRequested = new DisplayTextRequested(context, fingoErrorCode);
        return displayTextRequested;
    }

    private IdentifyData buildOnlineIdentifyResponse(IdentifyResponse identifyResponse){
        IdentifyData identifyData = new IdentifyData();
        identifyData.setMemberId(identifyResponse.getMemberId());
        identifyData.setVeinId(identifyResponse.getVeinId());
        identifyData.setOnlineData(true);
        return identifyData;
    }

    private IdentifyData buildOnlineIdentifyResponse(EnrollmentResponse enrollmentResponse){
        IdentifyData identifyData = new IdentifyData();
        identifyData.setMemberId(enrollmentResponse.getMemberId());
        identifyData.setVeinId(enrollmentResponse.getVeinId());
        identifyData.setOnlineData(true);
        return identifyData;
    }

    private IdentifyData buildOfflineResponse(String verificationTemplate, String enrolmentTemplate){
        IdentifyData identifyData = new IdentifyData();
        identifyData.setVerificationTemplate(verificationTemplate);
        identifyData.setEnrolmentTemplate(enrolmentTemplate);
        identifyData.setOnlineData(false);
        return identifyData;
    }

    private PaymentData buildOnlinePaymentResponse(PaymentResponse paymentResponse){
        PaymentData paymentData = new PaymentData();
        paymentData.setTransactionId(paymentResponse.getTransactionId());
        paymentData.setGatewayAuthCode(paymentResponse.getGatewayAuthCode());
        paymentData.setGatewayTransactionId(paymentResponse.getGatewayTransactionId());
        paymentData.setMaskedCardNumber(paymentResponse.getMaskedCardNumber());
        paymentData.setTimestamp(paymentResponse.getTimestamp());
        return paymentData;
    }

    private PaymentData buildOnlineRefundResponse(RefundResponse refundResponse){
        PaymentData paymentData = new PaymentData();
        paymentData.setTransactionId(refundResponse.getTransactionId());
        paymentData.setGatewayAuthCode(refundResponse.getGatewayAuthCode());
        paymentData.setGatewayTransactionId(refundResponse.getGatewayTransactionId());
        paymentData.setMaskedCardNumber(refundResponse.getMaskedCardNumber());
        paymentData.setTimestamp(refundResponse.getTimestamp());
        return paymentData;
    }

    private ProcessingFinished buildProcessingFinishedEvent(boolean status, FingoErrorCode fingoErrorCode){
        ProcessingFinished processingFinished = buildProcessingFinishedEvent(status, fingoErrorCode.getDescriptionResId());
        processingFinished.setErrorCode(fingoErrorCode.getErrorCode());
        processingFinished.setErrorName(fingoErrorCode.name());
        return processingFinished;
    }

    private ProcessingFinished buildProcessingFinishedEvent(boolean status, int msgId){
        ProcessingFinished processingFinished = new ProcessingFinished();
        processingFinished.setErrorCode(FingoErrorCode.H1_UNKNOWN_ERROR.getErrorCode());
        processingFinished.setErrorName(FingoErrorCode.H1_UNKNOWN_ERROR.name());
        processingFinished.setStatus(status);
        processingFinished.setText(context.getString(msgId));
        return processingFinished;
    }
}
