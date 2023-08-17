package com.shopping.app.ui.basket

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.shopping.app.R
import com.shopping.app.data.model.ProductBasket
import com.shopping.app.data.repository.basket.BasketRepositoryImpl
import com.shopping.app.ui.basket.viewmodel.BasketViewModel
import com.shopping.app.ui.basket.viewmodel.BasketViewModelFactory
import com.shopping.app.ui.basket.viewmodel.formatProductName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PurchaseActivity : AppCompatActivity() {

    private val viewModel by viewModels<BasketViewModel> {
        BasketViewModelFactory(
            BasketRepositoryImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        val button = findViewById<Button>(R.id.btnConfirm)


        button.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val pdfFile = generatePDF(
                    resources,
                    BitmapFactory.decodeResource(resources, R.drawable.company_logo),
                    viewModel.basketList,
                    "Customer Name",
                    "123456",
                    "12/12/2021"
                )

                //open the file with the above pdfFile as uri.
                if (pdfFile != null) {
                    viewModel.clearTheBasket()
                    val openPdfINtent = Intent(Intent.ACTION_VIEW)
                    openPdfINtent.setDataAndType(pdfFile, "application/pdf")
                    openPdfINtent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    finish()
                    Toast.makeText(
                        this@PurchaseActivity,
                        "Pdf generated successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
//                    startActivity(openPdfINtent)
                } else {
                    Toast.makeText(
                        this@PurchaseActivity,
                        "Error unable to generate the pdf.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }

    suspend fun generatePDF(
        resources: Resources,
        logoBitmap: Bitmap,
        basketList: List<ProductBasket>,
        customerName: String,
        invoiceNumber: String,
        date: String
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawColor(Color.WHITE)

            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 14f

            val boldPaint = Paint()
            boldPaint.color = Color.BLACK
            boldPaint.textSize = 14f
            boldPaint.isFakeBoldText = true

            val textSize = 14f
            val xPosition = 50f
            var yPosition = 50f


            val resizedLogo = Bitmap.createScaledBitmap(logoBitmap, 130, 110, false)
            canvas.drawBitmap(resizedLogo, xPosition, yPosition, paint)
            yPosition += resizedLogo.height + 20

            // Draw company details
            canvas.drawText("Your Company Name", xPosition, yPosition, paint)
            yPosition += (textSize * 1.5).toFloat()
            canvas.drawText("123 Street, City, Country", xPosition, yPosition, paint)
            yPosition += (textSize * 1.5).toFloat()
            canvas.drawText("Phone: 123-456-7890", xPosition, yPosition, paint)
            yPosition += textSize * 3

            // Draw invoice details
            canvas.drawText("Invoice Number: $invoiceNumber", xPosition, yPosition, boldPaint)
            yPosition += (textSize * 1.5).toFloat()
            canvas.drawText("Invoice Date: $date", xPosition, yPosition, paint)
            yPosition += textSize * 3

            // Draw customer details
            canvas.drawText("Bill To:", xPosition, yPosition, boldPaint)
            yPosition += (textSize * 1.5).toFloat()
            canvas.drawText(customerName, xPosition, yPosition, paint)
            yPosition += (textSize * 2.0).toFloat()


            // Draw table headers
            canvas.drawText("Product", xPosition, yPosition, boldPaint)
            canvas.drawText("Quantity", xPosition + 300, yPosition, boldPaint)
            canvas.drawText("Price", xPosition + 450, yPosition, boldPaint)
            yPosition += (textSize * 1.5).toFloat()

            for (product in basketList) {
                val formattedProductName =
                    formatProductName(product.title ?: "", 30) // Limit to 50 characters
                canvas.drawText(formattedProductName, xPosition, yPosition, paint)
                canvas.drawText("${product.piece}", xPosition + 300, yPosition, paint)
                canvas.drawText("${product.price}", xPosition + 450, yPosition, paint)
                yPosition += (textSize * 1.5).toFloat()
            }
            // Draw product basket items


            // Calculate total
            val total = basketList.sumByDouble { it.price ?: 0.0 * (it.piece ?: 0) }
            yPosition += (textSize * 1.5).toFloat()
            canvas.drawText("Total: $total", xPosition + 450, yPosition, paint)

            pdfDocument.finishPage(page)

            val pdfFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "formatted_invoice.pdf"
            )
            try {
                pdfDocument.writeTo(FileOutputStream(pdfFile))
                return@withContext pdfFile.toUri()
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext null
            }
            pdfDocument.close()
        } as Uri?
    }
}
