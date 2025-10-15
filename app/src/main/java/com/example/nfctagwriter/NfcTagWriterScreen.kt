package com.example.nfctagwriter

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nfctagwriter.ui.theme.NFCTagWriterTheme

@Composable
fun NfcTagWriterScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Hello Android!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun NfcTagWriterScreenPreview() {
    NFCTagWriterTheme {
        NfcTagWriterScreen()
    }
}