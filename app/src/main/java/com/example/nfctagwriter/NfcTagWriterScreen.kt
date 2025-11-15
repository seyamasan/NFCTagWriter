package com.example.nfctagwriter

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.example.nfctagwriter.ui.theme.NFCTagWriterTheme
import androidx.core.net.toUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcTagWriterScreen(modifier: Modifier = Modifier) {

    var inputText by rememberSaveable { mutableStateOf("") }
    var isWritingUrlToTag by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = (context as ComponentActivity)

    var nfcAdapter: NfcAdapter? = null

    val readerCallback = NfcAdapter.ReaderCallback { tag: Tag? ->
        if (tag == null || !isWritingUrlToTag) return@ReaderCallback

        try {
            // URIレコードを作成
            val uriRecord = NdefRecord.createUri(inputText.toUri())
            val message = NdefMessage(arrayOf(uriRecord))

            // 読み込んだタグのNDEFを取得
            val ndef = Ndef.get(tag)

            if (ndef != null) {
                ndef.connect()

                // 書き込み可能かチェック
                if (!ndef.isWritable) {
                    ndef.close()

                    // UIスレッドで表示する
                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "タグが書き込み禁止です。",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@ReaderCallback
                }

                // 容量チェック
                val needed = message.toByteArray().size
                val max = ndef.maxSize
                if (max < needed) {
                    ndef.close()

                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "容量不足: 必要 $needed / 上限 $max バイト",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    return@ReaderCallback
                }

                // 書き込み
                ndef.writeNdefMessage(message)
                ndef.close()

                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "URLを書き込みました。",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "書き込みできませんでした。",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(
                    activity,
                    "書き込みエラー: ${e.message ?: ""}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } finally {
            // 書き込み処理終了
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    isWritingUrlToTag = false
                }
            }
        }
    }

    ObserveLifecycleEvent { event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                // デフォルトの NFC アダプター(コントローラー)を取得
                // NFCに関する処理をコントロールできる
                nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

                if (nfcAdapter == null) {
                    Toast.makeText(
                        activity,
                        "この端末はNFC非対応です。",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Lifecycle.Event.ON_RESUME -> {
                if (nfcAdapter == null) { return@ObserveLifecycleEvent }

                // NTAG215を使う予定なので、Type A のフラグを使う
                val flag = NfcAdapter.FLAG_READER_NFC_A

                // NFC アダプターをリーダーモードに制限する
                nfcAdapter?.enableReaderMode(activity, readerCallback, flag, null)
            }
            Lifecycle.Event.ON_PAUSE -> {
                if (nfcAdapter == null) { return@ObserveLifecycleEvent }

                // リーダーモードを元に戻す
                nfcAdapter?.disableReaderMode(activity)
            }
            else -> {}
        }
    }

    if (isWritingUrlToTag) {
        // 書き込み時のボトムシート
        ModalBottomSheet(
            onDismissRequest = { isWritingUrlToTag = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    modifier = Modifier.size(48.dp),
                    contentDescription = "Nfc icon"
                )

                Text("↑", fontWeight = FontWeight.Bold)

                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    modifier = Modifier.size(48.dp),
                    contentDescription = "PhoneAndroid icon"
                )

                Text(
                    "スマホをかざす！",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "書き込みたいURLを入力しよう！",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                singleLine = true,
                placeholder = { Text(text = "URLを入力")}
            )

            Button(onClick = { isWritingUrlToTag = true }) {
                Text("書き込む")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NfcTagWriterScreenPreview() {
    NFCTagWriterTheme {
        NfcTagWriterScreen()
    }
}