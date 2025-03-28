///
/// ScreenshotManagerOnLoad.cpp
/// This file was generated by nitrogen. DO NOT MODIFY THIS FILE.
/// https://github.com/mrousavy/nitro
/// Copyright © 2025 Marc Rousavy @ Margelo
///

#ifndef BUILDING_SCREENSHOTMANAGER_WITH_GENERATED_CMAKE_PROJECT
#error ScreenshotManagerOnLoad.cpp is not being built with the autogenerated CMakeLists.txt project. Is a different CMakeLists.txt building this?
#endif

#include "ScreenshotManagerOnLoad.hpp"

#include <jni.h>
#include <fbjni/fbjni.h>
#include <NitroModules/HybridObjectRegistry.hpp>

#include "JHybridScreenshotManagerSpec.hpp"
#include "JFunc_void.hpp"
#include <NitroModules/JNISharedPtr.hpp>
#include <NitroModules/DefaultConstructableObject.hpp>

namespace margelo::nitro::screenshotmanager {

int initialize(JavaVM* vm) {
  using namespace margelo::nitro;
  using namespace margelo::nitro::screenshotmanager;
  using namespace facebook;

  return facebook::jni::initialize(vm, [] {
    // Register native JNI methods
    margelo::nitro::screenshotmanager::JHybridScreenshotManagerSpec::registerNatives();
    margelo::nitro::screenshotmanager::JFunc_void_cxx::registerNatives();

    // Register Nitro Hybrid Objects
    HybridObjectRegistry::registerHybridObjectConstructor(
      "ScreenshotManager",
      []() -> std::shared_ptr<HybridObject> {
        static DefaultConstructableObject<JHybridScreenshotManagerSpec::javaobject> object("com/margelo/nitro/screenshotmanager/ScreenshotManager");
        auto instance = object.create();
        auto globalRef = jni::make_global(instance);
        return JNISharedPtr::make_shared_from_jni<JHybridScreenshotManagerSpec>(globalRef);
      }
    );
  });
}

} // namespace margelo::nitro::screenshotmanager
