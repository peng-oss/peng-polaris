import AliyunOSSiOS
import ExpoModulesCore

let UPLOAD_PROGRESS_EVENT = "uploadProgress"

public class ExpoAliyunOssModule: Module {
  private var ossClient: OSSClient?
  private var bucketName: String?
  private var endpoint: String?
  
  public static func moduleName() -> String! {
    return "ExpoAliyunOSS"
  }
  
  public static func requiresMainQueueSetup() -> Bool {
    return false
  }
  
  public  func definition() -> ModuleDefinition {
    Name("ExpoAliyunOSS")
    
    Function("initWithAK") { (
      ossAccessKeySecretID: String,
      ossAccessKeySecret: String,
      securityToken: String,
      bucket: String,
      endpoint: String,
      expiration: String,
      promise: Promise
    ) in
      let credentialProvider = OSSStsTokenCredentialProvider(
        accessKeyId: ossAccessKeySecretID,
        secretKeyId: ossAccessKeySecret,
        securityToken: securityToken
      )
      
      let configuration = OSSClientConfiguration()
      configuration.maxRetryCount = 3
      configuration.timeoutIntervalForRequest = 30
      
      self.ossClient = OSSClient(
        endpoint: endpoint,
        credentialProvider: credentialProvider,
        clientConfiguration: configuration
      )
      
      self.bucketName = bucket
      self.endpoint = endpoint
      promise.resolve(nil)
    }
    
    AsyncFunction("uploadAsync") { (
      fileUriOrBase64: String,
      fileKey: String,
      promise: Promise
    ) in
      guard let bucket = self.bucketName, let client = self.ossClient else {
        promise.reject("NOT_INITIALIZED", "OSS client not initialized")
        return
      }
      
      let putRequest = OSSPutObjectRequest()
      putRequest.bucketName = bucket
      putRequest.objectKey = fileKey
      
      if fileUriOrBase64.hasPrefix("data:") {
        let base64String = fileUriOrBase64.components(separatedBy: ",").last ?? ""
        if let data = Data(base64Encoded: base64String) {
          putRequest.uploadingData = data
        }
      } else {
        let fileURL = URL(string: fileUriOrBase64.replacingOccurrences(of: "file://", with: ""))!
        putRequest.uploadingFileURL = fileURL
      }
      
      putRequest.uploadProgress = { (bytesSent: Int64, totalBytesSent: Int64, totalBytesExpectedToSend: Int64) in
        self.sendEvent(UPLOAD_PROGRESS_EVENT, [
          "uploadedSize": totalBytesSent,
          "totalSize": totalBytesExpectedToSend,
          "fileKey": fileKey
        ])
      }
      
      let task = client.putObject(putRequest)
        task.continue({ (task: OSSTask<AnyObject>) -> Any? in
           if let error = task.error {
             promise.reject("UPLOAD_FAILED", "Delete failed: \(error.localizedDescription)")
           } else {
             // 类型安全转换
             if let _ = task.result as? OSSDeleteMultipleObjectsResult {
               promise.resolve(["result": "UPLOAD completed"])
             } else {
               promise.reject("UPLOAD_FAILED", "Unexpected result type")
             }
           }
           return nil
         })
    }
    
    AsyncFunction("deleteObjectsAsync") { (fileKeys: [String], promise: Promise) in
      guard let bucket = self.bucketName, let client = self.ossClient else {
        promise.reject("NOT_INITIALIZED", "OSS client not initialized")
        return
      }
      
      let deleteRequest = OSSDeleteMultipleObjectsRequest()
      deleteRequest.bucketName = bucket
      deleteRequest.keys = fileKeys
      deleteRequest.quiet = true
      
      let task = client.deleteMultipleObjects(deleteRequest)
        task.continue({ (task: OSSTask<AnyObject>) -> Any? in
           if let error = task.error {
             promise.reject("DELETE_FAILED", "Delete failed: \(error.localizedDescription)")
           } else {
             // 类型安全转换
             if let _ = task.result as? OSSDeleteMultipleObjectsResult {
               promise.resolve(["result": "Delete completed"])
             } else {
               promise.reject("DELETE_FAILED", "Unexpected result type")
             }
           }
           return nil
         })
    }
    
    Events(UPLOAD_PROGRESS_EVENT)
  }
  
  @objc public func supportedEvents() -> [String]! {
    return [UPLOAD_PROGRESS_EVENT]
  }
  
  private func sendEvent(_ name: String, _ body: Any?) {
    self.appContext?.eventEmitter?.sendEvent(withName: name, body: body)
  }
}
