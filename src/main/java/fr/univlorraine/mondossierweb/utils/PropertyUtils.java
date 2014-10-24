package fr.univlorraine.mondossierweb.utils;

import org.springframework.util.StringUtils;

/**
 * @author Charlie Dubois
 * 
 * Accès aux porperties du fichier de config
 */
public class PropertyUtils {

	
	
	/** Retourne l'url de l'application */
	public static String getAppUrl(){
		String value = System.getProperty("context.app.url");
		if(!StringUtils.hasText(value)) throw new NullPointerException("app.url cannot be null !");
		return value;
	}
	
	/** Retourne l'url de l'application */
	public static String getAppUrlPath(){
		String value = System.getProperty("context.app.path");
		if(!StringUtils.hasText(value)) throw new NullPointerException("app.path cannot be null !");
		return value;
	}
	
	/** Retourne l'url de l'application */
	public static String getSolrUrl(){
		String value = System.getProperty("context.solr.url");
		if(!StringUtils.hasText(value)) throw new NullPointerException("solr.url cannot be null !");
		return value;
	}
	
	/** Retourne vrai si les données de l'état-civil doivent être récupérées dans l'annuaire */
	public static boolean isRecupMailAnnuaireApogee(){
		if(StringUtils.hasText(System.getProperty("context.param.apogee.mail.annuaire"))
				&& System.getProperty("context.param.apogee.mail.annuaire").equals("true")){
			return true;
		}
		return false;
	}
	
	
	/** Retourne l'extension au login pour l'e-mail des étudiants */
	public static String getExtensionMailEtudiant(){
		String value = System.getProperty("context.param.apogee.extesion.mail.etudiant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.apogee.extesion.mail.etudiant cannot be null !");
		return value;
	}
	
	/** Retourne l'url de serveur elasticSearch */
	public static String getElasticSearchUrl(){
		String value = System.getProperty("context.param.elasticsearch.url");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.url cannot be null !");
		return value;
	}
	
	/** Retourne le nom du cluster elasticSearch */
	public static String getElasticSearchCluster(){
		String value = System.getProperty("context.param.elasticsearch.cluster");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.cluster cannot be null !");
		return value;
	}
	
	/** Retourne l'index elasticSearch */
	public static String getElasticSearchIndex(){
		String value = System.getProperty("context.param.elasticsearch.index");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.index cannot be null !");
		return value;
	}
	
	/** Retourne le champ de recherche de l'index elasticSearch */
	public static String getElasticSearchChampRecherche(){
		String value = System.getProperty("context.param.elasticsearch.index.champrecherche");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.index.champrecherche cannot be null !");
		return value;
	}
	
	/** Retourne le port ElasticSearch */
	public static int getElasticSearchPort(){
		String value = System.getProperty("context.param.elasticsearch.port");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.port cannot be null !");
		return Integer.parseInt(value);
	}
	
	

}
