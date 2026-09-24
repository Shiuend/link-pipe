package com.linkpipe.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareBehaviorTest {
    @Test
    fun defaultsToQuickSaveWithoutOpeningDetail() {
        val behavior = ShareBehavior()

        assertTrue(behavior.quickSave)
        assertFalse(behavior.openDetail)
    }

    @Test
    fun enablingOpenDetailDisablesQuickSave() {
        val behavior = ShareBehavior().withOpenDetail(true)

        assertTrue(behavior.openDetail)
        assertFalse(behavior.quickSave)
    }

    @Test
    fun enablingQuickSaveDisablesOpenDetail() {
        val behavior = ShareBehavior(quickSave = false, openDetail = true).withQuickSave(true)

        assertTrue(behavior.quickSave)
        assertFalse(behavior.openDetail)
    }
}
