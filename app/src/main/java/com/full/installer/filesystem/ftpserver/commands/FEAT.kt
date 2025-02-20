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

package com.full.installer.filesystem.ftpserver.commands

import com.full.installer.R
import com.full.installer.application.AppConfig
import org.apache.ftpserver.command.AbstractCommand
import org.apache.ftpserver.ftplet.DefaultFtpReply
import org.apache.ftpserver.ftplet.FtpReply
import org.apache.ftpserver.ftplet.FtpRequest
import org.apache.ftpserver.impl.FtpIoSession
import org.apache.ftpserver.impl.FtpServerContext

/**
 * Custom [org.apache.ftpserver.command.impl.FEAT] to add [AVBL] command to the list.
 */
class FEAT : AbstractCommand() {
    override fun execute(session: FtpIoSession, context: FtpServerContext, request: FtpRequest) {
        session.resetState()
        session.write(
            DefaultFtpReply(
                FtpReply.REPLY_211_SYSTEM_STATUS_REPLY,
                AppConfig.getInstance().getString(R.string.ftp_command_FEAT)
            )
        )
    }
}
