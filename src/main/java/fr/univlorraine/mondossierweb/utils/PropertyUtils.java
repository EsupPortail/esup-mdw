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
package fr.univlorraine.mondossierweb.utils;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author Charlie Dubois
 * 
 * Accès aux porperties du fichier de config
 */
public class PropertyUtils {


	public final static String AFFICHAGE_NOM_BASIQUE = "basique";

	public final static String AFFICHAGE_NOM_STANDARD = "standard";

	/** Retourne l'url de l'application */
	public static String getAppUrl(){
		String value = System.getProperty("context.app.url");
		if(!StringUtils.hasText(value)) throw new NullPointerException("app.url cannot be null !");
		return value;
	}

	/** Retourne vrai si on doit rediriger automatique sur la version mobile si cela est possible */
	public static boolean isEnableAutoredirectToMobileView(){
		if(StringUtils.hasText(System.getProperty("context.autoredirect.to.mobileview"))
			&& System.getProperty("context.autoredirect.to.mobileview").equals("true")){
			return true;
		}
		return false;
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

	/** Retourne le champ de l'index elasticSearch correspondant au code de l'objet */
	public static String getElasticSearchChampCodeObjet(){
		String value = System.getProperty("context.param.elasticsearch.index.champcodeobjet");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.index.champcodeobjet cannot be null !");
		return value;
	}

	/** Retourne le champ de l'index elasticSearch correspondant à la version de l'objet */
	public static String getElasticSearchChampVersionObjet(){
		String value = System.getProperty("context.param.elasticsearch.index.champversionobjet");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.index.champversionobjet cannot be null !");
		return value;
	}

	/** Retourne le champ de l'index elasticSearch correspondant au libellé de l'objet */
	public static String getElasticSearchChampLibelleObjet(){
		String value = System.getProperty("context.param.elasticsearch.index.champlibelleobjet");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.index.champlibelleobjet cannot be null !");
		return value;
	}


	/** Retourne le port ElasticSearch */
	public static int getElasticSearchPort(){
		String value = System.getProperty("context.param.elasticsearch.port");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.elasticsearch.port cannot be null !");
		return Integer.parseInt(value);
	}



	/** Retourne vrai on doit aller voir dans la table utilisateur d'apogée pour gérer les accès à l'appli */
	public static boolean isLoginApogee(){
		if(StringUtils.hasText(System.getProperty("context.loginApogee"))
			&& System.getProperty("context.loginApogee").equals("true")){
			return true;
		}
		return false;
	}

	/** Retourne vrai si on effectue des tests de dev en local */
	public static boolean isLocalDevModeEnabled() {
		if(StringUtils.hasText(System.getProperty("context.localDevMode"))
			&& System.getProperty("context.localDevMode").equals("true")){
			return true;
		}
		return false;
	}

	/** Retourne vrai si on doit tester le témoin EN SERVICE de la table utilisateur d'apogée pour gérer les accès à l'appli */
	public static boolean isCheckTesUtilisateurApogee(){
		if(StringUtils.hasText(System.getProperty("context.checkTesUtilisateurApogee"))
			&& System.getProperty("context.checkTesUtilisateurApogee").equals("true")){
			return true;
		}
		return false;
	}

	/** Retourne vrai on doit activer l'encryption sur les pdf générés */
	public static boolean isEnablePdfSecurity(){
		if(StringUtils.hasText(System.getProperty("context.EnablePdfSecurity"))
			&& System.getProperty("context.EnablePdfSecurity").equals("true")){
			return true;
		}
		return false;
	}

	/** Retourne vrai on doit affiche le l'indicateur de loading entre certains écrans */
	public static boolean isShowLoadingIndicator(){
		if(StringUtils.hasText(System.getProperty("context.showLoadingIndicator"))
			&& System.getProperty("context.showLoadingIndicator").equals("true")){
			return true;
		}
		return false;
	}

	/** Retourne vrai on doit utiliser le push */
	public static boolean isPushEnabled(){
		if(StringUtils.hasText(System.getProperty("context.enablePush"))
			&& System.getProperty("context.enablePush").equals("true")){
			return true;
		}
		return false;
	}



	/*public static boolean isWebSocketPushEnabled(){
		if(StringUtils.hasText(System.getProperty("context.webSocketPush"))
				&& System.getProperty("context.webSocketPush").equals("true")){
			return true;
		}
		return false;
	}*/

	/** Retourne la clé apogée pour décrypter les blob de la base apogée */
	public static String getClefApogeeDecryptBlob(){
		String value = System.getProperty("context.clefApogeeDecryptBlob");
		if(!StringUtils.hasText(value)) throw new NullPointerException("clefApogeeDecryptBlob cannot be null !");
		return value;
	}


	/** Retourne le type Etudiant dans Ldap */
	public static List<String> getTypeEtudiantLdap(){
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.typeEtudiantLdap");
		if(!StringUtils.hasText(value)) throw new NullPointerException("typeEtudiantLdap cannot be null !");
		for(String s : value.split(";")){
			values.add(s);
		}
		return values;
	}



	/** Retourne la propriete ldap designant l'uid  */
	public static String getAttributLdapUid(){
		String value = System.getProperty("context.ldap.uid.attribute");
		if(!StringUtils.hasText(value)) throw new NullPointerException("ldap.uid.attribute cannot be null !");
		return value;
	}

	/** Retourne la propriete ldap du contact désignant son type (typeEtudiantLdap ou pas )  */
	public static String getAttributLdapEtudiant(){
		String value = System.getProperty("context.attributLdapEtudiant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("attributLdapEtudiant cannot be null !");
		return value;
	}

	/** Retourne la propriete ldap du contact désignant ses groupes  */
	public static String getAttributGroupeLdap(){
		return System.getProperty("context.attributGroupeLdap");
	}

	/** Retourne la liste des groupes ldap autorisés  */
	public static List<String> getListeGroupesLdapAutorises(){
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.listeGroupesLdap");
		if(!StringUtils.hasText(value)) return values;
		for(String s : value.split(";")){
			values.add(s);
		}
		return values;
	}

	/** Retourne la propriete ldap du contact désignant son code etudiant  */
	public static String getAttributLdapCodEtu(){
		String value = System.getProperty("context.attributLdapCodEtu");
		if(!StringUtils.hasText(value)) throw new NullPointerException("attributLdapCodEtu cannot be null !");
		return value;
	}

	/** Retourne la liste des groupes uportal autorisés  */
	public static List<String> getListeGroupesUportalAutorises(){
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.uportal.groupes.autorises");
		if(!StringUtils.hasText(value)) return null;
		for(String s : value.split(";")){
			values.add(s);
		}
		return values;
	}

	/** Retourne vrai si les données de l'état-civil doivent être récupérées dans l'annuaire */
	public static boolean isRecupMailAnnuaireApogee(){
		if(StringUtils.hasText(System.getProperty("context.param.apogee.mail.annuaire"))
			&& System.getProperty("context.param.apogee.mail.annuaire").equals("true")){
			return true;
		}
		return false;
	}

	public static String getSourceResultats() {
		String value = System.getProperty("context.sourceResultats");
		if(!StringUtils.hasText(value)) throw new NullPointerException("sourceResultats cannot be null !");
		return value;
	}


	/** Retourne la liste des groupes ldap autorisés  */
	public static List<String> getListeErreursAIgnorer(){
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.liste.erreur.a.ignorer");
		if(!StringUtils.hasText(value)) return values;
		for(String s : value.split(";")){
			values.add(s);
		}
		return values;
	}

	/** Façon d'afficher le nom des étudiants	 */
	public static String getTypeAffichageNomEtatCivil() {
		String value = System.getProperty("context.etatcivil.nom.affichage");
		if(!StringUtils.hasText(value)) throw new NullPointerException("etatcivil.nom.affichage cannot be null !");
		return value;
	}


	/** Retourne le username de l'utilisateur des WebServices Apogée à utiliser pour MDW   */
	public static String getApoWsUsername(){
		String value = System.getProperty("context.apoWsUsername");
		if(!StringUtils.hasText(value)) return null;
		return value;
	}

	/** Retourne le password de l'utilisateur des WebServices Apogée à utiliser pour MDW   */
	public static String getApoWsPassword(){
		String value = System.getProperty("context.apoWsPassword");
		if(!StringUtils.hasText(value)) return null;
		return value;
	}
	
	/** Retourne si oui ou non on affiche le sql des requêtes directes sur Apogée   */
	public static boolean getShowSqlApogee(){
		String value = System.getProperty("context.apoShowSql");
		if(!StringUtils.hasText(value) || !value.equals("true")) return false;
		return true;
	}


}
