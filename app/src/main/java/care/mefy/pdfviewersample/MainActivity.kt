package care.mefy.pdfviewersample

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import care.mefy.pdfviewer.PDFViewer


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//  Show pdf dialog in the app
        val filePathA = "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/fileA.pdf"
        PDFViewer.Builder(this, filePathA).showDialog(true).build().create()


//  Get only pdf pages as Bitmap list
        val filePathB = "${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/fileB.pdf"
        PDFViewer.Builder(this, filePathB).setReceivePagesListener(object : PDFViewer.OnPdfPagesReceiveListener{
            override fun onReceive(pdfPages: List<Bitmap>) {
                Log.e("PDF pages count", "${pdfPages.size}")
            }
        }).build().create()
    }
}