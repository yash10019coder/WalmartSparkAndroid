package com.shopping.app.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.shopping.app.R

class MainActivity : AppCompatActivity() {

    private var isWritePermissionGranted=false
    private var isManagePermissionGranted=false
    private var isReadPermissionGranted=false
    private lateinit var permissionLauncher:ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissions->
            isManagePermissionGranted=permissions[android.Manifest.permission.MANAGE_EXTERNAL_STORAGE]?:isManagePermissionGranted
            isReadPermissionGranted=permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE]?:isReadPermissionGranted
            isWritePermissionGranted=permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE]?:isWritePermissionGranted
        }
        requestPermission()
    }

    private fun requestPermission(){

        val permissionRequest:MutableList<String> = ArrayList()

        isManagePermissionGranted=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED

        if(!isManagePermissionGranted){
            permissionRequest.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }
        isReadPermissionGranted=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED

        if(!isReadPermissionGranted){
            permissionRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        isWritePermissionGranted=
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED
        if(!isWritePermissionGranted){
            permissionRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }
}