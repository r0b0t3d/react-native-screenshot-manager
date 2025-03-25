#include <jni.h>
#include "ScreenshotMangerOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::screenshotmanager::initialize(vm);
}
