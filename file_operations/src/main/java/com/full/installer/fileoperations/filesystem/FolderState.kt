/*
 * Copyright (C) 2014-2021 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
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

package com.full.installer.fileoperations.filesystem

import androidx.annotation.IntDef

const val DOESNT_EXIST = 0
const val WRITABLE_OR_ON_SDCARD = 1

// For Android 5
const val CAN_CREATE_FILES = 2
const val WRITABLE_ON_REMOTE = 3

@IntDef(DOESNT_EXIST, WRITABLE_OR_ON_SDCARD, CAN_CREATE_FILES, WRITABLE_ON_REMOTE)
annotation class FolderState
