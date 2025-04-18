/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.utils;


import com.vaadin.server.ConnectorResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.ui.UI;
import fr.univlorraine.tools.vaadin.BusyIndicatorFileDownloader;
import fr.univlorraine.tools.vaadin.BusyIndicatorWindow;

import java.io.IOException;


public class MyFileDownloader extends BusyIndicatorFileDownloader {

	private long fileSize = 0;

	public MyFileDownloader(Resource resource) {
		this(resource, 0);
	}

	public MyFileDownloader(Resource resource, long fileSize) {
		super(resource);
		this.fileSize = fileSize;
	}

	@Override
	public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {
		BusyIndicatorWindow busyIndicatorWindow = new BusyIndicatorWindow();
		UI ui = UI.getCurrent();
		ui.access(() -> ui.addWindow(busyIndicatorWindow));
		try {
			if (!path.matches("dl(/.*)?")) {
				// Ignore if it isn't for us
				return false;
			}

			Resource resource = getFileDownloadResource();

			if (resource instanceof ConnectorResource) {
				DownloadStream stream = ((ConnectorResource) resource).getStream();

				if ( stream == null )
					return false;

				if (stream.getParameter("Content-Disposition") == null) {
					// Content-Disposition: attachment generally forces download
					stream.setParameter("Content-Disposition",
							"attachment; filename=\"" + stream.getFileName() + "\"");
				}

				//Forcer "Ouvrir avec" par défaut. Permet de proposer "Ouvrir avec" sous Firefox/MacOS. Indisponible sinon
				stream.setParameter("Content-Type","application/force-download");

				// Content-Type to block eager browser plug-ins from hijacking the
				// file
				if (isOverrideContentType()) {
					stream.setContentType("application/octet-stream;charset=UTF-8");
				}

				if ( fileSize > 0 ) {
					stream.setParameter("Content-Length", "" + fileSize);
				}

				stream.writeResponse(request, response);
				return true;
			} else {
				return false;
			}
		} finally {
			busyIndicatorWindow.close();
		}
	}
}
