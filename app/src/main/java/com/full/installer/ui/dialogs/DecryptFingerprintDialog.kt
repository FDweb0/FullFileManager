/*
 * Copyright (C) 2014-2022 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.full.installer.ui.dialogs

import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import com.afollestad.materialdialogs.MaterialDialog
import com.full.installer.R
import com.full.installer.filesystem.files.CryptUtil
import com.full.installer.filesystem.files.EncryptDecryptUtils.DecryptButtonCallbackInterface
import com.full.installer.ui.activities.MainActivity
import com.full.installer.ui.theme.AppTheme
import com.full.installer.utils.FingerprintHandler
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Decrypt dialog prompt for user fingerprint.
 */
object DecryptFingerprintDialog {

    /**
     * Display dialog prompting user for fingerprint in order to decrypt file.
     */
    @JvmStatic
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Throws(
        GeneralSecurityException::class,
        IOException::class
    )
    fun show(
        c: Context,
        main: MainActivity,
        intent: Intent,
        appTheme: AppTheme,
        decryptButtonCallbackInterface: DecryptButtonCallbackInterface
    ) {
        val accentColor = main.accent
        val builder = MaterialDialog.Builder(c)
        builder.title(c.getString(R.string.crypt_decrypt))
        val rootView = View.inflate(c, R.layout.dialog_decrypt_fingerprint_authentication, null)
        val cancelButton = rootView.findViewById<Button>(R.id.button_decrypt_fingerprint_cancel)
        cancelButton.setTextColor(accentColor)
        builder.customView(rootView, true)
        builder.canceledOnTouchOutside(false)
        builder.theme(appTheme.getMaterialDialogTheme(c))
        val dialog = builder.show()
        cancelButton.setOnClickListener { v: View? -> dialog.cancel() }
        val manager = c.getSystemService(FingerprintManager::class.java)
        val handler = FingerprintHandler(c, intent, dialog, decryptButtonCallbackInterface)
        val `object` = FingerprintManager.CryptoObject(CryptUtil.initCipher())
        handler.authenticate(manager, `object`)
    }
}
