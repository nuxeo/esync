/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Benoit Delbosc
 */
package org.nuxeo.tools.esync.listener;

import static org.nuxeo.tools.esync.App.ANSI_BLUE;
import static org.nuxeo.tools.esync.App.ANSI_RESET;

import org.nuxeo.tools.esync.event.InfoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class InfoListener {
    private static final Logger log = LoggerFactory
            .getLogger(InfoListener.class);

    @Subscribe
    public void handleEvent(InfoEvent event) {
        log.info(ANSI_BLUE + event.getMessage() + ANSI_RESET);
    }
}
