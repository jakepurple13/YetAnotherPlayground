package com.programmersbox.extensionloader

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import dalvik.system.PathClassLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

private val PACKAGE_FLAGS =
    PackageManager.GET_CONFIGURATIONS or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
    }

class ExtensionLoader<T, R>(
    private val context: Context,
    private val extensionFeature: String,
    private val metadataClass: String,
    private val mapping: (T, ApplicationInfo) -> R
) {
    suspend fun loadExtensions(mapped: (T, ApplicationInfo) -> R = mapping): List<R> {
        val packageManager = context.packageManager
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(PACKAGE_FLAGS)
        }
            .filter { it.reqFeatures.orEmpty().any { it.name == extensionFeature } }

        return runBlocking {
            packages
                .map { async { loadOne(it, mapped) } }
                .flatMap { it.await() }
        }
    }

    private fun loadOne(packageInfo: PackageInfo, mapped: (T, ApplicationInfo) -> R): List<R> {
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getApplicationInfo(
                packageInfo.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(
                packageInfo.packageName,
                PackageManager.GET_META_DATA
            )
        }

        val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)

        return appInfo.metaData.getString(metadataClass)
            .orEmpty()
            .split(";")
            .map {
                val sourceClass = it.trim()
                if (sourceClass.startsWith(".")) {
                    packageInfo.packageName + sourceClass
                } else {
                    sourceClass
                }
            }
            .mapNotNull {
                @Suppress("UNCHECKED_CAST")
                Class.forName(it, false, classLoader)
                    .getDeclaredConstructor()
                    .newInstance() as? T
            }
            .map { mapped(it, appInfo) }
    }
}