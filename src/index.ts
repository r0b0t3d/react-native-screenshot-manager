import { NitroModules } from 'react-native-nitro-modules';
import type { ScreenshotManager } from './specs/ScreenshotManager.nitro';

const ScreenshotManagerHybridObject =
  NitroModules.createHybridObject<ScreenshotManager>('ScreenshotManager');

export function enabled(value: boolean) {
  ScreenshotManagerHybridObject.enabled(value);
}
export function addListener(fn: () => void): () => void {
  return ScreenshotManagerHybridObject.addListener(fn);
}
