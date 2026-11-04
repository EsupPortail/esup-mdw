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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PdfUtilsImageTest {

	@Test
	void imageDeDocumentChargeeDepuisLeClasspath() throws Exception {
		assertNotNull(PdfUtils.getImage("images/logo-UL.png"));
		assertNotNull(PdfUtils.getImage("/images/logo-UL.png"));
		assertNotNull(PdfUtils.getImage("logo-UL.png"));
	}

	@Test
	void imageChargeeAvecSaLargeurDOrigine() throws Exception {
		final float largeur = PdfUtils.getImage("logo-UL.png").getWidth();

		assertTrue(largeur > 0);
	}

	@Test
	void valeurVideNeChargeRien() throws Exception {
		assertNull(PdfUtils.getImage(null));
		assertNull(PdfUtils.getImage(""));
		assertNull(PdfUtils.getImage("   "));
	}

	@Test
	void imageIntrouvableNeLevePasEtRetourneNull() throws Exception {
		assertNull(PdfUtils.getImage("image-inexistante.png"));
		assertNull(PdfUtils.getImage("/chemin/absolu/inexistant.png"));
	}
}
