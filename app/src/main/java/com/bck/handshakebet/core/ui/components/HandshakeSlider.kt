package com.bck.handshakebet.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bck.handshakebet.ui.theme.HandshakeBetTheme
import kotlin.math.roundToInt

/**
 * Branded swipe-to-confirm control used for high-stakes bet actions.
 *
 * The user drags the [Gavel][Icons.Default.Gavel] thumb from the left edge
 * towards the right. When the thumb crosses 85 % of the track width the action
 * fires automatically via [onConfirmed]; if released before the threshold the
 * thumb springs back to the start position.
 *
 * This deliberate gesture prevents accidental taps on irreversible actions
 * (accept, reject, complete) while reinforcing the HandshakeBet brand identity.
 *
 * The component is intentionally stateless — the caller decides when to reset
 * or disable it (e.g. after a network request completes).
 *
 * @param label      Instructional text shown centred inside the track, e.g.
 *                   "Slide to accept". Fades as the thumb advances.
 * @param onConfirmed Callback invoked once when the thumb crosses the threshold.
 *                   Will not fire again until [enabled] toggles back to `true`.
 * @param modifier   Optional modifier applied to the outer [Box] container.
 * @param enabled    When `false` the thumb is non-interactive and visually muted.
 * @param containerColor Background colour of the slider track.
 * @param thumbColor     Fill colour of the draggable thumb.
 */
@Composable
fun HandshakeSlider(
    label: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    thumbColor: Color = MaterialTheme.colorScheme.primary
) {
    val thumbSize = 52.dp
    val trackHeight = 60.dp
    val trackPadding = 4.dp
    val confirmThreshold = 0.85f

    var rawFraction by remember { mutableFloatStateOf(0f) }
    var confirmed by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    // Animate to full width on confirm, spring back if released early.
    val animatedFraction by animateFloatAsState(
        targetValue = when {
            confirmed       -> 1f
            else            -> rawFraction
        },
        animationSpec = if (confirmed) {
            tween(durationMillis = 120)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        finishedListener = { fraction ->
            if (fraction >= 1f && confirmed) onConfirmed()
        },
        label = "handshakeSliderFraction"
    )

    // Label fades out as the thumb covers the first half of the track.
    val labelAlpha by animateFloatAsState(
        targetValue = (1f - animatedFraction * 2f).coerceIn(0f, 1f),
        label = "handshakeSliderLabelAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .background(
                if (enabled) containerColor
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
    ) {
        // ── Label ─────────────────────────────────────────────────────────────
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = (if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                     else MaterialTheme.colorScheme.onSurface)
                .copy(alpha = labelAlpha),
            modifier = Modifier.align(Alignment.Center)
        )

        // ── Draggable thumb ───────────────────────────────────────────────────
        val thumbSizePx = with(density) { thumbSize.toPx() }
        val paddingPx   = with(density) { trackPadding.toPx() }
        val maxOffsetPx = (trackWidthPx - thumbSizePx - paddingPx * 2f).coerceAtLeast(0f)

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(trackPadding)
                .size(thumbSize)
                .offset { IntOffset((animatedFraction * maxOffsetPx).roundToInt(), 0) }
                .clip(CircleShape)
                .background(
                    if (enabled) thumbColor
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                .draggable(
                    enabled = enabled && !confirmed,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        if (maxOffsetPx > 0f) {
                            rawFraction = (rawFraction + delta / maxOffsetPx).coerceIn(0f, 1f)
                            if (rawFraction >= confirmThreshold && !confirmed) {
                                confirmed = true
                            }
                        }
                    },
                    onDragStopped = {
                        if (!confirmed) rawFraction = 0f
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onPrimary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HandshakeSliderPreview() {
    HandshakeBetTheme {
        HandshakeSlider(
            label = "Slide to accept",
            onConfirmed = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HandshakeSliderDisabledPreview() {
    HandshakeBetTheme {
        HandshakeSlider(
            label = "Slide to accept",
            onConfirmed = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
