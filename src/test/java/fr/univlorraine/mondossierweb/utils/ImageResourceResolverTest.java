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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ImageResourceResolverTest {

	/** Image de document embarquée dans {@code src/main/resources/images/}. */
	private static final String IMAGE = "logo-UL.png";

	@Test
	void cheminWebNormaliseVersLaRacineDocuments() {
		assertEquals("images/logo-UL.png", ImageResourceResolver.toClasspathLocation("/images/logo-UL.png"));
	}

	@Test
	void nomDeFichierSeulPrefixeParLaRacineDocuments() {
		assertEquals("images/logo-UL.png", ImageResourceResolver.toClasspathLocation(IMAGE));
	}

	@Test
	void cheminClasspathDejaCompletInchange() {
		assertEquals("images/logo-UL.png", ImageResourceResolver.toClasspathLocation("images/logo-UL.png"));
	}

	@Test
	void separateursWindowsNormalises() {
		assertEquals("images/logo-UL.png", ImageResourceResolver.toClasspathLocation("images\\logo-UL.png"));
	}

	@Test
	void imageDeDocumentLueDepuisLeClasspath() {
		final byte[] bytes = ImageResourceResolver.readClasspathImage(IMAGE);

		assertNotNull(bytes);
		assertTrue(bytes.length > 0);
	}

	@Test
	void imageTrouveeQuelQueSoitLePrefixe() {
		assertNotNull(ImageResourceResolver.readClasspathImage("/images/" + IMAGE));
		assertNotNull(ImageResourceResolver.readClasspathImage("images/" + IMAGE));
	}

	@Test
	void imageAbsenteRetourneNull() {
		assertNull(ImageResourceResolver.readClasspathImage("image-inexistante.png"));
	}

	@Test
	void valeurVideRetourneNull() {
		assertNull(ImageResourceResolver.readClasspathImage(null));
		assertNull(ImageResourceResolver.readClasspathImage(""));
		assertNull(ImageResourceResolver.readClasspathImage("   "));
	}

	@Test
	void urlDistanteDetectee() {
		assertTrue(ImageResourceResolver.isRemoteUrl("https://example.org/logo.png"));
		assertTrue(ImageResourceResolver.isRemoteUrl("http://example.org/logo.png"));
		assertTrue(ImageResourceResolver.isRemoteUrl("//example.org/logo.png"));
		assertFalse(ImageResourceResolver.isRemoteUrl("images/logo-UL.png"));
		assertFalse(ImageResourceResolver.isRemoteUrl(null));
	}
}
