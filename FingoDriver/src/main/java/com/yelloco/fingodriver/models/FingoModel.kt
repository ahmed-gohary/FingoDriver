package com.yelloco.fingodriver.models

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yelloco.fingodriver.FingoFactory
import com.yelloco.fingodriver.FingoPayDriver
import com.yelloco.fingodriver.FingoSDK
import com.yelloco.fingodriver.callbacks.FingoCaptureCallback
import com.yelloco.fingodriver.callbacks.FingoContract
import com.yelloco.fingodriver.enums.*
import com.yelloco.fingodriver.models.fingo_operation.IdentifyData
import com.yelloco.fingodriver.models.fingo_operation.PaymentData
import com.yelloco.fingodriver.models.fingo_operation.ProcessingFinished
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayMsgCode
import com.yelloco.fingodriver.models.fingo_operation.display_text_requested.DisplayTextRequested
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentApi
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentRequest
import com.yelloco.fingodriver.models.networking.Enrollment.EnrollmentResponse
import com.yelloco.fingodriver.models.networking.FingoRequestHelper
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorObject
import com.yelloco.fingodriver.models.networking.fingo_error.FingoErrorResponse
import com.yelloco.fingodriver.models.networking.identify.IdentifyApi
import com.yelloco.fingodriver.models.networking.identify.IdentifyRequest
import com.yelloco.fingodriver.models.networking.identify.IdentifyResponse
import com.yelloco.fingodriver.models.networking.payment.PaymentApi
import com.yelloco.fingodriver.models.networking.payment.PaymentRequest
import com.yelloco.fingodriver.models.networking.payment.PaymentResponse
import com.yelloco.fingodriver.models.networking.payment.PosData
import com.yelloco.fingodriver.models.networking.refund.RefundApi
import com.yelloco.fingodriver.models.networking.refund.RefundRequest
import com.yelloco.fingodriver.models.networking.refund.RefundResponse
import com.yelloco.fingodriver.models.networking.refund.TerminalData
import com.yelloco.fingodriver.utils.Storage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class FingoModel(
    private val presenter: FingoContract.Presenter, // Members
    private val context: Context
) : FingoContract.Model 
{
    private var canProceed: Boolean
    override var isOperationCancelled = false
        private set
    private val fingoPayDriver: FingoPayDriver
    private val fingoRequestHelper: FingoRequestHelper
    private var retrofit: Retrofit? = null
    private val okHttpClient: OkHttpClient
    private var retrofitInitialized = false
    private var enrollmentApi: EnrollmentApi? = null
    private var enrollmentResponseCall: Call<EnrollmentResponse?>? = null
    private var identifyApi: IdentifyApi? = null
    private var identifyResponseCall: Call<IdentifyResponse?>? = null
    private var paymentApi: PaymentApi? = null
    private var paymentResponseCall: Call<PaymentResponse?>? = null
    private var refundApi: RefundApi? = null
    private var refundResponseCall: Call<RefundResponse?>? = null
    
    override fun invoke(fingoOperation: FingoOperation?) {
        Log.i(TAG, "Starting FingoOperation: " + fingoOperation!!.name)
        isOperationCancelled = false
        presenter.onProcessingStarted()
        canProceed =
            FingoSDK.isSdkInitialized && Storage.getBoolean(StorageKey.PARAMS_STATUS.name, false)
        if (canProceed && !retrofitInitialized) {
            retrofitInitialized = true
            retrofit = Retrofit.Builder()
                .baseUrl(fingoRequestHelper.fingoCloudBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            retrofit?.let {
                enrollmentApi = it.create<EnrollmentApi>(
                    EnrollmentApi::class.java
                )
                identifyApi = it.create<IdentifyApi>(
                    IdentifyApi::class.java
                )
                paymentApi = it.create<PaymentApi>(
                    PaymentApi::class.java
                )
                refundApi = it.create<RefundApi>(
                    RefundApi::class.java
                )
            } ?: kotlin.run {
                Log.e(TAG, "invoke: retrofit is NULL")
            }
        } else {
            Log.e(
                TAG, "Can't Proceed, Not Initializing Networking: " + canProceed
                        + " " + retrofitInitialized
            )
        }
        if (!canProceed) {
            if (!Storage.getBoolean(StorageKey.PARAMS_STATUS.name, false)) {
                presenter.onProcessingFinished(
                    this.buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_SDK_PARAMS_NOT_SET
                    )
                )
            } else {
                presenter.onProcessingFinished(
                    this.buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_SDK_INIT_FAILED_BLOCKED
                    )
                )
            }
            isOperationCancelled = true
        } else {
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.PLEASE_INSERT_FINGER))
        }
    }

    /**
     * captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     */
    override fun identify(timeoutInMillis: Long) {
        Log.d(TAG, "Starting identification process")
        val captureSession = fingoPayDriver.capture(timeoutInMillis)
        if (captureSession.first == FingoErrorCode.H1_OK) {
            Log.i(TAG, "identify: Identification OK")
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.ONE_SECOND)
        } else {
            Log.e(TAG, "Identification error happened: " + captureSession.first.name)
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
            return
        }
        val verificationTemplate = fingoPayDriver.createVerificationTemplate(
            captureSession.second!!
        )
        if (verificationTemplate.first == FingoErrorCode.H1_OK) {
            Log.i(
                TAG, """
     VerificationTemplate generation completed:
     ${verificationTemplate.second}
     """.trimIndent()
            )
            if (context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                validateVeinIdAtFingoCloud(verificationTemplate.second)
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED))
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR
                    )
                )
            }
        } else {
            Log.e(
                TAG,
                "Verification Template generation failed: " + verificationTemplate.first.name
            )
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
        }
    }

    /**
     * captures a vein biometric template by invoking 3 finger scans and then creates an enrollmentTemplate of the 3 combined scans.
     */
    override fun enroll(timeoutInMillis: Long) {
        Log.d(TAG, "Starting enrollment process")
        val firstCaptureSession = fingoPayDriver.capture(timeoutInMillis, object: FingoCaptureCallback {
            override fun onCaptureStarted() {
                presenter.onDisplayTextRequested(
                    buildDisplayTextRequested(
                        DisplayMsgCode.PLEASE_INSERT_FINGER
                    )
                )
            }
        })
        if (firstCaptureSession.first == FingoErrorCode.H1_OK) {
            Log.d(TAG, "enroll: First capture is successful")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_FIRST_SCAN_SUCCESS))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        } else {
            Log.e(TAG, "enroll: First capture failed")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(firstCaptureSession.first))
            presenter.onProcessingFinished(
                this.buildProcessingFinishedEvent(
                    false,
                    firstCaptureSession.first
                )
            )
            return
        }

        // scan second time
        val secondCaptureSession = fingoPayDriver.capture(timeoutInMillis, object: FingoCaptureCallback {
            override fun onCaptureStarted() {
                presenter.onDisplayTextRequested(
                    buildDisplayTextRequested(
                        DisplayMsgCode.PLEASE_INSERT_FINGER
                    )
                )
            }
        })
        if (secondCaptureSession.first == FingoErrorCode.H1_OK) {
            Log.d(TAG, "enroll: Second capture is successful")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_SECOND_SCAN_SUCCESS))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        } else {
            Log.e(TAG, "enroll: Second capture failed")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(secondCaptureSession.first))
            presenter.onProcessingFinished(
                this.buildProcessingFinishedEvent(
                    false,
                    secondCaptureSession.first
                )
            )
            return
        }

        // scan third time
        val thirdCaptureSession = fingoPayDriver.capture(timeoutInMillis, object: FingoCaptureCallback {
            override fun onCaptureStarted() {
                presenter.onDisplayTextRequested(
                    buildDisplayTextRequested(
                        DisplayMsgCode.PLEASE_INSERT_FINGER
                    )
                )
            }
        })
        if (thirdCaptureSession.first == FingoErrorCode.H1_OK) {
            Log.d(TAG, "enroll: Third capture is successful")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_THIRD_SCAN_SUCCESS))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        } else {
            Log.e(TAG, "enroll: Third capture failed")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(secondCaptureSession.first))
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    secondCaptureSession.first
                )
            )
            return
        }
        val enrollmentSession = arrayOf(
            firstCaptureSession.second,
            secondCaptureSession.second,
            thirdCaptureSession.second
        )
        val enrollmentTemplate = fingoPayDriver.createEnrolmentTemplate(enrollmentSession)
        if (enrollmentTemplate.first == FingoErrorCode.H1_OK) {
            Log.d(TAG, "enroll: Enrollment Template generated OK")
            Log.d(TAG, "enroll: " + enrollmentTemplate.second)
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.ENROLL_TEMPLATE_SUCCESS))
        } else {
            Log.w(TAG, "enroll: Enrollment template generation Failed")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(enrollmentTemplate.first))
            presenter.onProcessingFinished(
                this.buildProcessingFinishedEvent(
                    false,
                    enrollmentTemplate.first
                )
            )
            return
        }
        val verificationCaptureSession =
            fingoPayDriver.capture(timeoutInMillis, object: FingoCaptureCallback {
                override fun onCaptureStarted() {
                    presenter.onDisplayTextRequested(
                        buildDisplayTextRequested(
                            DisplayMsgCode.PLEASE_INSERT_FINGER
                        )
                    )
                }
            })
        if (verificationCaptureSession.first == FingoErrorCode.H1_OK) {
            Log.d(TAG, "enroll: creating verification template:")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        } else {
            presenter.onDisplayTextRequested(
                this.buildDisplayTextRequested(
                    verificationCaptureSession.first
                )
            )
            presenter.onProcessingFinished(
                this.buildProcessingFinishedEvent(
                    false,
                    verificationCaptureSession.first
                )
            )
            return
        }
        val verificationTemplate = fingoPayDriver.createVerificationTemplate(
            verificationCaptureSession.second!!
        )
        if (verificationTemplate.first == FingoErrorCode.H1_OK) {
            Log.d(
                TAG, """
     enroll: verification template created:
     ${verificationTemplate.second}
     """.trimIndent()
            )
            if (context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                enrollAtFingoCloud(enrollmentTemplate.second, verificationTemplate.second)
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED))
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR
                    )
                )
            }
        } else {
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(verificationTemplate.first))
            presenter.onProcessingFinished(
                this.buildProcessingFinishedEvent(
                    false,
                    verificationTemplate.first
                )
            )
        }
    }

    /**
     * captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     * to be used in the payment operation
     */
    override fun payment(
        totalAmount: Int,
        currency: Currency?,
        totalDiscount: Int,
        posData: PosData?,
        timeoutInMillis: Long
    ) {
        Log.d(TAG, "Starting payment process")
        val captureSession = fingoPayDriver.capture(timeoutInMillis)
        if (captureSession.first == FingoErrorCode.H1_OK) {
            Log.i(TAG, "identify: Identification OK")
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.ONE_SECOND)
        } else {
            Log.e(TAG, "Identification error happened: " + captureSession.first.name)
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
            return
        }
        val verificationTemplate = fingoPayDriver.createVerificationTemplate(
            captureSession.second!!
        )
        if (verificationTemplate.first == FingoErrorCode.H1_OK) {
            Log.i(
                TAG, """
     VerificationTemplate generation completed:
     ${verificationTemplate.second}
     """.trimIndent()
            )
            if (context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                payWithVeinIdAtFingoCloud(
                    totalAmount,
                    currency,
                    totalDiscount,
                    posData,
                    verificationTemplate.second
                )
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED))
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                    )
                )
            }
        } else {
            Log.e(
                TAG,
                "Verification Template generation failed: " + verificationTemplate.first.name
            )
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
        }
    }

    /**
     * captures a vein biometric template by invoking 1 finger scans and then creates a verification template
     * to be used in the refund operation
     */
    override fun refund(
        refundAmount: Int,
        transactionIdToRefund: String?,
        gatewayTransactionIdToRefund: String?,
        terminalData: TerminalData?,
        timeoutInMillis: Long
    ) {
        Log.d(TAG, "Starting refund process")
        val captureSession = fingoPayDriver.capture(timeoutInMillis)
        if (captureSession.first == FingoErrorCode.H1_OK) {
            Log.i(TAG, "identify: Identification OK")
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.IDENTIFYING_PLEASE_WAIT))
            SystemClock.sleep(FingoFactory.Constants.ONE_SECOND)
        } else {
            Log.e(TAG, "Identification error happened: " + captureSession.first.name)
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
            return
        }
        val verificationTemplate = fingoPayDriver.createVerificationTemplate(
            captureSession.second!!
        )
        if (verificationTemplate.first == FingoErrorCode.H1_OK) {
            Log.i(
                TAG, """
     VerificationTemplate generation completed:
     ${verificationTemplate.second}
     """.trimIndent()
            )
            if (context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                refundWithVeinIdAtFingoCloud(
                    refundAmount,
                    transactionIdToRefund,
                    gatewayTransactionIdToRefund,
                    terminalData,
                    verificationTemplate.second
                )
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_INTERNET_PERMISSION_NOT_GRANTED))
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_REFUND_ERROR
                    )
                )
            }
        } else {
            Log.e(
                TAG,
                "Verification Template generation failed: " + verificationTemplate.first.name
            )
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    captureSession.first
                )
            )
        }
    }

    override val isDeviceConnected: Boolean
        get() = FingoPayDriver.activeDevice.isDeviceOpened

    private fun validateVeinIdAtFingoCloud(verificationTemplate: String?) {
        val identifyRequest = IdentifyRequest()
        identifyRequest.verificationTemplate = verificationTemplate
        Log.d(TAG, "IdentifyRequest:\n$identifyRequest")
        identifyResponseCall =
            identifyApi!!.identify(fingoRequestHelper.getHeaders(), identifyRequest)
        identifyResponseCall!!.enqueue(object : Callback<IdentifyResponse?> {
            override fun onResponse(
                call: Call<IdentifyResponse?>,
                response: Response<IdentifyResponse?>
            ) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful)
                val identifyResponse = response.body()
                if (!response.isSuccessful || identifyResponse == null) {
                    Log.e(TAG, "onResponse: Identify Response is NULL")
                    presenter.onProcessingFinished(
                        buildProcessingFinishedEvent(
                            false,
                            FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR
                        )
                    )
                } else {
                    Log.i(TAG, "onResponse: " + identifyResponse.veinId)
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.ONLINE_VEIN_IDENTIFY_SUCCESS))
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(identifyResponse))
                    SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                    presenter.onProcessingFinished(
                        buildProcessingFinishedEvent(
                            true,
                            FingoErrorCode.H1_OK
                        )
                    )
                }
            }

            override fun onFailure(call: Call<IdentifyResponse?>, t: Throwable) {
                Log.i(TAG, "onResponse error: " + t.message)
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled)
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR))
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_IDENTIFICATION_ERROR
                    )
                )
            }
        })
    }

    private fun enrollAtFingoCloud(enrollmentTemplate: String?, verificationTemplate: String?) {
        val enrollmentRequest = EnrollmentRequest()
        enrollmentRequest.hand = FingoKeys.HAND_VALUE.value.toInt()
        enrollmentRequest.finger = FingoKeys.FINGER_VALUE.value
        enrollmentRequest.enrolmentTemplate = enrollmentTemplate
        enrollmentRequest.verificationTemplate = verificationTemplate
        Log.d(TAG, "EnrollmentRequest:\n$enrollmentRequest")
        enrollmentResponseCall =
            enrollmentApi!!.enrol(fingoRequestHelper.getHeaders(), enrollmentRequest)
        enrollmentResponseCall!!.enqueue(object : Callback<EnrollmentResponse?> {
            override fun onResponse(
                call: Call<EnrollmentResponse?>,
                response: Response<EnrollmentResponse?>
            ) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful)
                val enrollmentResponse = response.body()
                if (!response.isSuccessful || enrollmentResponse == null) {
                    Log.e(TAG, "onResponse: Enrollment Response is NULL")
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR))
                    presenter.onProcessingFinished(
                        buildProcessingFinishedEvent(
                            false,
                            FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR
                        )
                    )
                } else {
                    Log.i(TAG, "onResponse: " + enrollmentResponse.veinId)
                    presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.ONLINE_VEIN_ENROLL_SUCCESS))
                    presenter.onIdentifyData(buildOnlineIdentifyResponse(enrollmentResponse))
                    SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                    presenter.onProcessingFinished(
                        buildProcessingFinishedEvent(
                            true,
                            FingoErrorCode.H1_OK
                        )
                    )
                }
            }

            override fun onFailure(call: Call<EnrollmentResponse?>, t: Throwable) {
                Log.i(TAG, "onResponse error: " + t.message)
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR))
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_ENROLLMENT_ERROR
                    )
                )
            }
        })
    }

    private fun payWithVeinIdAtFingoCloud(
        totalAmount: Int,
        currency: Currency?,
        totalDiscount: Int,
        posData: PosData?,
        verificationTemplate: String?
    ) {
        val paymentRequest = PaymentRequest()
        paymentRequest.merchantId = fingoRequestHelper.merchantId
        paymentRequest.verificationTemplate = verificationTemplate
        paymentRequest.setTotalAmount(totalAmount)
        paymentRequest.totalDiscount = totalDiscount
        paymentRequest.posData = posData
        paymentRequest.currency = currency!!.name
        Log.d(TAG, "PaymentRequest:\n$paymentRequest")
        paymentResponseCall = paymentApi!!.pay(fingoRequestHelper.getHeaders(), paymentRequest)
        paymentResponseCall!!.enqueue(object : Callback<PaymentResponse?> {
            override fun onResponse(
                call: Call<PaymentResponse?>,
                response: Response<PaymentResponse?>
            ) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful)
                Log.d(TAG, "onResponse: $response")
                handlePaymentResponse(response)
            }

            override fun onFailure(call: Call<PaymentResponse?>, t: Throwable) {
                Log.i(TAG, "onResponse error: " + t.message)
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled)
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR))
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                    )
                )
            }
        })
    }

    private fun refundWithVeinIdAtFingoCloud(
        refundAmount: Int,
        transactionIdToRefund: String?,
        gatewayTransactionIdToRefund: String?,
        terminalData: TerminalData?,
        verificationTemplate: String?
    ) {
        val refundRequest = RefundRequest()
        refundRequest.merchantId = fingoRequestHelper.merchantId
        refundRequest.verificationTemplate = verificationTemplate
        refundRequest.refundAmount = refundAmount
        refundRequest.transactionIdToRefund = transactionIdToRefund
        refundRequest.gatewayTransactionIdToRefund = gatewayTransactionIdToRefund
        refundRequest.terminalData = terminalData
        Log.d(TAG, "RefundRequest:\n$refundRequest")
        refundResponseCall = refundApi!!.refund(fingoRequestHelper.getHeaders(), refundRequest)
        refundResponseCall!!.enqueue(object : Callback<RefundResponse?> {
            override fun onResponse(
                call: Call<RefundResponse?>,
                response: Response<RefundResponse?>
            ) {
                Log.d(TAG, "onResponse: " + response.code() + " --> " + response.isSuccessful)
                Log.d(TAG, "onResponse: $response")
                handleRefundResponse(response)
            }

            override fun onFailure(call: Call<RefundResponse?>, t: Throwable) {
                Log.i(TAG, "onResponse error: " + t.message)
                Log.i(TAG, "onFailure: cancelled: " + call.isCanceled)
                presenter.onDisplayTextRequested(buildDisplayTextRequested(FingoErrorCode.H1_ONLINE_PAYMENT_ERROR))
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        false,
                        FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                    )
                )
            }
        })
    }

    private fun handlePaymentResponse(response: Response<PaymentResponse?>) {
        val responseCode = response.code()
        if (responseCode == 200) {
            handlePaymentResponseOK(response)
        } else if (responseCode == 400) {
            handlePaymentResponseKO(response)
        } else {
            Log.d(TAG, "UNEXPECTED RESPONSE CODE: $responseCode")
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED))
            presenter.onPaymentData(null, null)
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                )
            )
        }
    }

    private fun handlePaymentResponseOK(response: Response<PaymentResponse?>) {
        val paymentResponse = response.body()
        if (!response.isSuccessful || paymentResponse == null) {
            Log.e(TAG, "onResponse: Payment Response is NULL")
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                )
            )
        } else {
            Log.i(TAG, "onResponse: $paymentResponse")
            if (paymentResponse.transactionId != null && paymentResponse.gatewayAuthCode != null) {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_ACCEPTED))

                // create OK object
                val fingoOKObject = FingoErrorObject(0, "Success")
                val fingoErrorResponse = FingoErrorResponse(listOf(
                    fingoOKObject
                ))
                presenter.onPaymentData(
                    buildOnlinePaymentResponse(paymentResponse),
                    fingoErrorResponse
                )
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        true,
                        FingoErrorCode.H1_OK
                    )
                )
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED))
            }
        }
    }

    private fun handlePaymentResponseKO(response: Response<PaymentResponse?>) {
        val fingoErrorResponse: FingoErrorResponse
        if (response.errorBody() == null) {
            val fingoErrorObject = FingoErrorObject(-1, "UNEXPECTED ERROR REFER TO KAN")
            fingoErrorResponse = FingoErrorResponse(listOf(
                fingoErrorObject
            ))
        } else {
            val collectionType: Type = object : TypeToken<List<FingoErrorObject?>?>() {}.getType()
            val gson = Gson()
            val fingoErrorObjectList: List<FingoErrorObject> =
                gson.fromJson<List<FingoErrorObject>>(
                    response.errorBody()!!.charStream(), collectionType
                )
            fingoErrorResponse = FingoErrorResponse()
            fingoErrorResponse.fingoErrorList = fingoErrorObjectList
        }
        presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.PAYMENT_DECLINED))
        presenter.onPaymentData(PaymentData(), fingoErrorResponse)
        SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        presenter.onProcessingFinished(
            buildProcessingFinishedEvent(
                false,
                FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
            )
        )
    }

    private fun handleRefundResponse(response: Response<RefundResponse?>) {
        val responseCode = response.code()
        if (responseCode == 200) {
            handleRefundResponseOK(response)
        } else if (responseCode == 400) {
            handleRefundResponseKO(response)
        } else {
            Log.d(TAG, "UNEXPECTED RESPONSE CODE: $responseCode")
            presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED))
            presenter.onPaymentData(null, null)
            SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    FingoErrorCode.H1_ONLINE_PAYMENT_ERROR
                )
            )
        }
    }

    private fun handleRefundResponseOK(response: Response<RefundResponse?>) {
        val refundResponse = response.body()
        if (!response.isSuccessful || refundResponse == null) {
            Log.e(TAG, "onResponse: Refund Response is NULL")
            presenter.onProcessingFinished(
                buildProcessingFinishedEvent(
                    false,
                    FingoErrorCode.H1_ONLINE_REFUND_ERROR
                )
            )
        } else {
            Log.i(TAG, "onResponse: $refundResponse")
            if (refundResponse.transactionId != null && refundResponse.gatewayAuthCode != null) {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_ACCEPTED))

                // create OK object
                val fingoOKObject = FingoErrorObject(0, "Success")
                val fingoErrorResponse = FingoErrorResponse(listOf(
                    fingoOKObject
                ))
                presenter.onPaymentData(
                    buildOnlineRefundResponse(refundResponse),
                    fingoErrorResponse
                )
                SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
                presenter.onProcessingFinished(
                    buildProcessingFinishedEvent(
                        true,
                        FingoErrorCode.H1_OK
                    )
                )
            } else {
                presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED))
            }
        }
    }

    private fun handleRefundResponseKO(response: Response<RefundResponse?>) {
        val fingoErrorResponse: FingoErrorResponse
        if (response.errorBody() == null) {
            val fingoErrorObject = FingoErrorObject(-1, "UNEXPECTED ERROR REFER TO KAN")
            fingoErrorResponse = FingoErrorResponse(listOf(
                fingoErrorObject
            ))
        } else {
            val collectionType: Type = object : TypeToken<List<FingoErrorObject?>?>() {}.getType()
            val gson = Gson()
            val fingoErrorObjectList: List<FingoErrorObject> =
                gson.fromJson<List<FingoErrorObject>>(
                    response.errorBody()!!.charStream(), collectionType
                )
            fingoErrorResponse = FingoErrorResponse()
            fingoErrorResponse.fingoErrorList = fingoErrorObjectList
        }
        presenter.onDisplayTextRequested(buildDisplayTextRequested(DisplayMsgCode.REFUND_DECLINED))
        presenter.onPaymentData(PaymentData(), fingoErrorResponse)
        SystemClock.sleep(FingoFactory.Constants.HALF_SECOND)
        presenter.onProcessingFinished(
            buildProcessingFinishedEvent(
                false,
                FingoErrorCode.H1_ONLINE_REFUND_ERROR
            )
        )
    }

    override fun cancel() {
        isOperationCancelled = true
        cancelAllNetworkRequests()
        val cancelErrorCode: FingoErrorCode = FingoPayDriver.cancelCaptureSession()
        Log.d(TAG, "Canceling Result: " + cancelErrorCode.name)
        if (cancelErrorCode == FingoErrorCode.H1_CANCELLED) {
            Log.d(TAG, "Internal Cancel Function DONE")
            presenter.onDisplayTextRequested(this.buildDisplayTextRequested(DisplayMsgCode.CANCELLED))
        }
    }

    fun cancelAllNetworkRequests() {
        if (enrollmentResponseCall != null) {
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Enrollment API call")
            enrollmentResponseCall!!.cancel()
        }
        if (identifyResponseCall != null) {
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call")
            identifyResponseCall!!.cancel()
        }
        if (paymentResponseCall != null) {
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call")
            paymentResponseCall!!.cancel()
        }
        if (refundResponseCall != null) {
            Log.i(TAG, "cancelAllNetworkRequests: Cancelling Identify API call")
            refundResponseCall!!.cancel()
        }
    }

    private fun buildDisplayTextRequested(displayMsgCode: DisplayMsgCode): DisplayTextRequested {
        return DisplayTextRequested(
            context, displayMsgCode
        )
    }

    private fun buildDisplayTextRequested(fingoErrorCode: FingoErrorCode): DisplayTextRequested {
        return DisplayTextRequested(
            context, fingoErrorCode
        )
    }

    private fun buildOnlineIdentifyResponse(identifyResponse: IdentifyResponse): IdentifyData {
        val identifyData = IdentifyData()
        identifyData.memberId = identifyResponse.memberId
        identifyData.veinId = identifyResponse.veinId
        identifyData.isOnlineData = true
        return identifyData
    }

    private fun buildOnlineIdentifyResponse(enrollmentResponse: EnrollmentResponse): IdentifyData {
        val identifyData = IdentifyData()
        identifyData.memberId = enrollmentResponse.memberId
        identifyData.veinId = enrollmentResponse.veinId
        identifyData.isOnlineData = true
        return identifyData
    }

    private fun buildOfflineResponse(
        verificationTemplate: String,
        enrolmentTemplate: String
    ): IdentifyData {
        val identifyData = IdentifyData()
        identifyData.verificationTemplate = verificationTemplate
        identifyData.enrolmentTemplate = enrolmentTemplate
        identifyData.isOnlineData = false
        return identifyData
    }

    private fun buildOnlinePaymentResponse(paymentResponse: PaymentResponse): PaymentData {
        val paymentData = PaymentData()
        paymentData.transactionId = paymentResponse.transactionId
        paymentData.gatewayAuthCode = paymentResponse.gatewayAuthCode
        paymentData.gatewayTransactionId = paymentResponse.gatewayTransactionId
        paymentData.maskedCardNumber = paymentResponse.maskedCardNumber
        paymentData.timestamp = paymentResponse.timestamp
        return paymentData
    }

    private fun buildOnlineRefundResponse(refundResponse: RefundResponse): PaymentData {
        val paymentData = PaymentData()
        paymentData.transactionId = refundResponse.transactionId
        paymentData.gatewayAuthCode = refundResponse.gatewayAuthCode
        paymentData.gatewayTransactionId = refundResponse.gatewayTransactionId
        paymentData.maskedCardNumber = refundResponse.maskedCardNumber
        paymentData.timestamp = refundResponse.timestamp
        return paymentData
    }

    private fun buildProcessingFinishedEvent(
        status: Boolean,
        fingoErrorCode: FingoErrorCode
    ): ProcessingFinished {
        val processingFinished =
            buildProcessingFinishedEvent(status, fingoErrorCode.descriptionResId)
        processingFinished.errorCode = fingoErrorCode.errorCode
        processingFinished.errorName = fingoErrorCode.name
        return processingFinished
    }

    private fun buildProcessingFinishedEvent(status: Boolean, msgId: Int): ProcessingFinished {
        val processingFinished = ProcessingFinished()
        processingFinished.errorCode = FingoErrorCode.H1_UNKNOWN_ERROR.errorCode
        processingFinished.errorName = FingoErrorCode.H1_UNKNOWN_ERROR.name
        processingFinished.status = status
        processingFinished.text = context.getString(msgId)
        return processingFinished
    }

    companion object {
        private val TAG = FingoModel::class.java.simpleName
    }

    init {
        canProceed =
            FingoSDK.isSdkInitialized && Storage.getBoolean(StorageKey.PARAMS_STATUS.name, false)
        fingoPayDriver = FingoPayDriver
        fingoRequestHelper = FingoRequestHelper()
        val httpLoggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                FingoSDK.fingoRequestLogger?.onLogDataAvailable(message) ?: Log.d(TAG, message)
            }
        })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()
    }
}