class ScreenshotManager: HybridScreenshotManagerSpec {
  private var enabled = false
  private var blurEffectView: UIVisualEffectView?
  private var listerners = [() -> Void]()
  
  override init() {
    super.init()
    NotificationCenter.default.addObserver(self, selector: #selector(handleAppStateResignActive), name: UIApplication.willResignActiveNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(handleAppStateActive), name: UIApplication.didBecomeActiveNotification, object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(handleAppScreenshotNotification), name: UIApplication.userDidTakeScreenshotNotification, object: nil)
  }
  
  @objc private func handleAppStateResignActive() {
    guard enabled, let keyWindow = UIApplication.shared.connectedScenes
      .compactMap({ ($0 as? UIWindowScene)?.windows.first { $0.isKeyWindow } })
      .first else { return }
    
    self.blurEffectView = keyWindow.addBlurView()
  }
  
  @objc private func handleAppStateActive() {
    guard blurEffectView?.superview != nil else { return }
    
    UIView.animate(withDuration: 0.3, animations: {
      self.blurEffectView?.alpha = 0
    }, completion: { _ in
      self.blurEffectView?.removeFromSuperview()
      self.blurEffectView = nil
    })
  }
  
  @objc private func handleAppScreenshotNotification() {
    for listerner in listerners {
      listerner()
    }
  }
  
  func enabled(value: Bool) throws {
    self.enabled = value
    
    DispatchQueue.main.async {
      if value {
        guard let keyWindow = UIApplication.shared.connectedScenes
          .compactMap({ ($0 as? UIWindowScene)?.windows.first { $0.isKeyWindow } })
          .first else { return }
        keyWindow.makeSecure()
      } else {
        UIApplication.shared.connectedScenes
          .compactMap { $0 as? UIWindowScene }
          .flatMap { $0.windows }
          .forEach { $0.makeInsecure() }
      }
    }
  }
  
  func addListener(listener: @escaping () -> Void) throws -> () -> Void {
    self.listerners.append(listener)
    
    return {
      self.listerners.remove(at: 0)
    }
  }
}

private class SecureTextField: UITextField {
  override var canBecomeFirstResponder: Bool { false }

  override func canPerformAction(_ action: Selector, withSender sender: Any?) -> Bool {
    return false
  }

  override func selectionRects(for range: UITextRange) -> [UITextSelectionRect] {
    return []
  }

  override func caretRect(for position: UITextPosition) -> CGRect {
    return .zero
  }
}

extension UIWindow {
  private var secureFieldTag: Int { 97531 }

  func makeSecure() {
    guard self.viewWithTag(secureFieldTag) == nil else { return }

    let field = SecureTextField()
    field.tag = secureFieldTag
    field.isUserInteractionEnabled = false
    let view = UIView(frame: CGRect(x: 0, y: 0, width: field.frame.self.width, height: field.frame.self.height))
    field.isSecureTextEntry = true
    self.addSubview(field)
    
    guard let superlayer = self.layer.superlayer else { return }
    
    superlayer.addSublayer(field.layer)
    field.layer.sublayers?.last?.addSublayer(self.layer)
    
    field.leftView = view
    field.leftViewMode = .always
  }

  func makeInsecure() {
    guard let field = self.viewWithTag(secureFieldTag) else { return }
    
    if let superlayer = field.layer.superlayer {
      superlayer.addSublayer(self.layer)
    }
    
    field.removeFromSuperview()
    field.layer.removeFromSuperlayer()
  }
  
  func addBlurView() -> UIVisualEffectView {
    let blurEffect = UIBlurEffect(style: .light)
    let blurEffectView = UIVisualEffectView(effect: blurEffect)
    
    blurEffectView.frame = self.bounds
    blurEffectView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    blurEffectView.alpha = 0        // Start invisible
    
    self.addSubview(blurEffectView)
    // Animate the blur effect view's appearance
    UIView.animate(withDuration: 0.3) {
      blurEffectView.alpha = 1        // Fade in
    }
    return blurEffectView
  }
}
