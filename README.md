# react-native-screenshot-manager

A React Native Nitro Module to manage screenshot prevention and detection.

## Features

- **Prevent Screenshots & Screen Recording**:
  - **Android**: Uses `FLAG_SECURE` to prevent screen capture and recording.
  - **iOS**: Uses a secure hidden field trick to mask the content in screenshots and recordings.
- **Screenshot Detection**:
  - Listen for screenshot events (Android 14+ and iOS).
- **Privacy Protection (iOS)**:
  - Automatically blurs the screen when the app moves to the background/multitasking view.

## Installation

1. Install the package and its peer dependency `react-native-nitro-modules`:

```bash
npm install react-native-screenshot-manager react-native-nitro-modules
# or
yarn add react-native-screenshot-manager react-native-nitro-modules
```

2. (iOS) Install Pods:

```bash
cd ios && pod install
```

## Usage

### Prevent Screenshots

Enable or disable screenshot protection.

```typescript
import { enabled } from 'react-native-screenshot-manager';

// Enable screenshot protection (prevents screenshots/recording)
enabled(true);

// Disable screenshot protection
enabled(false);
```

### Detect Screenshots

Add a listener to detect when a user takes a screenshot.

```typescript
import { addListener } from 'react-native-screenshot-manager';
import { useEffect } from 'react';

// ... inside your component
useEffect(() => {
  const removeListener = addListener(() => {
    console.log('User took a screenshot!');
    // You can show an alert or log the event
  });

  return () => {
    removeListener();
  };
}, []);
```

## Platform Support

| Feature | iOS | Android |
| :--- | :---: | :---: |
| Prevent Screenshot | ✅ | ✅ |
| Screenshot Listener | ✅ | ✅ (Android 14+) |
| Privacy Blur (Background) | ✅ | ❌ |

## Implementation Details

- **Android**:
  - Uses `WindowManager.LayoutParams.FLAG_SECURE` for preventing screenshots.
  - Uses `Activity.registerScreenCaptureCallback` (API 34+) for detection.
- **iOS**:
  - Uses a hidden `UITextField` with `isSecureTextEntry = true` attached to the window to obscure content.
  - Listens to `UIApplication.userDidTakeScreenshotNotification`.
  - Observes `UIApplication.willResignActiveNotification` and `didBecomeActiveNotification` to apply a blur effect in the app switcher.

## License

MIT
