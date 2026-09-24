package com.linkpipe.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleSignInRequestTest {
    @Test
    fun explicitGoogleSignInButtonUsesSignInWithGoogleOption() {
        val request = buildGoogleSignInRequest("web-client-id")

        assertEquals(1, request.credentialOptions.size)
        assertTrue(request.credentialOptions.single() is GetSignInWithGoogleOption)
    }
}
