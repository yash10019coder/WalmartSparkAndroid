package com.shopping.app.ui.basket.viewmodel


import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import com.shopping.app.R
import com.shopping.app.data.model.DataState
import com.shopping.app.data.model.ProductBasket
import com.shopping.app.data.repository.basket.BasketRepository
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.jar.Manifest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument

import java.io.File
import java.io.IOException


//fun generatePDF(basketList: List<ProductBasket>) {
//    val pdfDocument = PdfDocument()
//
//    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
//    val page = pdfDocument.startPage(pageInfo)
//    val canvas = page.canvas
//
//    canvas.drawColor(Color.WHITE)
//
//    val paint = Paint()
//    paint.color = Color.BLACK
//
//    val textSize = 12f
//    val xPosition = 50f
//    var yPosition = 50f
//
//    // Draw table headers
//    canvas.drawText("Product", xPosition, yPosition, paint)
//    canvas.drawText("Price", xPosition + 200, yPosition, paint)
//    canvas.drawText("Quantity", xPosition + 350, yPosition, paint)
//    yPosition += (textSize * 1.5).toFloat()
//
//    // Draw product basket items
//    for (product in basketList) {
//        canvas.drawText(product.title ?: "", xPosition, yPosition, paint)
//        canvas.drawText("${product.price}", xPosition + 200, yPosition, paint)
//        canvas.drawText("${product.piece}", xPosition + 350, yPosition, paint)
//        yPosition += (textSize * 1.5).toFloat()
//    }
//
//    // Calculate total
//    val total = basketList.sumByDouble { it.price ?: 0.0 * (it.piece ?: 0) }
//    canvas.drawText("Total: $total", xPosition + 350, yPosition + textSize * 2, paint)
//
//    pdfDocument.finishPage(page)
//
//    val pdfFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "basket_invoice.pdf")
//    try {
//        pdfDocument.writeTo(FileOutputStream(pdfFile))
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//
//    pdfDocument.close()
//}

fun formatProductName(name: String, maxLength: Int): String {
    return if (name.length > maxLength) {
        name.substring(0, maxLength - 3) + "..."
    } else {
        name
    }
}
fun generatePDF(resources: Resources,logoBitmap: Bitmap , basketList: List<ProductBasket>, customerName: String, invoiceNumber: String, date: String) {
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
        val formattedProductName = formatProductName(product.title ?: "", 30) // Limit to 50 characters
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

    val pdfFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "formatted_invoice.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(pdfFile))
    } catch (e: IOException) {
        e.printStackTrace()
    }

    pdfDocument.close()
}


class BasketViewModel(private val basketRepository: BasketRepository) : ViewModel() {

    private var _basketTotalLiveData = MutableLiveData<Double>()

    val basketTotalLiveData: LiveData<Double>
        get() = _basketTotalLiveData


    var basketList = mutableListOf<ProductBasket>()
    private var _basketLiveData = MutableLiveData<DataState<List<ProductBasket>?>>()
    val basketLiveData: LiveData<DataState<List<ProductBasket>?>>
        get() = _basketLiveData


    private var _updateProductPieceLiveData = MutableLiveData<DataState<Int>>()
    val updateProductPieceLiveData: LiveData<DataState<Int>>
        get() = _updateProductPieceLiveData


    private var _purchaseLiveData = MutableLiveData<DataState<Int>>()
    val purchaseLiveData: LiveData<DataState<Int>>
        get() = _purchaseLiveData


    init {
        _basketTotalLiveData.value = 0.0
        getProductsBasket()
    }

    private fun getProductsBasket(){

        basketRepository.getAllProductsBasket()
            .addSnapshotListener{ value, error ->

                if(error == null){

                    basketList = mutableListOf()
                    var total = 0.0

                    value?.forEach {
                        val product = it.toObject(ProductBasket::class.java)
                        total += product.price!! * product.piece!!
                        basketList.add(product)
                    }

                    _basketLiveData.value = DataState.Success(basketList)
                    _basketTotalLiveData.value = total

                }else{
                    _basketLiveData.value = DataState.Error(error.message!!)
                }

            }

        }


    fun increaseProduct(productBasket: ProductBasket){

        if(productBasket.piece!! < 100){

            productBasket.piece = productBasket.piece!! + 1
            updateProductPiece(productBasket, true)

        }

    }


    fun reduceProduct(productBasket: ProductBasket){

        if(productBasket.piece!! > 1){

            productBasket.piece = productBasket.piece!! - 1
            updateProductPiece(productBasket, false)

        }else{
            deleteProduct(productBasket)
        }

    }

    private fun updateProductPiece(productBasket: ProductBasket, isIncrease: Boolean){

        basketRepository.updateProductsPiece(productBasket)
            .addOnSuccessListener {

                if(isIncrease) _updateProductPieceLiveData.value = DataState.Success(R.string.product_increased_message)
                else _updateProductPieceLiveData.value = DataState.Success(R.string.product_reduce_message)

            }
            .addOnFailureListener { e ->
                _updateProductPieceLiveData.value = DataState.Error(e.message!!)
            }

    }

    private fun deleteProduct(productBasket: ProductBasket){

        basketRepository.deleteProducts(productBasket)
            .addOnSuccessListener {
                _updateProductPieceLiveData.value = DataState.Success(R.string.product_deleted_message)
            }
            .addOnFailureListener { e ->
                _updateProductPieceLiveData.value = DataState.Error(e.message!!)
            }

    }

    fun clearTheBasket(){

        basketList.forEach {
            deleteProduct(it)
        }

        _purchaseLiveData.value = DataState.Success(R.string.purchase_success_message)


    }
    fun getPDF(resources: Resources,logoBitmap: Bitmap){
        generatePDF(resources,logoBitmap, basketList, "John Doe", "INV12345", "2023-08-17")


    }

//        fun getPDF(){
//            val textData = "hello this is deeapppp"
//            generatePDF(textData)
//        val mDoc= Document()
//        val mFileName=SimpleDateFormat("yyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
//        var mFilePath=Environment.getExternalStorageDirectory().toString()+"/"+mFileName+".pdf"
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
////                mFilePath= .getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + mFileName + ".jpeg";
////            }
////            else
//            mFilePath= Environment.getExternalStorageDirectory().toString() + "/" + mFileName + ".pdf";
//
//
//        try{
//            PdfWriter.getInstance(mDoc,FileOutputStream(mFilePath))
//            mDoc.open()
//            var pdfData="new pdf"
//            val data= pdfData.trim()
//            mDoc.addAuthor("Deepanshu")
//            mDoc.add(Paragraph(data))
//            mDoc.close()
//
//        }
//        catch (e:Exception){
//            Log.d("no pdf",e.message.toString())
//        }
//
//
//    }



}