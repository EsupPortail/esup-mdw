package fr.univlorraine.mondossierweb.entities.solr;

import org.apache.solr.client.solrj.beans.Field;

import lombok.Data;

@Data
public class ObjSolr {
	
	@Field
	String id;
	
	@Field
	String type;
	
	@Field
	String libelle;
	
	@Field
	String description;
	
	@Field
	String code;
	
	@Field
	int version;

}
