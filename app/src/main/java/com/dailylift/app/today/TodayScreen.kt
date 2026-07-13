package com.dailylift.app.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailylift.app.data.Exercise
import com.dailylift.app.ui.theme.AppAccent
import com.dailylift.app.ui.theme.AppBackground
import com.dailylift.app.ui.theme.AppCard
import com.dailylift.app.ui.theme.AppCardLine
import com.dailylift.app.ui.theme.AppGreen
import com.dailylift.app.ui.theme.AppTextFaint
import com.dailylift.app.ui.theme.AppTextMuted
import com.dailylift.app.ui.theme.AppTextPrimary

private const val TAP_TARGET_DP = 48
private const val COMPACT_TAP_TARGET_DP = 32
/** Weight and reps are both single short values now (e.g. "70", "12") - one narrow width for both. */
private const val NUMERIC_FIELD_WIDTH_DP = 40
private const val NAME_MAX_WIDTH_DP = 150

/**
 * Today screen: a card showing [uiState]'s viewed day, with [onNavigate] driving the day-navigation
 * arrows and the remaining callbacks wiring up Step D's editing controls (D1-D8).
 *
 * [HeaderRow] stays pinned; the exercise list/rest message below it scrolls independently so a long
 * exercise list (or the "Add exercise" button) is never clipped off the bottom of the screen.
 */
@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onNavigate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onToggleChecked: (String) -> Unit = {},
    onUpdateWeight: (String, String) -> Unit = { _, _ -> },
    onUpdateReps: (String, String) -> Unit = { _, _ -> },
    onRename: (String, String) -> Unit = { _, _ -> },
    onAddExercise: () -> Unit = {},
    onDeleteExercise: (String) -> Unit = {},
    onExerciseClick: (Exercise) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = Color.Black,
                    spotColor = Color.Black,
                )
                .clip(RoundedCornerShape(22.dp))
                .background(AppCard)
                .border(1.dp, AppCardLine, RoundedCornerShape(22.dp)),
        ) {
            HeaderRow(uiState, onNavigate)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                when (val content = uiState.content) {
                    is DayContent.Workout -> WorkoutContent(
                        content = content,
                        isToday = uiState.isToday,
                        onToggleChecked = onToggleChecked,
                        onUpdateWeight = onUpdateWeight,
                        onUpdateReps = onUpdateReps,
                        onRename = onRename,
                        onAddExercise = onAddExercise,
                        onDeleteExercise = onDeleteExercise,
                        onExerciseClick = onExerciseClick,
                    )
                    is DayContent.Rest -> RestContent(content)
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(uiState: TodayUiState, onNavigate: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = uiState.viewedWeekday.displayName(),
                    color = AppTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (uiState.isToday) {
                    Spacer(modifier = Modifier.width(6.dp))
                    TodayBadge()
                }
            }
            val focus = (uiState.content as? DayContent.Workout)?.focus
            if (focus != null) {
                Text(
                    text = focus,
                    color = AppTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        Row {
            NavButton(label = "‹", contentDescription = "Previous day", onClick = { onNavigate(-1) })
            NavButton(label = "›", contentDescription = "Next day", onClick = { onNavigate(1) })
        }
    }
}

/** Today badge background/text combo verified to clear the 4.5:1 contrast floor by `ContrastTest`. */
@Composable
private fun TodayBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppAccent.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text = "Today", color = AppTextPrimary, fontSize = 11.sp)
    }
}

@Composable
private fun NavButton(label: String, contentDescription: String, onClick: () -> Unit) {
    TapTarget(contentDescription = contentDescription, onClick = onClick) {
        Text(text = label, color = AppTextPrimary, fontSize = 18.sp)
    }
}

/**
 * A [size]x[size] tappable region (D9, default [TAP_TARGET_DP]) exposing exactly
 * [contentDescription] as its accessibility label (D10), regardless of [content]'s own semantics.
 */
@Composable
private fun TapTarget(
    contentDescription: String,
    enabled: Boolean = true,
    size: Dp = TAP_TARGET_DP.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun WorkoutContent(
    content: DayContent.Workout,
    isToday: Boolean,
    onToggleChecked: (String) -> Unit,
    onUpdateWeight: (String, String) -> Unit,
    onUpdateReps: (String, String) -> Unit,
    onRename: (String, String) -> Unit,
    onAddExercise: () -> Unit,
    onDeleteExercise: (String) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Exercise?>(null) }

    if (content.rows.isEmpty()) {
        EmptyWorkoutDay(onAddExercise = onAddExercise)
    } else {
        Text(
            text = "Do 3 sets of each exercise below",
            color = AppTextPrimary.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        ColumnHeaders()
        content.rows.forEach { row ->
            ExerciseRowView(
                row = row,
                isToday = isToday,
                onToggleChecked = onToggleChecked,
                onUpdateWeight = onUpdateWeight,
                onUpdateReps = onUpdateReps,
                onRename = onRename,
                onRequestDelete = { pendingDelete = it },
                onExerciseClick = onExerciseClick,
            )
        }
        if (isAllDone(content)) {
            AllDoneBanner()
        }
        AddExerciseButton(onClick = onAddExercise)
        Spacer(modifier = Modifier.height(4.dp))
    }

    pendingDelete?.let { exercise ->
        DeleteConfirmDialog(
            exerciseName = exercise.name,
            onConfirm = {
                onDeleteExercise(exercise.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

/** Step E, point 8: shown once [isAllDone] is true. Colors ported from `workout-widget-prototype.html`'s `.celebrate`. */
@Composable
private fun AllDoneBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppGreen.copy(alpha = 0.15f))
            .border(1.dp, AppGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nice work - you finished today's workout! 🎉",
            color = Color(0xFF8FE0A4),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** D7: confirm before [onConfirm] removes the exercise. */
@Composable
private fun DeleteConfirmDialog(exerciseName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Remove exercise?") },
        text = { Text(text = "Remove \"$exerciseName\" from this workout?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = "Remove") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "Cancel") } },
    )
}

/**
 * Mirrors [ExerciseRowView]'s line-2 [Row] structure exactly (same horizontal padding, same
 * leading [TAP_TARGET_DP] spacer, same flex/fixed-width columns in the same order, same
 * [Arrangement.spacedBy] gap) so WEIGHT/REPS actually line up under the row's editable fields,
 * regardless of exercise name length.
 */
@Composable
private fun ColumnHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Spacer(modifier = Modifier.width(TAP_TARGET_DP.dp))
        Text(
            text = "EXERCISE",
            color = AppTextFaint,
            fontSize = 9.5.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "WEIGHT",
            color = AppTextFaint,
            fontSize = 9.5.sp,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(NUMERIC_FIELD_WIDTH_DP.dp),
        )
        Text(
            text = "REPS",
            color = AppTextFaint,
            fontSize = 9.5.sp,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(NUMERIC_FIELD_WIDTH_DP.dp),
        )
        Spacer(modifier = Modifier.width(TAP_TARGET_DP.dp))
    }
}

/**
 * One exercise's editing controls. At the default (100%) font scale, renders as a single dense
 * row via [CompactExerciseRow], matching `workout-widget-prototype.html`'s one-line grid exactly.
 * Past 100% font scale, switches to [ExpandedExerciseRow]'s two-line layout (checkbox + name +
 * rename icon on line 1, weight/reps/delete on line 2) so text and tap targets still have room to
 * breathe at larger accessibility sizes, mirroring [ColumnHeaders]' column widths in both cases
 * (Decision 6).
 */
@Composable
private fun ExerciseRowView(
    row: ExerciseRow,
    isToday: Boolean,
    onToggleChecked: (String) -> Unit,
    onUpdateWeight: (String, String) -> Unit,
    onUpdateReps: (String, String) -> Unit,
    onRename: (String, String) -> Unit,
    onRequestDelete: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
) {
    val exercise = row.exercise
    var renaming by remember(exercise.id) { mutableStateOf(false) }
    var nameInput by remember(exercise.id) { mutableStateOf(exercise.name) }
    val isCompact = LocalDensity.current.fontScale <= 1f

    val onNameInputChange: (String) -> Unit = { value ->
        nameInput = value.take(Exercise.MAX_NAME_LENGTH)
        onRename(exercise.id, nameInput)
    }
    val onToggleRenaming: () -> Unit = {
        if (renaming) onRename(exercise.id, nameInput)
        renaming = !renaming
    }
    val onFinishRenaming: () -> Unit = { renaming = false }

    if (isCompact) {
        CompactExerciseRow(
            row = row,
            isToday = isToday,
            renaming = renaming,
            nameInput = nameInput,
            onNameInputChange = onNameInputChange,
            onToggleRenaming = onToggleRenaming,
            onFinishRenaming = onFinishRenaming,
            onToggleChecked = onToggleChecked,
            onUpdateWeight = onUpdateWeight,
            onUpdateReps = onUpdateReps,
            onRequestDelete = onRequestDelete,
            onExerciseClick = onExerciseClick,
        )
    } else {
        ExpandedExerciseRow(
            row = row,
            isToday = isToday,
            renaming = renaming,
            nameInput = nameInput,
            onNameInputChange = onNameInputChange,
            onToggleRenaming = onToggleRenaming,
            onFinishRenaming = onFinishRenaming,
            onToggleChecked = onToggleChecked,
            onUpdateWeight = onUpdateWeight,
            onUpdateReps = onUpdateReps,
            onRequestDelete = onRequestDelete,
            onExerciseClick = onExerciseClick,
        )
    }
}

/**
 * Default (100% font scale) layout: one dense row - checkbox, name (flexible, ellipsized), rename
 * icon, weight, reps, delete - ported directly from the prototype's single-row grid. Icon tap
 * targets are [COMPACT_TAP_TARGET_DP], not the full [TAP_TARGET_DP]: at real phone widths, a
 * 48dp checkbox + 48dp rename + 48dp delete alongside the 70dp weight and 92dp reps fields don't
 * fit next to a readable name column, so this trades some tap-target size for the density the
 * prototype relies on. The full [TAP_TARGET_DP] returns in [ExpandedExerciseRow] once the user's
 * font scale grows, where there's vertical room to spare.
 */
@Composable
private fun CompactExerciseRow(
    row: ExerciseRow,
    isToday: Boolean,
    renaming: Boolean,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onToggleRenaming: () -> Unit,
    onFinishRenaming: () -> Unit,
    onToggleChecked: (String) -> Unit,
    onUpdateWeight: (String, String) -> Unit,
    onUpdateReps: (String, String) -> Unit,
    onRequestDelete: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
) {
    val exercise = row.exercise
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TapTarget(
            contentDescription = if (row.checked) {
                "${exercise.name}, marked done"
            } else {
                "Mark ${exercise.name} done"
            },
            enabled = isToday,
            size = COMPACT_TAP_TARGET_DP.dp,
            onClick = { onToggleChecked(exercise.id) },
        ) {
            CheckIndicator(checked = row.checked)
        }

        if (renaming) {
            NameField(
                value = nameInput,
                onValueChange = onNameInputChange,
                onDone = onFinishRenaming,
                widthModifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = exercise.name,
                color = if (row.checked) AppTextFaint else AppTextPrimary.copy(alpha = 0.92f),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (row.checked) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = { onExerciseClick(exercise) }),
            )
        }

        TapTarget(
            contentDescription = "Rename ${exercise.name}",
            size = COMPACT_TAP_TARGET_DP.dp,
            onClick = onToggleRenaming,
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = AppTextFaint,
                modifier = Modifier.size(16.dp),
            )
        }

        EditableField(
            value = exercise.weight,
            onValueChange = { onUpdateWeight(exercise.id, it) },
            width = NUMERIC_FIELD_WIDTH_DP.dp,
            placeholder = "—",
            keyboardType = KeyboardType.Text,
            emptyAccessibilityLabel = weightAccessibilityLabel(exercise.weight),
        )

        EditableField(
            value = exercise.reps,
            onValueChange = { onUpdateReps(exercise.id, it) },
            width = NUMERIC_FIELD_WIDTH_DP.dp,
            placeholder = "",
            keyboardType = KeyboardType.Text,
        )

        TapTarget(
            contentDescription = "Delete ${exercise.name}",
            size = COMPACT_TAP_TARGET_DP.dp,
            onClick = { onRequestDelete(exercise) },
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = AppTextFaint,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Larger-font-scale layout (Decision 6): an explicit two-line [Column] (not a reflowing
 * [androidx.compose.foundation.layout.FlowRow]) so the structure holds at every scale above 100%,
 * including 130% (MD1): line 1 is checkbox + name + rename icon, line 2 is weight/reps/delete,
 * mirroring [ColumnHeaders]' column widths exactly so weight/reps line up under WEIGHT/REPS
 * regardless of name length. Full [TAP_TARGET_DP] tap targets throughout, since the two-line
 * structure has the vertical room a single row doesn't.
 */
@Composable
private fun ExpandedExerciseRow(
    row: ExerciseRow,
    isToday: Boolean,
    renaming: Boolean,
    nameInput: String,
    onNameInputChange: (String) -> Unit,
    onToggleRenaming: () -> Unit,
    onFinishRenaming: () -> Unit,
    onToggleChecked: (String) -> Unit,
    onUpdateWeight: (String, String) -> Unit,
    onUpdateReps: (String, String) -> Unit,
    onRequestDelete: (Exercise) -> Unit,
    onExerciseClick: (Exercise) -> Unit,
) {
    val exercise = row.exercise
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TapTarget(
                contentDescription = if (row.checked) {
                    "${exercise.name}, marked done"
                } else {
                    "Mark ${exercise.name} done"
                },
                enabled = isToday,
                onClick = { onToggleChecked(exercise.id) },
            ) {
                CheckIndicator(checked = row.checked)
            }

            if (renaming) {
                NameField(
                    value = nameInput,
                    onValueChange = onNameInputChange,
                    onDone = onFinishRenaming,
                )
            } else {
                Text(
                    text = exercise.name,
                    color = if (row.checked) AppTextFaint else AppTextPrimary.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    textDecoration = if (row.checked) TextDecoration.LineThrough else null,
                    modifier = Modifier
                        .widthIn(max = NAME_MAX_WIDTH_DP.dp)
                        .clickable(onClick = { onExerciseClick(exercise) }),
                )
            }

            TapTarget(
                contentDescription = "Rename ${exercise.name}",
                onClick = onToggleRenaming,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = AppTextFaint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(modifier = Modifier.width(TAP_TARGET_DP.dp))
            Spacer(modifier = Modifier.weight(1f))

            EditableField(
                value = exercise.weight,
                onValueChange = { onUpdateWeight(exercise.id, it) },
                width = NUMERIC_FIELD_WIDTH_DP.dp,
                placeholder = "—",
                keyboardType = KeyboardType.Text,
                emptyAccessibilityLabel = weightAccessibilityLabel(exercise.weight),
            )

            EditableField(
                value = exercise.reps,
                onValueChange = { onUpdateReps(exercise.id, it) },
                width = NUMERIC_FIELD_WIDTH_DP.dp,
                placeholder = "",
                keyboardType = KeyboardType.Text,
            )

            TapTarget(
                contentDescription = "Delete ${exercise.name}",
                onClick = { onRequestDelete(exercise) },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AppTextFaint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Inline rename input (D4), capped to [Exercise.MAX_NAME_LENGTH] by [onValueChange]. */
@Composable
private fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    widthModifier: Modifier = Modifier.widthIn(max = NAME_MAX_WIDTH_DP.dp),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = AppTextPrimary, fontSize = 14.sp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        cursorBrush = SolidColor(AppAccent),
        modifier = widthModifier
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

/**
 * A small editable text field for weight/reps. When [value] is empty and [emptyAccessibilityLabel]
 * is set, it's exposed as the field's spoken label (D11) instead of the literal [placeholder].
 */
@Composable
private fun EditableField(
    value: String,
    onValueChange: (String) -> Unit,
    width: Dp,
    placeholder: String,
    keyboardType: KeyboardType,
    emptyAccessibilityLabel: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .width(width)
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = if (isFocused) AppAccent else Color.Transparent,
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .then(
                if (value.isEmpty() && emptyAccessibilityLabel != null) {
                    Modifier.semantics { contentDescription = emptyAccessibilityLabel }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = AppTextMuted, fontSize = 13.sp, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
            cursorBrush = SolidColor(AppAccent),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = AppTextFaint,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/** D8: a workout day with zero exercises shows "Rest Day" with "Add exercise" still available. */
@Composable
private fun EmptyWorkoutDay(onAddExercise: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🌙", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Rest Day", color = AppTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No exercises here yet.",
            color = AppTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        AddExerciseButton(onClick = onAddExercise)
    }
}

/** D5: appends a new placeholder exercise to the viewed day. */
@Composable
private fun AddExerciseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .heightIn(min = TAP_TARGET_DP.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "+ Add exercise", color = AppAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CheckIndicator(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) AppGreen else Color.Transparent)
            .border(1.5.dp, if (checked) AppGreen else Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(text = "✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RestContent(content: DayContent.Rest) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🌙", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Rest & recover", color = AppTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No workout today.\nLet your muscles rebuild.",
            color = AppTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Next up · Monday · ${content.nextUpFocus}",
                color = AppTextFaint,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val preview = content.nextUpExercises.joinToString(", ") +
                if (content.hasMore) " + more" else ""
            Text(
                text = preview,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
