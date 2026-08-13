package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.Category
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput

@Composable
fun AdminCategoriesScreen(
    categories: List<Category>,
    onAddCategory: (name: String, icon: String, isLongProject: Boolean, duration: String?, upfrontFee: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("Hammer") }
    var isLongProject by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf("1 Month") }
    var upfrontFee by remember { mutableStateOf("500") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Service Categories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text("Add New Category", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Category Name", value = name, onValueChange = { name = it }, placeholder = "e.g. Gardener")
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Icon Name (Lucide)", value = icon, onValueChange = { icon = it }, placeholder = "e.g. Hammer, Zap, Droplets")

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isLongProject, onCheckedChange = { isLongProject = it })
                Text("Long-term Project (requires upfront fee)", style = MaterialTheme.typography.bodySmall)
            }

            if (isLongProject) {
                Spacer(modifier = Modifier.height(8.dp))
                ObragoInput(label = "Duration", value = duration, onValueChange = { duration = it }, placeholder = "e.g. 1 Month")
                Spacer(modifier = Modifier.height(10.dp))
                ObragoInput(label = "Upfront Fee", value = upfrontFee, onValueChange = { upfrontFee = it }, keyboardType = KeyboardType.Number)
            }

            Spacer(modifier = Modifier.height(14.dp))
            ObragoButton(text = "Add Category", enabled = name.isNotBlank()) {
                onAddCategory(name, icon, isLongProject, if (isLongProject) duration else null, if (isLongProject) upfrontFee.toDoubleOrNull() else null)
                name = ""
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Existing Categories (${categories.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(10.dp))

        categories.forEach { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(if (cat.isLongProject) "Long Project • ${cat.duration}" else "Standard", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
