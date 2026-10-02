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
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EsVersionTest {

	@Test
	void version2DonneV2() {
		assertEquals(EsVersion.V2, EsVersion.fromNumber("2.3.5"));
	}

	@Test
	void version8DonneV8() {
		assertEquals(EsVersion.V8, EsVersion.fromNumber("8.13.0"));
	}

	@Test
	void version9DonneV8() {
		assertEquals(EsVersion.V8, EsVersion.fromNumber("9.0.0"));
	}

	@Test
	void versionNonSupporteeLeve() {
		assertThrows(IllegalArgumentException.class, () -> EsVersion.fromNumber("6.8.0"));
	}
}
