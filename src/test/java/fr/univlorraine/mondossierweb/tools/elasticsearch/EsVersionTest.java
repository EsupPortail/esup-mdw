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
