
package com.automattic.photoeditor.camera.interfaces

import androidx.camera.core.FlashMode
import com.automattic.photoeditor.camera.interfaces.FlashIndicatorState.AUTO
import com.automattic.photoeditor.camera.interfaces.FlashIndicatorState.OFF
import com.automattic.photoeditor.camera.interfaces.FlashIndicatorState.ON

interface CameraFlashStateHandler {
    fun advanceFlashState()
    fun setFlashState(flashIndicatorState: FlashIndicatorState)
    fun currentFlashState(): FlashIndicatorState
}

interface CameraFlashSupportQuery {
    fun isFlashAvailable(): Boolean
}

enum class FlashIndicatorState {
    ON, OFF, AUTO
}

class CameraFlashStateKeeper : CameraFlashStateHandler {
    private var flashState = OFF
    override fun advanceFlashState() {
        flashState = when (flashState) {
            AUTO -> ON
            ON -> OFF
            OFF -> AUTO
        }
    }

    override fun setFlashState(flashIndicatorState: FlashIndicatorState) {
        flashState = flashIndicatorState
    }

    override fun currentFlashState(): FlashIndicatorState {
        return flashState
    }
}

// helper method to get CameraX flash mode from CameraFlashStateHandler.FlashIndicatorState enum
fun cameraXflashModeFromPortkeyFlashState(flashIndicatorState: FlashIndicatorState): FlashMode {
    return when (flashIndicatorState) {
        AUTO -> FlashMode.AUTO
        ON -> FlashMode.ON
        OFF -> FlashMode.OFF
    }
}

// helper method to get Camera2 flash mode from CameraFlashStateHandler.FlashIndicatorState enum
fun camera2flashModeFromPortkeyFlashState(flashIndicatorState: FlashIndicatorState): FlashMode {
    return when (flashIndicatorState) {
        AUTO -> FlashMode.AUTO
        ON -> FlashMode.ON
        OFF -> FlashMode.OFF
    }
}
