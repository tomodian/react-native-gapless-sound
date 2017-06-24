require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name            = "RNGaplessSound"
  s.version         = package['version']
  s.summary         = package['description']
  s.homepage        = "https://github.com/tomodian/react-native-gapless-sound"
  s.license         = package['license']
  s.author          = package['author']
  s.source          = { :git => package['repository']['url'], :tag => s.version }
  s.default_subspec = 'Core'
  s.requires_arc    = true
  s.platform        = :ios, "7.0"

  s.dependency "React/Core"

  s.subspec 'Core' do |ss|
    ss.source_files     = "RNGaplessSound/**/*.{h,m}"
  end

end