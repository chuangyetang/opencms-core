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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.shared.CmsSerialDateUtil;
import org.opencms.ade.contenteditor.client.Messages;

/** Abstract base class for pattern panel controllers. */
public abstract class A_CmsPatternPanelController implements I_CmsSerialDatePatternController {

    /** The change handler called on {@link #onValueChange()}. */
    private final I_ChangeHandler m_changeHandler;
    /** The model to read data from. */
    protected final CmsSerialDateValueWrapper m_model;

    /**
     * Constructor for the abstract pattern panel controller
     * @param model the model to read data from.
     * @param validationHandler the validation handler used for validation.
     */
    public A_CmsPatternPanelController(final CmsSerialDateValueWrapper model, final I_ChangeHandler validationHandler) {
        m_model = model;
        m_changeHandler = validationHandler;

    }

    /**
     * @see org.opencms.acacia.client.widgets.serialdate.I_CmsSerialDatePatternController#getView()
     */
    abstract public I_CmsPatternView getView();

    /**
     * Call when the value has changed.
     */
    protected void onValueChange() {

        m_changeHandler.valueChanged();
    }

    /**
     * Validates if a suitable day of month is set.
     * @return An error message, if the day of month is invalid, <code>null</code> otherwise.
     */
    protected String validateDayOfMonth() {

        return (m_model.getDayOfMonth() > 0) && (m_model.getDayOfMonth() < 32)
        ? null
        : Messages.get().key(Messages.GUI_SERIALDATE_ERROR_INVALID_DAYOFMONTH_0);
    }

    /**
     * Validates if the interval between two events is valid.
     * @return An error message, if the interval is invalid, <code>null</code> otherwise.
     */
    protected String validateInterval() {

        return m_model.getInterval() < 1 ? Messages.get().key(Messages.GUI_SERIALDATE_ERROR_INVALID_INTERVAL_0) : null;
    }

    /**
     * Sets the day of the month.
     * @param day the day to set.
     */
    void setDayOfMonth(String day) {

        int i = CmsSerialDateUtil.toIntWithDefault(day, -1);
        if (m_model.getDayOfMonth() != i) {
            m_model.setDayOfMonth(i);
            onValueChange();
        }

    }

    /**
     * Sets the interval.
     * @param interval the interval to set.
     */
    void setInterval(String interval) {

        int i = CmsSerialDateUtil.toIntWithDefault(interval, -1);
        if (m_model.getInterval() != i) {
            m_model.setInterval(i);
            onValueChange();
        }

    }
}