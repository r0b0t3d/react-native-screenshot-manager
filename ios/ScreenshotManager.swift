class ScreenshotManager: HybridScreenshotManagerSpec {
  private var enabled = false
  private var obfuscatingView: UIImageView?
  private var listerners = [() -> Void]()
  
  override init() {
    super.init()
//    NotificationCenter.default.addObserver(self, selector: #selector(handleAppStateResignActive), name: UIApplication.willResignActiveNotification, object: nil)
//    NotificationCenter.default.addObserver(self, selector: #selector(handleAppStateActive), name: UIApplication.didBecomeActiveNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(handleAppScreenshotNotification), name: UIApplication.userDidTakeScreenshotNotification, object: nil)
  }
  
  @objc private func handleAppStateResignActive() {
    guard enabled, let keyWindow = UIApplication.shared.keyWindow else {
      return
    }
    
    UIGraphicsBeginImageContext(keyWindow.bounds.size)
    keyWindow.drawHierarchy(in: keyWindow.bounds, afterScreenUpdates: false)
    let viewImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    
    if let blurredImage = viewImage?.applyBlurEffect() {
      obfuscatingView = UIImageView(image: blurredImage)
      obfuscatingView?.frame = keyWindow.bounds
      keyWindow.addSubview(obfuscatingView!)
    }
  }
  
  @objc private func handleAppStateActive() {
    UIView.animate(withDuration: 0.3, animations: {
      self.obfuscatingView?.alpha = 0
    }) { _ in
      self.obfuscatingView?.removeFromSuperview()
      self.obfuscatingView = nil
    }
  }
  
  @objc private func handleAppScreenshotNotification() {
    for listerner in listerners {
      listerner()
    }
  }
  
  func enabled(value: Bool) throws {
    self.enabled = value
  }
  
  func addListener(listener: @escaping () -> Void) throws -> () -> Void {
    self.listerners.append(listener)
    
    return {
      self.listerners.remove(at: 0)
    }
  }
}

extension UIImage {
  func applyBlurEffect() -> UIImage? {
    let context = CIContext(options: nil)
    guard let currentFilter = CIFilter(name: "CIGaussianBlur") else { return nil }
    currentFilter.setValue(CIImage(image: self), forKey: kCIInputImageKey)
    currentFilter.setValue(5.0, forKey: kCIInputRadiusKey)
    
    guard let outputImage = currentFilter.outputImage, let cgImage = context.createCGImage(outputImage, from: outputImage.extent) else { return nil }
    return UIImage(cgImage: cgImage)
  }
}
