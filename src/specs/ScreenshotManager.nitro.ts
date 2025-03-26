import type { HybridObject } from 'react-native-nitro-modules';

export interface ScreenshotManager
  extends HybridObject<{ ios: 'swift'; android: 'kotlin' }> {
  enabled(value: boolean): void;
  addListener(listener: () => void): () => void;
}
