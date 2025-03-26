package com.margelo.nitro.screenshotmanager

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.NitroModules


@DoNotStrip
class ScreenshotManager : HybridScreenshotManagerSpec() {
  private val applicationContext = NitroModules.applicationContext
  private var listeners = mutableListOf<() -> Unit>()

  val screenCaptureCallback = Activity.ScreenCaptureCallback {
    Log.d("ScreenshotManager", "Screen captured")
    for (listener in listeners) {
      listener()
    }
  }

  override fun enabled(value: Boolean) {
    Log.i("ScreenshotManager", "enabled " + value)
    val activity = this.applicationContext?.currentActivity
    if (this.applicationContext?.hasCurrentActivity() == false || activity == null) {
      return
    }
    if (value) {
      activity.runOnUiThread {
        activity.window.setFlags(
          WindowManager.LayoutParams.FLAG_SECURE,
          WindowManager.LayoutParams.FLAG_SECURE
        )
      }
    } else {
      activity.runOnUiThread {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      }
    }
  }

  override fun addListener(listener: () -> Unit): () -> Unit {
    listeners.add(listener)
    if (listeners.size == 1) {
      val activity = this.applicationContext?.currentActivity
      if (this.applicationContext?.hasCurrentActivity() == true && activity != null) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          activity.registerScreenCaptureCallback(activity.mainExecutor, screenCaptureCallback)
        }
      }
    }

    return { removeListener(listener) }
  }

  private fun removeListener(listener: () -> Unit) {
    listeners.remove(listener)
    if (listeners.size == 0) {
      val activity = this.applicationContext?.currentActivity
      if (this.applicationContext?.hasCurrentActivity() == true && activity != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          activity.unregisterScreenCaptureCallback(screenCaptureCallback)
        }
      }
    }
  }
}
