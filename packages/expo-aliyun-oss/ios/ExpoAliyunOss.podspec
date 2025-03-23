require 'json'

package = JSON.parse(File.read(File.join(__dir__, '..', 'package.json')))

Pod::Spec.new do |s|
  s.name           = 'ExpoAliyunOss'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.platforms      = {
    :ios => '15.1',
    :tvos => '15.1'
  }
  s.swift_version  = '5.4'
  s.source         = { git: 'https://github.com/peng-oss/peng-polaris.git' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'
    # 关键：声明阿里云 OSS 依赖
  s.dependency "AliyunOSSiOS", "~> 2.10.15"

    # 如果需要其他系统框架
  s.frameworks = 'SystemConfiguration', 'CoreTelephony'

  # Swift/Objective-C compatibility
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
  }

  s.source_files = "**/*.{h,m,mm,swift,hpp,cpp}"
end
