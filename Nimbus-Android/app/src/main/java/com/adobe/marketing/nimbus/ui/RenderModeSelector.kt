package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RenderModeSelector(mode: RenderMode, onModeSelected: (RenderMode) -> Unit) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        RenderMode.entries.forEachIndexed { index, entry ->
            SegmentedButton(
                selected = mode == entry,
                onClick = { onModeSelected(entry) },
                shape = SegmentedButtonDefaults.itemShape(index,
                    RenderMode.entries.size)
            ) {
                Text(entry.label)
            }
        }
    }
}