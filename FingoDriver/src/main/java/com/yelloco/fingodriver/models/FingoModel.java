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
import com.yelloco.fingodriver.R;
import com.yelloco.fingodriver.callbacks.FingoContract;
import com.yelloco.fingodriver.enums.FingoCurrency;
import com.yelloco.fingodriver.enums.FingoKeys;
import com.yelloco.fingodriver.enums.FingoOperation;
import com.yelloco.fingodriver.models.fingo_operation.DisplayTextRequested;
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData;
import com.yelloco.fingodriver.models.fingo_operation.PaymentData;
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentApi;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentRequest;
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentResponse;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
    private boolean operationCancelled;
    private FingoPayDriver fingoPayDriver;
    private Retrofit retrofit;
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
        this.fingoPayDriver = FingoPayDriver.getInstance();
        this.retrofit = new Retrofit.Builder()
                .baseUrl(FingoKeys.FINGO_CLOUD_BASE_URL.getValue())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        enrollmentApi = retrofit.create(EnrollmentApi.class);
        identifyApi = retrofit.create(IdentifyApi.class);
        paymentApi = retrofit.create(PaymentApi.class);
        refundApi = retrofit.create(RefundApi.class);
    }

    public void invoke(FingoOperation fingoOperation, boolean forceOnline){
        operationCancelled = false;
        this.presenter.onProcessingStarted();
        this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.please_insert_finger));
    }

    /**
     *  captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     * @param forceOnline
     */
    @Override
    public void identify(boolean forceOnline){
        Log.d(TAG, "Starting identification process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture();

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
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
            if(forceOnline && context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                validateVeinIdAtFingoCloud(verificationTemplate.second);
            }
            else if(forceOnline){
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED.getDescriptionResId()));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
        }
        else {
            Log.e(TAG, "Verification Template generation failed: " + verificationTemplate.first.name());
            this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, captureSession.first));
        }
    }

    /**
     * captures a vein biometric template by invoking 3 finger scans and then creates an enrollmentTemplate of the 3 combined scans.
     * @param forceOnline
     */
    @Override
    public void enroll(boolean forceOnline){
        Log.d(TAG, "Starting enrollment process");

        Pair<FingoErrorCode, byte[]> firstCaptureSession = FingoPayDriver.getInstance().capture(() -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.please_insert_finger));
        });

        if(firstCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: First capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.enrol_first_scan_success));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: First capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        // scan second time
        Pair<FingoErrorCode, byte[]> secondCaptureSession = FingoPayDriver.getInstance().capture(() -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.please_insert_finger));
        });

        if(secondCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Second capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.enrol_second_scan_success));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: Second capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        // scan third time
        Pair<FingoErrorCode, byte[]> thirdCaptureSession = FingoPayDriver.getInstance().capture(() -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.please_insert_finger));
        });

        if(thirdCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Third capture is successful");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.enrol_third_scan_success));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            Log.e(TAG, "enroll: Third capture failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        byte[][] enrollmentSession = {firstCaptureSession.second, secondCaptureSession.second, thirdCaptureSession.second};

        Pair<FingoErrorCode, String> enrollmentTemplate = FingoPayDriver.getInstance().createEnrolmentTemplate(enrollmentSession);

        if(enrollmentTemplate.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: Enrollment Template generated OK");
            Log.d(TAG, "enroll: " + enrollmentTemplate.second);
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.enrollment_template_generated_successfully));
        }
        else{
            Log.w(TAG, "enroll: Enrollment template generation Failed");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        Pair<FingoErrorCode, byte[]> verificationCaptureSession = FingoPayDriver.getInstance().capture(() -> {
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.please_insert_finger));
        });

        if(verificationCaptureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.d(TAG, "enroll: creating verification template:");
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
            SystemClock.sleep(FingoConstants.HALF_SECOND);
        }
        else{
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }

        Pair<FingoErrorCode, String> verificationTemplate = FingoPayDriver.getInstance().createVerificationTemplate(verificationCaptureSession.second);

        if(verificationTemplate.first.equals(FingoErrorCode.H1_OK)){
            if(forceOnline && context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                enrollAtFingoCloud(enrollmentTemplate.second, verificationTemplate.second);
            }
            else if(forceOnline){
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED.getDescriptionResId()));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, enrollmentTemplate.second));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, enrollmentTemplate.second));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
        }
        else{
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first.getDescriptionResId()));
            this.presenter.onProcessingFinished(this.buildProcessingFinishedEvent(false, firstCaptureSession.first));
            return;
        }
    }

    /**
     *  captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     *  to be used in the payment operation
     * @param forceOnline
     */
    @Override
    public void payment(int totalAmount, FingoCurrency fingoCurrency, int totalDiscount, PosData posData, boolean forceOnline){
        Log.d(TAG, "Starting payment process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture();

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
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
            if(forceOnline && context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                payWithVeinIdAtFingoCloud(totalAmount, fingoCurrency, totalDiscount, posData, verificationTemplate.second);
            }
            else if(forceOnline){
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED.getDescriptionResId()));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
            }
            else {
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
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
     * @param forceOnline
     */
    @Override
    public void refund(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, boolean forceOnline){
        Log.d(TAG, "Starting refund process");

        Pair<FingoErrorCode, byte[]> captureSession = this.fingoPayDriver.capture();

        if(captureSession.first.equals(FingoErrorCode.H1_OK)){
            Log.i(TAG, "identify: Identification OK");
            this.presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.indentifying_vein_please_wait));
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
            if(forceOnline && context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){
                refundWithVeinIdAtFingoCloud(refundAmount, transactionIdToRefund, gatewayTransactionIdToRefund, terminalData, verificationTemplate.second);
            }
            else if(forceOnline){
                this.presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED.getDescriptionResId()));
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
                this.presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_REFUND_ERROR));
            }
            else {
                this.presenter.onIdentifyData(buildOfflineResponse(verificationTemplate.second, null));
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

        identifyResponseCall = identifyApi.identify(identifyRequest);

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
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.finger_vein_validation_success));
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(identifyResponse));
                    SystemClock.sleep(FingoConstants.HALF_SECOND);
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
                }
            }

            @Override
            public void onFailure(Call<IdentifyResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR.getDescriptionResId()));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR));
            }
        });
    }

    private void enrollAtFingoCloud(String enrollmentTemplate, String verificationTemplate) {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setHand(0);
        enrollmentRequest.setFinger("2");
        enrollmentRequest.setEnrolmentTemplate(enrollmentTemplate);
        enrollmentRequest.setVerificationTemplate(verificationTemplate);

        enrollmentResponseCall = enrollmentApi.enrol(enrollmentRequest);

        enrollmentResponseCall.enqueue(new Callback<EnrollmentResponse>() {
            @Override
            public void onResponse(Call<EnrollmentResponse> call, Response<EnrollmentResponse> response) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful());

                EnrollmentResponse enrollmentResponse = response.body();
                if(! response.isSuccessful() || enrollmentResponse == null){
                    Log.e(TAG, "onResponse: Enrollment Response is NULL");
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR.getDescriptionResId()));
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
                }
                else{
                    Log.i(TAG, "onResponse: " + enrollmentResponse.getVeinId());
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.finger_vein_enrollment_success));
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(enrollmentResponse));
                    SystemClock.sleep(FingoConstants.HALF_SECOND);
                    presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
                }

            }

            @Override
            public void onFailure(Call<EnrollmentResponse> call, Throwable t) {
                Log.i(TAG, "onResponse error: " + t.getMessage());
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR.getDescriptionResId()));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR));
            }
        });
    }

    private void payWithVeinIdAtFingoCloud(int totalAmount, FingoCurrency fingoCurrency, int totalDiscount, PosData posData, String verificationTemplate){
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setMerchantId(FingoKeys.FINGO_MERCHANT_ID.getValue());
        paymentRequest.setVerificationTemplate(verificationTemplate);
        paymentRequest.setTotalAmount(totalAmount);
        paymentRequest.setTotalDiscount(totalDiscount);
        paymentRequest.setPosData(posData);
        paymentRequest.setCurrency(fingoCurrency.name());

//        Log.d(TAG, "PaymentRequest:\n" + paymentRequest.toString());

        paymentResponseCall = paymentApi.pay(paymentRequest);

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
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR.getDescriptionResId()));
                presenter.onProcessingFinished(buildProcessingFinishedEvent(false, FingoErrorCode.H1_ONLINE_PAYMENT_ERROR));
            }
        });
    }

    private void refundWithVeinIdAtFingoCloud(int refundAmount, String transactionIdToRefund, String gatewayTransactionIdToRefund, TerminalData terminalData, String verificationTemplate){
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setMerchantId(FingoKeys.FINGO_MERCHANT_ID.getValue());
        refundRequest.setVerificationTemplate(verificationTemplate);
        refundRequest.setRefundAmount(refundAmount);
        refundRequest.setTransactionIdToRefund(transactionIdToRefund);
        refundRequest.setGatewayTransactionIdToRefund(gatewayTransactionIdToRefund);
        refundRequest.setTerminalData(terminalData);

//        Log.d(TAG, "PaymentRequest:\n" + paymentRequest.toString());

        refundResponseCall = refundApi.refund(refundRequest);

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
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR.getDescriptionResId()));
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
            presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.payment_declined));
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
                presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.payment_accepted));

                // create OK object
                FingoErrorObject fingoOKObject = new FingoErrorObject(0, "Success");
                FingoErrorResponse fingoErrorResponse = new FingoErrorResponse();
                fingoErrorResponse.getFingoErrorList().add(fingoOKObject);

                presenter.onPaymentData(buildOnlinePaymentResponse(paymentResponse), fingoErrorResponse);
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.payment_declined));
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

        presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.payment_declined));
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
            presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.payment_declined));
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
                presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.refund_accepted));

                // create OK object
                FingoErrorObject fingoOKObject = new FingoErrorObject(0, "Success");
                FingoErrorResponse fingoErrorResponse = new FingoErrorResponse();
                fingoErrorResponse.getFingoErrorList().add(fingoOKObject);

                presenter.onPaymentData(buildOnlineRefundResponse(refundResponse), fingoErrorResponse);
                SystemClock.sleep(FingoConstants.HALF_SECOND);
                presenter.onProcessingFinished(buildProcessingFinishedEvent(true, FingoErrorCode.H1_OK));
            }
            else{
                presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.refund_declined));
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

        presenter.onDisplayTextRequested(buildDisplayTextRequested(R.string.refund_declined));
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
            this.presenter.onDisplayTextRequested(this.buildDisplayTextRequested(R.string.operation_cancelled));
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

    private DisplayTextRequested buildDisplayTextRequested(int msgId){
        DisplayTextRequested displayTextRequested = new DisplayTextRequested(context.getString(msgId));
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
