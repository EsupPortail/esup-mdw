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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Fichier généré côté serveur, prêt à être servi par un DownloadHandler Flow
 * (remplace les StreamResource Vaadin 7 retournées par les exports).
 *
 * @param fileName nom du fichier proposé au téléchargement
 * @param content  contenu binaire du fichier
 * @param mimeType type mime du fichier
 */
public record DownloadFile(String fileName, byte[] content, String mimeType) {

	public static final String MIME_TYPE_PDF = "application/pdf";
	public static final String MIME_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	public long getContentLength() {
		return content.length;
	}
}
