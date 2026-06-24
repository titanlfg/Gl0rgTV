package tv.gl0rg.kick.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import tv.gl0rg.kick.BuildConfig
import java.io.File

sealed interface InstallLaunchResult {
    data object InstallerOpened : InstallLaunchResult
    data object PermissionSettingsOpened : InstallLaunchResult
    data class Failed(val reason: String, val cause: Throwable? = null) : InstallLaunchResult
}

object UpdateInstaller {
    fun launch(context: Context, apkFile: File): InstallLaunchResult {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return InstallLaunchResult.PermissionSettingsOpened
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            InstallLaunchResult.InstallerOpened
        }.getOrElse { InstallLaunchResult.Failed("install_intent_failed", it) }
    }
}
