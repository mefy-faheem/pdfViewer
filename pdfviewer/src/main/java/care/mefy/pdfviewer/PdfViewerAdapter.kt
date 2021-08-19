package care.mefy.pdfviewer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PdfViewerAdapter(private val pdfPages: ArrayList<Bitmap>) :
    RecyclerView.Adapter<PdfViewerAdapter.ViewHolder>() {

//    internal lateinit var currentHolder: ViewHolder

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: TouchImageView = itemView.findViewById(R.id.imgView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_pdf_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        currentHolder = holder
        holder.imageView.setImageBitmap(pdfPages[position])
    }

    override fun getItemCount(): Int {
        return pdfPages.size
    }
}