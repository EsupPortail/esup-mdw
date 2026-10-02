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
