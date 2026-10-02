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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

/** Construit et interprète une requête de recherche selon la version d'Elasticsearch. */
public interface EsQueryStrategy {

	/** Chemin REST de recherche (ex. `/apogee/_search`). */
	String searchPath(String index);

	/** Corps JSON de la requête, en enveloppant le nœud de requête commun. */
	String buildBody(ObjectNode queryNode);

	/** Interprète la réponse JSON. */
	List<SearchHit> parse(String json) throws IOException;
}
