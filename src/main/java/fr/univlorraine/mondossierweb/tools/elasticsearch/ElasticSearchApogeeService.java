package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.List;
import java.util.Map;

/** Service de recherche dans Elasticsearch. */
public interface ElasticSearchApogeeService {

	List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearck);

	List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearck, String elasticSearchIndex,
		String elasticSearchChampRecherche, String elasticSearchChampCodeObjet, String elasticSearchChampVersionObjet);

	boolean initConnexion();

	boolean initConnexion(String url, int port, String cluster);
}
