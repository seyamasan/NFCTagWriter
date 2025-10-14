package com.example.nfctagwriter

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nfctagwriter.ui.theme.NFCTagWriterTheme

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null

    private val readerCallback = NfcAdapter.ReaderCallback { tag: Tag? ->
        if (tag == null) return@ReaderCallback

        // ここで書き込み処理を書く予定
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // デフォルトの NFC アダプター(コントローラー)を取得
        // NFCに関する処理をコントロールできる
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            val toast = Toast.makeText(this, "この端末はNFC非対応です。", Toast.LENGTH_SHORT)
            toast.show()
        }

        enableEdgeToEdge()
        setContent {
            NFCTagWriterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()

        disableReaderMode()
    }

    private fun enableReaderMode() {
        val adapter = nfcAdapter ?: return

        // NTAG215を使う予定なので、Type A のフラグを使う
        val flag = NfcAdapter.FLAG_READER_NFC_A

        // NFC アダプターをリーダーモードに制限する
        adapter.enableReaderMode(this, readerCallback, flag, null)
    }

    private fun disableReaderMode() {
        val adapter = nfcAdapter ?: return

        // NFC アダプターを元に戻す
        adapter.disableReaderMode(this)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NFCTagWriterTheme {
        Greeting("Android")
    }
}