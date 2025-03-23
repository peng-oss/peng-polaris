import { NativeModule, requireNativeModule } from 'expo';

import { ExpoAliyunOSSModuleEvents } from './ExpoAliyunOss.types';

declare class ExpoAliyunOSSModule extends NativeModule<ExpoAliyunOSSModuleEvents> {
  /**
   * 异步上传文件
   * @param fileUriOrBase64 文件本地URI或者Base64格式的文件内容
   * @param fileKey 文件在阿里云OSS的存储目录，包含路径和文件名
   */
  uploadAsync(fileUriOrBase64: string, fileKey: string): Promise<any>;
  /**
   * 批量删除文件
   * @param fileKeys 待删除的文件列表
   */
  deleteObjectsAsync(fileKeys: string[]): Promise<any>

  /**
   * 使用阿里云AK初始化
   * 
   * @param ossAccessKeySecretID 阿里云Access Key ID
   * @param ossAccessKeySecret 阿里云Access Key Secret
   * @param securityToken 阿里云securityToken
   * @param bucket 阿里云bucket
   * @param endpoint 阿里云endpoint，比如oss-cn-beijing.aliyuncs.com
   * @param expiration 阿里云expiration，比如2025-03-20 06:51:01
   */
  initWithAK(
    ossAccessKeySecretID: string,
    ossAccessKeySecret: string,
    securityToken: string,
    bucket: string,
    endpoint: string,
    expiration?: string,
  ): void;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoAliyunOSSModule>("ExpoAliyunOss");
