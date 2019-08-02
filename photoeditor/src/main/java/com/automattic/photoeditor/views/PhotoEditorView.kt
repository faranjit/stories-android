package com.automattic.photoeditor.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import com.automattic.photoeditor.views.filter.CustomEffect
import com.automattic.photoeditor.OnSaveBitmap
import com.automattic.photoeditor.R.styleable
import com.automattic.photoeditor.views.background.fixed.BackgroundImageView
import com.automattic.photoeditor.views.background.video.AutoFitTextureView
import com.automattic.photoeditor.views.brush.BrushDrawingView
import com.automattic.photoeditor.views.filter.ImageFilterView
import com.automattic.photoeditor.views.filter.PhotoFilter

/**
 *
 *
 * This ViewGroup will have the [BrushDrawingView] to draw paint on it with [ImageView]
 * which our source image
 *
 *
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.1
 * @since 1/18/2018
 */

class PhotoEditorView : RelativeLayout {
    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var backgroundImage: BackgroundImageView
    private lateinit var brushDrawingView: BrushDrawingView
    private lateinit var imageFilterView: ImageFilterView
    private var surfaceListeners: ArrayList<SurfaceTextureListener> = ArrayList()

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            for (listener in surfaceListeners) {
                listener.onSurfaceTextureAvailable(texture, width, height)
            }
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            for (listener in surfaceListeners) {
                listener.onSurfaceTextureSizeChanged(texture, width, height)
            }
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    val source: ImageView
        get() = backgroundImage

    val brush: BrushDrawingView
        get() = brushDrawingView

    val textureView: AutoFitTextureView
        get() = autoFitTextureView

    val listeners: ArrayList<SurfaceTextureListener>
        get() = surfaceListeners

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(attrs)
    }

    @SuppressLint("Recycle")
    private fun init(attrs: AttributeSet?) {
        // Setup image attributes
        backgroundImage = BackgroundImageView(context)
        backgroundImage.id = imgSrcId
        backgroundImage.adjustViewBounds = true
        val imgSrcParam = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imgSrcParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, styleable.PhotoEditorView)
            val imgSrcDrawable = a.getDrawable(styleable.PhotoEditorView_photo_src)
            if (imgSrcDrawable != null) {
                backgroundImage.setImageDrawable(imgSrcDrawable)
            }
        }

        // Setup Camera preview view
        autoFitTextureView = AutoFitTextureView(context)
        autoFitTextureView.id = cameraPreviewId
        autoFitTextureView.visibility = View.GONE
        val cameraParam = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cameraParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        // set main listener
        autoFitTextureView.surfaceTextureListener = surfaceTextureListener

        // Setup brush view
        brushDrawingView = BrushDrawingView(context)
        brushDrawingView.visibility = View.GONE
        brushDrawingView.id = brushSrcId
        // Align brush to the size of image view
        val brushParam = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        brushParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        brushParam.addRule(RelativeLayout.ALIGN_TOP, imgSrcId)
        brushParam.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId)

        // Setup GLSurface attributes
        imageFilterView = ImageFilterView(context)
        imageFilterView.id = glFilterId
        imageFilterView.visibility = View.GONE

        // Align brush to the size of image view
        val imgFilterParam = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imgFilterParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        imgFilterParam.addRule(RelativeLayout.ALIGN_TOP, imgSrcId)
        imgFilterParam.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId)

        backgroundImage.setOnImageChangedListener(object : BackgroundImageView.OnImageChangedListener {
            override fun onBitmapLoaded(sourceBitmap: Bitmap?) {
                imageFilterView.setFilterEffect(PhotoFilter.NONE)
                imageFilterView.setSourceBitmap(sourceBitmap!!)
                Log.d(TAG, "onBitmapLoaded() called with: sourceBitmap = [$sourceBitmap]")
            }
        })

        // Add camera preview
        addView(autoFitTextureView, cameraParam)

        // Add image source
        addView(backgroundImage, imgSrcParam)

        // Add Gl FilterView
        addView(imageFilterView, imgFilterParam)

        // Add brush view
        addView(brushDrawingView, brushParam)
    }

    internal fun saveFilter(onSaveBitmap: OnSaveBitmap) {
        // check which background is currently visible: if it's
        // - imageFilterView:  a filter has been applied, process it first
        // - autoFitTextureView:  a video (camera preview or video player) is being shown, use that
        // - else: use deafult backgroundImage.bitmap
        if (imageFilterView.visibility == View.VISIBLE) {
            imageFilterView.saveBitmap(object : OnSaveBitmap {
                override fun onBitmapReady(saveBitmap: Bitmap) {
                    Log.e(TAG, "saveFilter: $saveBitmap")
                    backgroundImage.setImageBitmap(saveBitmap)
                    imageFilterView.visibility = View.GONE
                    onSaveBitmap.onBitmapReady(saveBitmap)
                }

                override fun onFailure(e: Exception) {
                    onSaveBitmap.onFailure(e)
                }
            })
        } else if (autoFitTextureView.visibility == View.VISIBLE) {
            // this saves just a snapshot of whatever is being displayed on the TextureSurface
            backgroundImage.setImageBitmap(autoFitTextureView.bitmap)
            toggleTextureView()
            onSaveBitmap.onBitmapReady(backgroundImage.bitmap!!)
        } else {
            onSaveBitmap.onBitmapReady(backgroundImage.bitmap!!)
        }
    }

    internal fun setFilterEffect(filterType: PhotoFilter) {
        imageFilterView.visibility = View.VISIBLE
        imageFilterView.setSourceBitmap(backgroundImage.bitmap!!)
        imageFilterView.setFilterEffect(filterType)
    }

    internal fun setFilterEffect(customEffect: CustomEffect) {
        imageFilterView.visibility = View.VISIBLE
        imageFilterView.setSourceBitmap(backgroundImage.bitmap!!)
        imageFilterView.setFilterEffect(customEffect)
    }

    internal fun turnTextureViewOn() {
        backgroundImage.visibility = View.GONE
        autoFitTextureView.visibility = View.VISIBLE
    }

    internal fun turnTextureViewOff() {
        backgroundImage.visibility = View.VISIBLE
        autoFitTextureView.visibility = View.GONE
    }

    internal fun toggleTextureView(): Boolean {
        backgroundImage.visibility = autoFitTextureView.visibility.also {
            autoFitTextureView.visibility = backgroundImage.visibility
        }
        return autoFitTextureView.visibility == View.VISIBLE
    }

    internal fun turnTextureAndImageViewOff() {
        backgroundImage.visibility = View.GONE
        autoFitTextureView.visibility = View.GONE
    }

    companion object {
        private val TAG = "PhotoEditorView"
        private val imgSrcId = 1
        private val brushSrcId = 2
        private val glFilterId = 3
        private val cameraPreviewId = 4
    }
}