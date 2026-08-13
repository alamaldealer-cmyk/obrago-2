package com.obrago.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.obrago.app.data.model.AdminSettings
import com.obrago.app.ui.common.ObragoButton
import com.obrago.app.ui.common.ObragoInput

@Composable
fun AdminSettingsScreen(
    settings: AdminSettings,
    onSave: (AdminSettings) -> Unit
) {
    var commissionRate by remember(settings) { mutableStateOf(settings.commissionRate.toString()) }
    var bankName by remember(settings) { mutableStateOf(settings.bankName) }
    var accountTitle by remember(settings) { mutableStateOf(settings.accountTitle) }
    var accountNumber by remember(settings) { mutableStateOf(settings.accountNumber) }
    var easypaisa by remember(settings) { mutableStateOf(settings.easypaisaNumber) }
    var jazzcash by remember(settings) { mutableStateOf(settings.jazzcashNumber) }
    var coinPricePkr by remember(settings) { mutableStateOf(settings.coinPricePkr.toString()) }
    var minTopupCoins by remember(settings) { mutableStateOf(settings.minTopupCoins.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Platform Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Commission & Wallet") {
            ObragoInput(label = "Commission Rate (%)", value = commissionRate, onValueChange = { commissionRate = it }, keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Coin Price (PKR per coin)", value = coinPricePkr, onValueChange = { coinPricePkr = it }, keyboardType = KeyboardType.Number)
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Minimum Top-up (coins)", value = minTopupCoins, onValueChange = { minTopupCoins = it }, keyboardType = KeyboardType.Number)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Bank Transfer Details") {
            ObragoInput(label = "Bank Name", value = bankName, onValueChange = { bankName = it })
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Account Title", value = accountTitle, onValueChange = { accountTitle = it })
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "Account Number / IBAN", value = accountNumber, onValueChange = { accountNumber = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard(title = "Mobile Wallets") {
            ObragoInput(label = "Easypaisa Number", value = easypaisa, onValueChange = { easypaisa = it }, keyboardType = KeyboardType.Phone)
            Spacer(modifier = Modifier.height(10.dp))
            ObragoInput(label = "JazzCash Number", value = jazzcash, onValueChange = { jazzcash = it }, keyboardType = KeyboardType.Phone)
        }

        Spacer(modifier = Modifier.height(20.dp))
        ObragoButton(text = "Save Settings") {
            onSave(
                AdminSettings(
                    commissionRate = commissionRate.toDoubleOrNull() ?: settings.commissionRate,
                    bankName = bankName,
                    accountTitle = accountTitle,
                    accountNumber = accountNumber,
                    easypaisaNumber = easypaisa,
                    jazzcashNumber = jazzcash,
                    coinPricePkr = coinPricePkr.toDoubleOrNull() ?: settings.coinPricePkr,
                    minTopupCoins = minTopupCoins.toLongOrNull() ?: settings.minTopupCoins
                )
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
