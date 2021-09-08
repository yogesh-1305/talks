package com.example.talks.others.utility

import android.Manifest
import android.os.Build
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionsUtility {

    fun hasStoragePermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

    fun hasContactsPermissions(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.READ_CONTACTS
        )
}