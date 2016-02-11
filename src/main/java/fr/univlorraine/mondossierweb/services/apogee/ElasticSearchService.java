/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.services.apogee;

import java.util.List;
import java.util.Map;



public interface ElasticSearchService {
	

	
	public abstract List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearch);
	
	public abstract boolean initConnexion(boolean fullInit);
}
