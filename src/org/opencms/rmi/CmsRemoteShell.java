/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.rmi;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsShellCommandException;
import org.opencms.main.I_CmsShellCommands;
import org.opencms.main.OpenCms;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

/**
 * RMI object which wraps a CmsShell and can be used for shell command execution.
 */
public class CmsRemoteShell extends UnicastRemoteObject implements I_CmsRemoteShell {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRemoteShell.class);

    /** Serial version id. */
    private static final long serialVersionUID = -243325251951003282L;

    /** Byte array stream used to capture output of shell commands; will be cleared for each individual command. */
    private ByteArrayOutputStream m_baos = new ByteArrayOutputStream();

    /** Random id string for debugging purposes. */
    private String m_id;

    /** The output stream used to capture the shell command output. */
    private PrintStream m_out;

    /** The wrapped shell instance. */
    private CmsShell m_shell;

    /**
     * Creates a new instance.<p>
     *
     * @param additionalCommandsName a class name for an additional shell commands class (may be null)
     * @param port the port to use
     *
     * @throws CmsException if something goes wrong
     * @throws RemoteException if RMI stuff goes wrong
     */
    public CmsRemoteShell(String additionalCommandsName, int port)
    throws CmsException, RemoteException {
        super(port);
        m_id = RandomStringUtils.randomAlphanumeric(8);
        I_CmsShellCommands additionalCommands = null;
        if (additionalCommandsName != null) {
            try {
                Class<?> commandsCls = Class.forName(additionalCommandsName);
                if (I_CmsShellCommands.class.isAssignableFrom(commandsCls)) {
                    additionalCommands = (I_CmsShellCommands)(commandsCls.newInstance());
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                throw new IllegalArgumentException(
                    "Could not create command class instance for " + additionalCommandsName,
                    e);
            }
        }

        CmsObject cms = OpenCms.initCmsObject("Guest");
        m_out = new PrintStream(m_baos, true);
        m_shell = new CmsShell(cms, "${user}@${project}:${siteroot}|${uri}>", additionalCommands, m_out, m_out);
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShell#executeCommand(java.lang.String, java.util.List)
     */
    public CmsShellCommandResult executeCommand(String cmd, List<String> params) {

        LOG.debug(m_id + " executing " + cmd + " " + params);
        CmsShellCommandResult result = new CmsShellCommandResult();
        m_baos.reset();
        boolean hasError = false;
        try {
            CmsShell.SHELL_INSTANCE.set(m_shell);
            m_shell.executeCommand(cmd, params);
        } catch (CmsShellCommandException e) {
            hasError = true;
            LOG.warn(m_id + " " + e.getLocalizedMessage(), e);
        } finally {
            CmsShell.SHELL_INSTANCE.set(null);
            m_out.flush();
        }
        hasError |= m_shell.hasReportError();
        result.setExitCalled(m_shell.isExitCalled());
        result.setHasError(hasError);
        result.setErrorCode(m_shell.getErrorCode());
        result.setPrompt(m_shell.getPrompt());
        result.setEcho(m_shell.hasEcho());
        try {
            String outputString = new String(m_baos.toByteArray(), "UTF-8");
            result.setOutput(outputString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @see org.opencms.rmi.I_CmsRemoteShell#getPrompt()
     */
    public String getPrompt() {

        return m_shell.getPrompt();
    }

}