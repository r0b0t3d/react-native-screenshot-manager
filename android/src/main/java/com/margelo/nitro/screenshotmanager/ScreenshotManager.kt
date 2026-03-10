package com.margelo.nitro.screenshotmanager

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.margelo.nitro.NitroModules

@DoNotStrip
class ScreenshotManager : HybridScreenshotManagerSpec(), LifecycleEventListener, Application.ActivityLifecycleCallbacks {
  private val applicationContext = NitroModules.applicationContext as? ReactApplicationContext
  private var listeners = mutableListOf<() -> Unit>()
  private var isSecure = false
  private var screenCaptureCallback: Any? = null

  init {
    if (Build.VERSION.SDK_INT >= 34) {
      screenCaptureCallback = Activity.ScreenCaptureCallback {
        Log.d("ScreenshotManager", "Screen captured")
        synchronized(listeners) {
          for (listener in listeners) {
            listener()
          }
        }
      }
    }

    applicationContext?.addLifecycleEventListener(this)
    val app = applicationContext?.currentActivity?.application ?: applicationContext?.applicationContext as? Application
    app?.registerActivityLifecycleCallbacks(this)
  }

  override fun enabled(value: Boolean) {
    Log.i("ScreenshotManager", "enabled $value")
    isSecure = value
    val activity = applicationContext?.currentActivity ?: return
    updateSecureFlag(activity)
  }

  override fun addListener(listener: () -> Unit): () -> Unit {
    synchronized(listeners) {
      listeners.add(listener)
      if (listeners.size == 1) {
        val activity = applicationContext?.currentActivity
        if (activity != null) {
          registerScreenCaptureCallback(activity)
        }
      }
    }
    return { removeListener(listener) }
  }

  private fun removeListener(listener: () -> Unit) {
    synchronized(listeners) {
      listeners.remove(listener)
      if (listeners.isEmpty()) {
        val activity = applicationContext?.currentActivity
        if (activity != null) {
          unregisterScreenCaptureCallback(activity)
        }
      }
    }
  }

  private fun updateSecureFlag(activity: Activity) {
    activity.runOnUiThread {
      if (isSecure) {
        activity.window.setFlags(
          WindowManager.LayoutParams.FLAG_SECURE,
          WindowManager.LayoutParams.FLAG_SECURE
        )
      } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      }
    }
  }

  private fun registerScreenCaptureCallback(activity: Activity) {
    if (Build.VERSION.SDK_INT >= 34) {
      val callback = screenCaptureCallback as? Activity.ScreenCaptureCallback ?: return
      try {
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
      } catch (e: Exception) {
        Log.e("ScreenshotManager", "Failed to register screen capture callback", e)
      }
    }
  }

  private fun unregisterScreenCaptureCallback(activity: Activity) {
    if (Build.VERSION.SDK_INT >= 34) {
      val callback = screenCaptureCallback as? Activity.ScreenCaptureCallback ?: return
      try {
        activity.unregisterScreenCaptureCallback(callback)
      } catch (e: Exception) {
        Log.e("ScreenshotManager", "Failed to unregister screen capture callback", e)
      }
    }
  }

  // LifecycleEventListener
  override fun onHostResume() {
    val activity = applicationContext?.currentActivity
    if (activity != null) {
      if (isSecure) updateSecureFlag(activity)
      synchronized(listeners) {
        if (listeners.isNotEmpty()) {
          registerScreenCaptureCallback(activity)
        }
      }
    }
  }

  override fun onHostPause() {
    // We rely on onActivityStopped for unregistration to support split-screen (visible but paused)
  }

  override fun onHostDestroy() {
    applicationContext?.removeLifecycleEventListener(this)
    val app = applicationContext?.currentActivity?.application ?: applicationContext?.applicationContext as? Application
    app?.unregisterActivityLifecycleCallbacks(this)
  }

  // Application.ActivityLifecycleCallbacks
  override fun onActivityStarted(activity: Activity) {
    if (isSecure) updateSecureFlag(activity)
    synchronized(listeners) {
      if (listeners.isNotEmpty()) {
        registerScreenCaptureCallback(activity)
      }
    }
  }

  override fun onActivityStopped(activity: Activity) {
    unregisterScreenCaptureCallback(activity)
  }

  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
  override fun onActivityResumed(activity: Activity) {}
  override fun onActivityPaused(activity: Activity) {}
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
  override fun onActivityDestroyed(activity: Activity) {}
}
