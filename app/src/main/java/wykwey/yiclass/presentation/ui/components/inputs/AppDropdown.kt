package wykwey.yiclass.presentation.ui.components.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wykwey.yiclass.presentation.theme.LocalAppColors

data class AppDropdownItem<T>(val value: T, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppDropdown(
    value: T?,
    items: List<AppDropdownItem<T>>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "请选择",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalAppColors.current
    val shape = RoundedCornerShape(8.dp)
    val selectedLabel = items.firstOrNull { it.value == value }?.label ?: hint

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .background(if (enabled) colors.surfaceCard else colors.surfaceTint, shape)
                .border(1.dp, if (expanded) colors.textPrimary else colors.divider, shape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = if (enabled) colors.textSecondary else colors.textDisabled,
                modifier = Modifier.size(20.dp)
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.surfaceCard,
            shape = shape,
            border = BorderStroke(1.dp, colors.divider)
        ) {
            items.forEach { item ->
                val isSelected = item.value == value
                DropdownMenuItem(
                    text = {
                        Text(
                            item.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) colors.textPrimary else colors.textSecondary
                        )
                    },
                    onClick = {
                        expanded = false
                        onValueChange(item.value)
                    },
                    modifier = Modifier.background(if (isSelected) colors.surfaceTint else colors.surfaceCard)
                )
            }
        }
    }
}
