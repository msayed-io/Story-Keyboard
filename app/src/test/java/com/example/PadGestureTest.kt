package com.example

import com.example.ui.screens.DRAG_HOLD_MS
import com.example.ui.screens.classifyPadGesture
import com.example.ui.screens.isRightClickCorner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The mouse pad has no buttons any more: the corners are clicks and a resting
 * finger becomes a drag. These tests pin that contract, including the cases the
 * writer can produce by accident (a swipe that starts in a corner, or a second
 * finger arriving mid-press).
 */
class PadGestureTest {

    @Test
    fun `a quick tap is a left click`() {
        val outcome = classifyPadGesture(travelled = false, dragging = false, multiTouch = false, rightCorner = false)
        assertEquals("left", outcome.clickButton)
        assertFalse(outcome.releaseButton)
    }

    @Test
    fun `a quick tap in the bottom-right corner is a right click`() {
        val outcome = classifyPadGesture(travelled = false, dragging = false, multiTouch = false, rightCorner = true)
        assertEquals("right", outcome.clickButton)
        assertFalse(outcome.releaseButton)
    }

    @Test
    fun `a quick tap in the bottom-left corner is a left click`() {
        assertFalse(isRightClickCorner(10f, 900f, 1000f, 1000f))
        val outcome = classifyPadGesture(travelled = false, dragging = false, multiTouch = false, rightCorner = false)
        assertEquals("left", outcome.clickButton)
    }

    @Test
    fun `swiping never clicks, even when it starts in the right corner`() {
        val outcome = classifyPadGesture(travelled = true, dragging = false, multiTouch = false, rightCorner = true)
        assertNull(outcome.clickButton)
        assertFalse(outcome.releaseButton)
    }

    @Test
    fun `resting then travelling is a drag and releases the button`() {
        val outcome = classifyPadGesture(travelled = true, dragging = true, multiTouch = false, rightCorner = false)
        assertTrue(outcome.releaseButton)
        assertNull(outcome.clickButton)
    }

    @Test
    fun `a slow tap still clicks and still releases the grabbed button`() {
        val outcome = classifyPadGesture(travelled = false, dragging = true, multiTouch = false, rightCorner = false)
        assertTrue(outcome.releaseButton)
        assertEquals("left", outcome.clickButton)
    }

    @Test
    fun `a second finger cancels the click but never leaves the button held`() {
        val cancel = classifyPadGesture(travelled = false, dragging = false, multiTouch = true, rightCorner = false)
        assertNull(cancel.clickButton)

        val grabbedThenScrolled =
            classifyPadGesture(travelled = false, dragging = true, multiTouch = true, rightCorner = false)
        assertTrue(grabbedThenScrolled.releaseButton)
        assertNull(grabbedThenScrolled.clickButton)
    }

    @Test
    fun `only the bottom-right corner is the right-click zone`() {
        val width = 1000f
        val height = 800f
        assertTrue(isRightClickCorner(780f, 620f, width, height))
        assertTrue(isRightClickCorner(1000f, 800f, width, height))
        assertFalse(isRightClickCorner(500f, 620f, width, height))
        assertFalse(isRightClickCorner(780f, 300f, width, height))
    }

    @Test
    fun `the drag hold window is short enough to feel instant and long enough to mean intent`() {
        assertTrue(DRAG_HOLD_MS in 220L..400L)
    }
}
