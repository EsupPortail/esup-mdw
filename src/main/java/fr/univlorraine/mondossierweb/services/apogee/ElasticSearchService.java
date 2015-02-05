package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;
import java.util.Map;

import fr.univlorraine.mondossierweb.entities.solr.ObjSolr;


public interface ElasticSearchService {
	

	
	public abstract List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearch);
	
	
}
