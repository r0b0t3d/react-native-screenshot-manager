#include <jni.h>
#include "ScreenshotManagerOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return facebook::jni::initialize(vm, []() {
    margelo::nitro::screenshotmanager::registerAllNatives();
  });
}
