package com.surendramaran.yolov8tflite

import android.os.Bundle
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
        // Use the activity_main2.xml layout (ensure it has a container with id "filament_container")
        setContentView(R.layout.activity_main2)

        // Initialize the Choreographer.
        choreographer = Choreographer.getInstance()

        // Get the container from the layout and create a SurfaceView.
        val container = findViewById<FrameLayout>(R.id.filament_container)
        surfaceView = SurfaceView(this)
        container.addView(surfaceView)

        // Initialize ModelViewer with the SurfaceView.
        modelViewer = ModelViewer(surfaceView)

        // (Optional) Set a touch listener for interaction.
        surfaceView.setOnTouchListener(modelViewer)

        // Load your 3D model from assets.
        loadGlb("DamagedHelmet") // Ensure you have assets/models/DamagedHelmet.glb
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
     * Loads a glTF binary model (.glb) from the assets folder.
     */
    private fun loadGlb(name: String) {
        val buffer = readAsset("models/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        // Adjust the model to fit within a unit cube.
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
