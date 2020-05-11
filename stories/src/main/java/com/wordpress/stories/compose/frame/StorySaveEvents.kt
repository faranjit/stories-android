package com.wordpress.stories.compose.frame

import android.os.Parcelable
import com.wordpress.stories.compose.frame.StorySaveEvents.SaveResultReason.SaveSuccess
import com.wordpress.stories.compose.story.StoryIndex
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

class StorySaveEvents {
    @Parcelize
    data class StorySaveResult(
        var storyIndex: StoryIndex = 0,
        val frameSaveResult: MutableList<FrameSaveResult> = mutableListOf()
    ) : Parcelable {
        fun isSuccess(): Boolean {
            return frameSaveResult.all { it.resultReason == SaveSuccess }
        }
    }
    @Parcelize
    data class FrameSaveResult(val frameIndex: FrameIndex, val resultReason: SaveResultReason) : Parcelable

    sealed class SaveResultReason : Parcelable {
        @Parcelize
        object SaveSuccess : SaveResultReason()

        @Parcelize
        data class SaveError(
            var reason: String? = null
        ) : SaveResultReason()
    }

    data class StorySaveProcessStart(
        var storyIndex: StoryIndex
    ) : Serializable
}
