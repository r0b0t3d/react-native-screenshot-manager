#include <jni.h>
#include "ScreenshotManagerOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::screenshotmanager::initialize(vm);
}
