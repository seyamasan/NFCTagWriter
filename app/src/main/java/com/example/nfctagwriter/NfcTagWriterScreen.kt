package com.example.nfctagwriter

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.example.nfctagwriter.ui.theme.NFCTagWriterTheme
import androidx.core.net.toUri

@Composable
fun NfcTagWriterScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val activity = (context as ComponentActivity)

    var nfcAdapter: NfcAdapter? = null

    val readerCallback = NfcAdapter.ReaderCallback { tag: Tag? ->
        if (tag == null) return@ReaderCallback

        // URIレコードを作成
        val uriRecord = NdefRecord.createUri("".toUri())
        val message = NdefMessage(arrayOf(uriRecord))

        try {
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
                if (needed > max) {
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