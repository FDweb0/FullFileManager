/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
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

package com.full.installer.asynchronous.asynctasks;

import static com.full.installer.ui.activities.MainActivity.TAG_INTENT_FILTER_FAILED_OPS;
import static com.full.installer.ui.activities.MainActivity.TAG_INTENT_FILTER_GENERAL;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.full.installer.R;
import com.full.installer.application.AppConfig;
import com.full.installer.database.CryptHandler;
import com.full.installer.fileoperations.exceptions.ShellNotRunningException;
import com.full.installer.fileoperations.filesystem.OpenMode;
import com.full.installer.filesystem.HybridFile;
import com.full.installer.filesystem.HybridFileParcelable;
import com.full.installer.filesystem.SafRootHolder;
import com.full.installer.filesystem.cloud.CloudUtil;
import com.full.installer.filesystem.files.CryptUtil;
import com.full.installer.filesystem.files.FileUtils;
import com.full.installer.ui.activities.MainActivity;
import com.full.installer.ui.fragments.CompressedExplorerFragment;
import com.full.installer.ui.fragments.preferencefragments.PreferencesConstants;
import com.full.installer.ui.notifications.NotificationConstants;
import com.full.installer.utils.DataUtils;
import com.full.installer.utils.OTGUtil;
import com.cloudrail.si.interfaces.CloudStorage;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import jcifs.smb.SmbException;

public class DeleteTask
    extends AsyncTask<ArrayList<HybridFileParcelable>, String, AsyncTaskResult<Boolean>> {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteTask.class);

  private ArrayList<HybridFileParcelable> files;
  private final Context applicationContext;
  private final boolean rootMode;
  private CompressedExplorerFragment compressedExplorerFragment;
  private final DataUtils dataUtils = DataUtils.getInstance();

  public DeleteTask(@NonNull Context applicationContext) {
    this.applicationContext = applicationContext.getApplicationContext();
    rootMode =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
  }

  public DeleteTask(
      @NonNull Context applicationContext, CompressedExplorerFragment compressedExplorerFragment) {
    this.applicationContext = applicationContext.getApplicationContext();
    rootMode =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean(PreferencesConstants.PREFERENCE_ROOTMODE, false);
    this.compressedExplorerFragment = compressedExplorerFragment;
  }

  @Override
  protected void onProgressUpdate(String... values) {
    super.onProgressUpdate(values);
    Toast.makeText(applicationContext, values[0], Toast.LENGTH_SHORT).show();
  }

  @Override
  @SafeVarargs
  protected final AsyncTaskResult<Boolean> doInBackground(
      final ArrayList<HybridFileParcelable>... p1) {
    files = p1[0];
    boolean wasDeleted = true;
    if (files.size() == 0) return new AsyncTaskResult<>(true);

    for (HybridFileParcelable file : files) {
      try {
        wasDeleted = doDeleteFile(file);
        if (!wasDeleted) break;
      } catch (Exception e) {
        return new AsyncTaskResult<>(e);
      }

      // delete file from media database
      if (!file.isSmb()) {
        try {
          deleteFromMediaDatabase(applicationContext, file.getPath());
        } catch (Exception e) {
          FileUtils.scanFile(applicationContext, files.toArray(new HybridFile[files.size()]));
        }
      }

      // delete file entry from encrypted database
      if (file.getName(applicationContext).endsWith(CryptUtil.CRYPT_EXTENSION)) {
        CryptHandler handler = CryptHandler.INSTANCE;
        handler.clear(file.getPath());
      }
    }

    return new AsyncTaskResult<>(wasDeleted);
  }

  @Override
  public void onPostExecute(AsyncTaskResult<Boolean> result) {

    Intent intent = new Intent(MainActivity.KEY_INTENT_LOAD_LIST);
    if (files.size() > 0) {
      String path = files.get(0).getParent(applicationContext);
      intent.putExtra(MainActivity.KEY_INTENT_LOAD_LIST_FILE, path);
      applicationContext.sendBroadcast(intent);
    }

    if (result.result == null || !result.result) {
      applicationContext.sendBroadcast(
          new Intent(TAG_INTENT_FILTER_GENERAL)
              .putParcelableArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS, files));
    } else if (compressedExplorerFragment == null) {
      AppConfig.toast(applicationContext, R.string.done);
    }

    if (compressedExplorerFragment != null) {
      compressedExplorerFragment.files.clear();
    }

    // cancel any processing notification because of cut/paste operation
    NotificationManager notificationManager =
        (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(NotificationConstants.COPY_ID);
  }

  private boolean doDeleteFile(@NonNull HybridFileParcelable file) throws Exception {
    switch (file.getMode()) {
      case OTG:
        DocumentFile documentFile =
            OTGUtil.getDocumentFile(file.getPath(), applicationContext, false);
        return documentFile.delete();
      case DOCUMENT_FILE:
        documentFile =
            OTGUtil.getDocumentFile(
                file.getPath(),
                SafRootHolder.getUriRoot(),
                applicationContext,
                OpenMode.DOCUMENT_FILE,
                false);
        return documentFile.delete();
      case DROPBOX:
      case BOX:
      case GDRIVE:
      case ONEDRIVE:
        CloudStorage cloudStorage = dataUtils.getAccount(file.getMode());
        try {
          cloudStorage.delete(CloudUtil.stripPath(file.getMode(), file.getPath()));
          return true;
        } catch (Exception e) {
          LOG.warn("failed to delete cloud files", e);
          return false;
        }
      default:
        try {
          return (file.delete(applicationContext, rootMode));
        } catch (ShellNotRunningException | SmbException e) {
          LOG.warn("failed to delete files", e);
          throw e;
        }
    }
  }

  private void deleteFromMediaDatabase(final Context context, final String file) {
    final String where = MediaStore.MediaColumns.DATA + "=?";
    final String[] selectionArgs = new String[] {file};
    final ContentResolver contentResolver = context.getContentResolver();
    final Uri filesUri = MediaStore.Files.getContentUri("external");
    // Delete the entry from the media database. This will actually delete media files.
    contentResolver.delete(filesUri, where, selectionArgs);
  }
}
