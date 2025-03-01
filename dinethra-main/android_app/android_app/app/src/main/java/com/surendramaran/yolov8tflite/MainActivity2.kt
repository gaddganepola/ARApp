package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity2 : AppCompatActivity() {

    companion object {
        // Initialize Filament's native libraries.
        init {
            Utils.init()
        }
        private const val TAG = "MainActivity2"
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var modelViewer: ModelViewer
    private lateinit var choreographer: Choreographer

    // Frame callback to continuously render the scene.
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(frameTimeNanos)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the activity_main2.xml layout
        setContentView(R.layout.activity_main2)

        choreographer = Choreographer.getInstance()

        val container = findViewById<FrameLayout>(R.id.filament_container)
        surfaceView = SurfaceView(this)
        container.addView(surfaceView)

        modelViewer = ModelViewer(surfaceView)
        surfaceView.setOnTouchListener(modelViewer)

        // Get the detected object's name from the Intent extras.
        val rawObjectName = intent.getStringExtra("objectName")
        Log.d(TAG, "Received objectName: '$rawObjectName'")
        loadModelForObject(rawObjectName)
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }

    /**
     * Loads a 3D model based on the provided object name.
     */
    private fun loadModelForObject(objectName: String?) {
        if (objectName == null) {
            Log.e(TAG, "objectName is null. Loading default model.")
            loadGlb("DamagedHelmet")
            return
        }
        // Remove leading/trailing whitespace, convert to lowercase, and remove all inner whitespace.
        val cleaned = objectName.trim().lowercase().replace("\\s+".toRegex(), "")
        Log.d(TAG, "Cleaned object name: '$cleaned' (original: '$objectName')")
        when (cleaned) {
            "sandakadapahana" -> loadGlb("sandakadapahana")
            "muragala"         -> loadGlb("muragala")
            "korawakgala"      -> loadGlb("korawakgala")
            "wamanarupa"       -> loadGlb("wamanarupa")
            else -> {
                Log.d(TAG, "No matching case for '$cleaned'. Loading default model.")
                loadGlb("DamagedHelmet")
            }
        }
    }

    /**
     * Loads a glTF binary model (.glb) from the assets folder.
     */
    private fun loadGlb(name: String) {
        val buffer = readAsset("models/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }

    /**
     * Reads an asset file into a ByteBuffer.
     */
    private fun readAsset(assetName: String): ByteBuffer {
        try {
            val input = assets.open(assetName)
            val bytes = ByteArray(input.available())
            input.read(bytes)
            input.close()
            return ByteBuffer.wrap(bytes)
        } catch (e: IOException) {
            throw RuntimeException("Error reading asset $assetName", e)
        }
    }
}
