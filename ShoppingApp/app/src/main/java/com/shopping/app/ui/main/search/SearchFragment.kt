package com.shopping.app.ui.main.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.shopping.app.R
import com.shopping.app.data.api.ApiClient
import com.shopping.app.data.model.CategoryModel
import com.shopping.app.data.model.DataState
import com.shopping.app.data.repository.search.SearchRepositoryImpl
import com.shopping.app.databinding.FragmentSearchBinding
import com.shopping.app.ui.loadingprogress.LoadingProgressBar
import com.shopping.app.ui.main.search.adapter.CategoryAdapter
import com.shopping.app.ui.main.search.adapter.SearchAdapter
import com.shopping.app.ui.main.search.viewmodel.SearchViewModel
import com.shopping.app.ui.main.search.viewmodel.SearchViewModelFactory
import com.shopping.app.utils.Constants

class SearchFragment : Fragment(), CategoryClickListener, SearchView.OnQueryTextListener {

    private lateinit var bnd: FragmentSearchBinding
    private lateinit var loadingProgressBar: LoadingProgressBar
    private lateinit var barcodeView: CompoundBarcodeView
    private lateinit var resultTextView: TextView
    private val viewModel by viewModels<SearchViewModel> {
        SearchViewModelFactory(
            SearchRepositoryImpl(
                ApiClient.getApiService()
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bnd = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        barcodeView = bnd.barcodeScannerView
        resultTextView = bnd.resultTextView
        return bnd.root
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private fun startBarcodeScanning() {
        barcodeView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { barcodeText ->
                    resultTextView.text = barcodeText
                    searchQuery(resultTextView.text.toString().substring(0,1))
//                    onQueryTextChange(resultTextView.text.toString())
                    val product = viewModel.searchedProduct
//        bnd.searchView.setQuery("",true)
                    if(product==null)return
                    val navController = findNavController()
                    navController.navigate(
                        R.id.action_searchFragment_to_productDetailsFragment,
                        Bundle().apply {
                            putString(Constants.PRODUCT_MODEL_NAME, product?.toJson())
                        })
//                    searchQuery(resultTextView.text.toString())
//                    bnd.searchView.setQuery(barcodeText.substring(0,1),true)
                    //TODO barcode scanner
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
                // Optional: Handle possible result points
            }
        })
        barcodeView.resume()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanning()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startBarcodeScanning()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){

        loadingProgressBar = LoadingProgressBar(requireContext())

        viewModel.searchLiveData.observe(viewLifecycleOwner){

            when (it) {
                is DataState.Success -> {
                    loadingProgressBar.hide()
                    it.data?.let { safeData ->

                        val searchAdapter = SearchAdapter(findNavController(), safeData)
                        bnd.rvSearch.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        bnd.rvSearch.setHasFixedSize(true)
                        bnd.rvSearch.adapter = searchAdapter

                    } ?: run {
                        Snackbar.make(bnd.root, getString(R.string.no_data), Snackbar.LENGTH_LONG).show()
                    }
                }
                is DataState.Error -> {
                    loadingProgressBar.hide()
                    Snackbar.make(bnd.root, it.message, Snackbar.LENGTH_LONG).show()
                }
                is DataState.Loading -> {
                    loadingProgressBar.show()
                }
            }

        }

        viewModel.categoryLiveData.observe(viewLifecycleOwner){

            when (it) {
                is DataState.Success -> {
                    it.data?.let { safeData ->

                        val categoryAdapter = CategoryAdapter(safeData, this)
                        bnd.rvCategory.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        bnd.rvCategory.setHasFixedSize(true)
                        bnd.rvCategory.adapter = categoryAdapter

                    } ?: run {
                        Snackbar.make(bnd.root, getString(R.string.no_data), Snackbar.LENGTH_LONG).show()
                    }
                }
                is DataState.Error -> {
                    Snackbar.make(bnd.root, it.message, Snackbar.LENGTH_LONG).show()
                }
                is DataState.Loading -> {}
            }

        }

        bnd.searchView.setOnQueryTextListener(this)

    }

    private fun searchQuery(query:String?){

        if(query != null && query.length > 0){
            viewModel.searchProducts(true, query.lowercase())
        }else{
            viewModel.searchProducts()
        }

    }

    override fun onClickCategory(category: CategoryModel) {
        clearSearchView()
        viewModel.getProductsByCategoryCheck(category)
    }

    private fun clearSearchView(){
        bnd.searchView.setQuery("", false)
        bnd.searchView.clearFocus()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchQuery(query)
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchQuery(newText)
        return false
    }

}

interface CategoryClickListener{
    fun onClickCategory(category: CategoryModel)
}