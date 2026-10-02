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
