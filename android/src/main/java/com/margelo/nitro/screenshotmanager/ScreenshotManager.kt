package com.margelo.nitro.screenshotmanager

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.modules.dialog.AlertFragment
import com.margelo.nitro.NitroModules
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.Objects


@DoNotStrip
class ScreenshotManager : HybridScreenshotManagerSpec(),
  ViewTreeObserver.OnWindowFocusChangeListener {
  private val applicationContext = NitroModules.applicationContext
  private var securedDialog: Dialog? = null
  private var listeners = mutableListOf<() -> Unit>()

  val screenCaptureCallback = Activity.ScreenCaptureCallback {
    for (listener in listeners) {
      listener()
    }
  }

  @SuppressLint("MissingPermission")
  override fun enabled(value: Boolean) {
    Log.i("ScreenshotManager", "enabled " + value)
    val activity = this.applicationContext?.getCurrentActivity()
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

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        activity.registerScreenCaptureCallback(activity.mainExecutor, screenCaptureCallback)
      }
    } else {
      activity.runOnUiThread {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        activity.unregisterScreenCaptureCallback(screenCaptureCallback)
      }
    }
  }

  override fun enableSecureView(imagePath: String?) {
    Log.i("ScreenshotManager", "enable secured view " + imagePath)
    val activity = this.applicationContext?.getCurrentActivity()
    if (this.applicationContext?.hasCurrentActivity() == false || activity == null) {
      return
    }
    activity.runOnUiThread {
      this.createSecuredDialog(imagePath, activity)
      enabled(true)
      activity.window.decorView.rootView.viewTreeObserver.addOnWindowFocusChangeListener(this)
    }
  }

  override fun disableSecureView() {
    Log.i("ScreenshotManager", "disable secured view")
    val activity = this.applicationContext?.getCurrentActivity()
    if (this.applicationContext?.hasCurrentActivity() == false || activity == null) {
      return
    }
    activity.runOnUiThread {
      this.securedDialog = null
      enabled(false)
      activity.window.decorView.rootView.viewTreeObserver.removeOnWindowFocusChangeListener(this)
    }
  }

  override fun addListener(listener: () -> Unit): () -> Unit {
    listeners.add(listener)

    return { removeListener(listener) }
  }

  private fun removeListener(listener: () -> Unit) {
    listeners.remove(listener)
  }

  private fun decodeImageUrl(imagePath: String): Bitmap? {
    try {
      val imageUrl = URL(imagePath)
      return BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
    } catch (e: IOException) {
      if (e is MalformedURLException) {
        try {
          val resourceId: Int = this.applicationContext?.resources?.getIdentifier(
            imagePath, "drawable",
            this.applicationContext.getPackageName()
          ) ?: 0
          return BitmapFactory.decodeResource(
            this.applicationContext?.resources,
            resourceId
          )
        } catch (ee: Exception) {
          Log.e("RNScreenshotPreventModule", "exception", ee)
          return null
        }
      }
      Log.e("RNScreenshotPreventModule", "exception", e)
      return null
    }
  }

  private fun createSecuredDialog(@Nullable imagePath: String?, activity: Activity) {
    this.securedDialog = Dialog(activity, R.style.Theme_Light)
    this.securedDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull(this.securedDialog!!.getWindow())?.setFlags(
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    )

    val layout = RelativeLayout(activity)
    layout.setBackgroundColor(Color.parseColor("#FFFFFF"))

    val imageView = ImageView(activity)
    val imageParams = RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT
    )
    imageParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

    imageView.layoutParams = imageParams

    // Set image resource
    if (imagePath != null) {
      val bitmap: Bitmap? = decodeImageUrl(imagePath)

      if (bitmap != null) {
        val imageHeight =
          (bitmap.height * (activity.resources.displayMetrics.widthPixels.toFloat() / bitmap.width)).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(
          bitmap,
          activity.resources.displayMetrics.widthPixels,
          imageHeight,
          true
        )
        imageView.setImageBitmap(scaledBitmap)
      }

      layout.addView(imageView)
    }

    this.securedDialog!!.setContentView(layout)
  }

  private fun getSecuredDialog(): Dialog? {
    val activity: Activity? = this.applicationContext?.currentActivity
    if (this.securedDialog == null && activity != null) {
      this.createSecuredDialog(null, activity)
    }

    return securedDialog
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    Log.i("ScreenshotManager", "onWindowFocusChanged")
    val activity = this.applicationContext?.getCurrentActivity()
    if (this.applicationContext?.hasCurrentActivity() == false || activity == null) {
      return
    }
    var hasAlert = false
    if (activity is FragmentActivity) {
      val fragments = activity.supportFragmentManager.fragments
      for (fragment in fragments) {
        val isDiablog = fragment is DialogFragment
        val isAlert = fragment is AlertFragment
        if (isDiablog || isAlert) {
          hasAlert = true
          break
        }
      }
    }

    if (!hasFocus && !hasAlert) {
      Log.i("ScreenshotManager", "call to hide content")
      this.getSecuredDialog()?.show()
      activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
      Log.i("ScreenshotManager", "call to show content")
      this.getSecuredDialog()?.hide()
      activity.window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
      )
    }
  }
}
