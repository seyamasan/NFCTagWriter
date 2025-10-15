package com.example.nfctagwriter

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.example.nfctagwriter.ui.theme.NFCTagWriterTheme

@Composable
fun NfcTagWriterScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val activity = (context as ComponentActivity)

    var nfcAdapter: NfcAdapter? = null

    val readerCallback = NfcAdapter.ReaderCallback { tag: Tag? ->
        if (tag == null) return@ReaderCallback

        // ここで書き込み処理を書く予定
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