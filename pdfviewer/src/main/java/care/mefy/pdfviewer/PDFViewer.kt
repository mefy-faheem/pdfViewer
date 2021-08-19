package care.mefy.pdfviewer

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlin.concurrent.thread

class PDFViewer private constructor(builder: Builder) {

    private val context: Context = builder.context
    private val filePath: String = builder.filePath
    private val show = builder.getShow()
    private val pageReceiveListener = builder.getReceivePagesListener()

    private var progressDialog: Dialog = Dialog(this.context)

    init {
//        val binding = LayoutProgressDialogBinding.inflate(LayoutInflater.from(this.context))

        val layout = LinearLayout(context)
        layout.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        layout.gravity = Gravity.CENTER
        layout.isClickable = true
        layout.isFocusable = true

        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleLarge)
        val paramsProgressBar = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        paramsProgressBar.gravity = Gravity.CENTER
        progressBar.layoutParams = paramsProgressBar

        layout.addView(progressBar)

//        progressDialog.setContentView(R.layout.layout_progress_dialog)
        progressDialog.setContentView(layout)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
        progressDialog.setCancelable(false)
    }

    class Builder(val context: Context, val filePath: String) {

        private var show: Boolean = false
        private var pageReceiveListener: OnPdfPagesReceiveListener? = null


        fun setReceivePagesListener(onPdfPagesReceiveListener: OnPdfPagesReceiveListener): Builder {
            this.pageReceiveListener = onPdfPagesReceiveListener
            return this
        }

        internal fun getReceivePagesListener(): OnPdfPagesReceiveListener? {
            return this.pageReceiveListener
        }

        fun showDialog(show: Boolean): Builder {
            this.show = show
            return this
        }

        internal fun getShow(): Boolean {
            return show
        }

        fun build(): PDFViewer {
//            renderPDF(context, this.filePath)
            return PDFViewer(this)
        }
    }

    fun create() {
        val dialog = Dialog(context, android.R.style.Theme_Material_Dialog)
//        val binding = DialogPdfViewerBinding.inflate(LayoutInflater.from(context))

        val parentLayout = LinearLayout(context)
        parentLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        parentLayout.orientation = LinearLayout.VERTICAL

        val toolbar = Toolbar(context)
        toolbar.navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back)
        val toolbarParams = Toolbar.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        toolbar.layoutParams = toolbarParams
        parentLayout.addView(toolbar)

        val rvPDF = RecyclerView(context)
        rvPDF.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        rvPDF.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        parentLayout.addView(rvPDF)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(parentLayout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
        dialog.setCancelable(false)

//        val toolbar = dialog.findViewById<Toolbar>(R.id.toolbar)
//        val rvPDF = dialog.findViewById<RecyclerView>(R.id.rvPDF)

        val pdfPages: ArrayList<Bitmap> = arrayListOf()
        val file = File(filePath)
        val documentName: String?

        if (file.exists()) {
            documentName = file.name

            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            if (fileDescriptor != null) {
                val pdfRenderer = PdfRenderer(fileDescriptor)

                if (pdfRenderer.pageCount > 0) {

                    thread {
                        if (show) {
                            Handler(Looper.getMainLooper()).post {
                                (context as Activity).runOnUiThread {
                                    progressDialog.show()
                                }
                            }
                        }
                        for (i in 0 until pdfRenderer.pageCount) {
//                            Log.e(javaClass.simpleName, "Current page ---> $i")
                            val currentPage = pdfRenderer.openPage(i)
                            val bitmap = Bitmap.createBitmap(
                                currentPage.width,
                                currentPage.height,
                                Bitmap.Config.ARGB_8888
                            )
                            currentPage.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            pdfPages.add(bitmap)
                            currentPage.close()
                        }
                        pdfRenderer.close()
                        fileDescriptor.close()

                        if (pageReceiveListener != null) {
                            pageReceiveListener.onReceive(pdfPages)
                        }
                        if (show) {
                            Handler(Looper.getMainLooper()).post {
                                (context as Activity).runOnUiThread {
                                    if (progressDialog.isShowing) {
                                        progressDialog.dismiss()
                                    }

//                                var currentMode = EVENTS.DRAG_LEAVE
                                    toolbar.setNavigationOnClickListener { dialog.dismiss() }
                                    toolbar.title = documentName
                                    val adapter = PdfViewerAdapter(pdfPages)
                                    rvPDF.adapter = adapter

//                                binding.rvPDF.addOnItemTouchListener(object :
//                                    SimpleOnItemTouchListener() {
//
//                                    override fun onInterceptTouchEvent(
//                                        rv: RecyclerView,
//                                        e: MotionEvent
//                                    ): Boolean {
//
//                                        Log.e("Current event", " ---------> ${e.action}")
//
//                                        if (e.action == 261 || e.action == 5) {
//                                            Log.e("Recyclerview state", "Start dragging")
//                                            currentMode = EVENTS.DRAG
//                                        }
//
//                                        if (e.action == 262 || e.action == 6) {
//                                            Log.e("RecyclerView state", "Stop dragging")
//                                            currentMode = EVENTS.DRAG_LEAVE
//                                        }
//
//                                        return super.onInterceptTouchEvent(rv, e)
//                                    }
//                                })

                                    dialog.show()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            AlertDialog.Builder(context)
                .setMessage("File not found in the given path.")
                .setPositiveButton("OK") { d, _ ->
                    d.dismiss()
                }.show()
        }
    }

    interface OnPdfPagesReceiveListener {
        fun onReceive(pdfPages: List<Bitmap>)
    }
}