package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class EsQueryBuilderTest {

	@Test
	void matchProduitLeJsonAttendu() {
		assertEquals("{\"match\":{\"lib_desc_obj\":\"abc\"}}", EsQueryBuilder.match("lib_desc_obj", "abc").toString());
	}

	@Test
	void wildcardProduitLeJsonAttendu() {
		assertEquals("{\"wildcard\":{\"lib_desc_obj\":\"abc*\"}}", EsQueryBuilder.wildcard("lib_desc_obj", "abc*").toString());
	}

	@Test
	void boolMustRegroupeLesClauses() {
		assertEquals(
			"{\"bool\":{\"must\":[{\"match\":{\"f\":\"a\"}},{\"wildcard\":{\"f\":\"b*\"}}]}}",
			EsQueryBuilder.boolMust(List.of(EsQueryBuilder.match("f", "a"), EsQueryBuilder.wildcard("f", "b*"))).toString());
	}

	@Test
	void termsProduitLeJsonAttendu() {
		assertEquals("{\"terms\":{\"type_objet\":[\"CMP\",\"VET\"]}}", EsQueryBuilder.terms("type_objet", List.of("CMP", "VET")).toString());
	}
}
