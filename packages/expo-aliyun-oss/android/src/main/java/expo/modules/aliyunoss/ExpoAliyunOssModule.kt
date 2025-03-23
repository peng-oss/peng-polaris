package expo.modules.aliyunoss

import android.content.pm.PackageManager
import android.util.Base64
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectRequest
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectResult
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL

class ExpoAliyunOssModule : Module() {
    var ossClient: OSSClient? = null
    var bucketName: String? = null
    var endpoint: String? = null

    override fun definition() = ModuleDefinition {
        Name("ExpoAliyunOSS")

        Constants(
            "PI" to Math.PI
        )

        Function("initWithAK") { ossAccessKeySecretID: String, ossAccessKeySecret: String, securityToken: String, bucket: String, _endpoint: String,expiration: String ,promise: Promise  ->

            val credentialProvider = object : OSSFederationCredentialProvider() {
                override fun getFederationToken(): OSSFederationToken {
                    return OSSFederationToken(ossAccessKeySecretID, ossAccessKeySecret, securityToken,expiration)
                }
            }
            val configuration = ClientConfiguration()
            // TODO: 设置请求超时等信息
            ossClient = OSSClient(appContext.reactContext, _endpoint, credentialProvider, configuration)
            bucketName = bucket
            endpoint = _endpoint

            try {
                val credentialProvider = object : OSSFederationCredentialProvider() {
                    override fun getFederationToken(): OSSFederationToken {
                        return OSSFederationToken(ossAccessKeySecretID, ossAccessKeySecret, securityToken, expiration)
                    }
                }
                val configuration = ClientConfiguration()
                // TODO: 设置请求超时等信息
                ossClient = OSSClient(appContext.reactContext, _endpoint, credentialProvider, configuration)
                bucketName = bucket
                endpoint = _endpoint

                // 初始化成功
                promise.resolve("初始化成功")
            } catch (e: Exception) {
                // 初始化失败
                promise.reject(
                    CodedException(
                        code = "INIT_FAILED", // 自定义错误码
                        message = "初始化失败: ${e.message ?: "未知错误"}", // 错误信息
                        cause = e // 原始异常
                    )
                )
            }
        }
        AsyncFunction("uploadAsync") { fileUriOrBase64: String, fileKey: String, promise: Promise ->
            val request: PutObjectRequest
            if (fileUriOrBase64.startsWith("data:")) {
                val byteArray = Base64.decode(fileUriOrBase64, Base64.DEFAULT)
                request = PutObjectRequest(bucketName, fileKey, byteArray)
            } else {
                val resolvedFilePath = fileUriOrBase64.replace("file://", "")
                request = PutObjectRequest(bucketName, fileKey, resolvedFilePath)
            }
            request.progressCallback = object : OSSProgressCallback<PutObjectRequest> {
                override fun onProgress(request: PutObjectRequest?, currentSize: Long, totalSize: Long) {
                    sendEvent(
                        "uploadProgress",
                        mapOf(
                            "uploadedSize" to currentSize,
                            "totalSize" to totalSize,
                            "fileKey" to fileKey
                        )
                    )
                }
            }
            val task = ossClient?.asyncPutObject(
                request,
                object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                        promise.resolve(result?.toString())
                    }

                    override fun onFailure(request: PutObjectRequest?, clientException: ClientException?, serviceException: ServiceException?) {
                        val throwable = clientException ?: serviceException
                        promise.reject(
                            CodedException(
                                "Resource: ${fileKey} upload failed. Client Exception：${clientException.toString()}，Service Exception: ${serviceException.toString()}",
                                throwable
                            )
                        )
                    }
                })


        }

        AsyncFunction("deleteObjectsAsync") { fileKeys: List<String>, promise: Promise ->
            if (bucketName != null && ossClient != null) {
                val request = DeleteMultipleObjectRequest(bucketName, fileKeys, true)
                ossClient?.asyncDeleteMultipleObject(
                    request,
                    object : OSSCompletedCallback<DeleteMultipleObjectRequest, DeleteMultipleObjectResult> {
                        override fun onSuccess(request: DeleteMultipleObjectRequest?, result: DeleteMultipleObjectResult?) {
                            promise.resolve(result.toString())
                        }

                        override fun onFailure(request: DeleteMultipleObjectRequest?, clientException: ClientException?, serviceException: ServiceException?) {
                            val throwable = clientException ?: serviceException
                            promise.reject(
                                CodedException(
                                    "Resource: ${fileKeys} delete failed. Client Exception：${clientException.toString()}，Service Exception: ${serviceException.toString()}",
                                    throwable
                                )
                            )
                        }
                    })
            }
        }

        Events("uploadProgress")
    }
}