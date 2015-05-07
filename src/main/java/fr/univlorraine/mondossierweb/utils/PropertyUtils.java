package fr.univlorraine.mondossierweb.utils;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.UtilisateurSwapRepository;

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



	/** Retourne vrai on doit aller voir dans la table utilisateur d'apogée pour gérer les accès à l'appli */
	public static boolean isLoginApogee(){
		if(StringUtils.hasText(System.getProperty("context.loginApogee"))
				&& System.getProperty("context.loginApogee").equals("true")){
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

	

	/** Retourne le type Etudiant dans Ldap */
	public static String getTypeEtudiantLdap(){
		String value = System.getProperty("context.typeEtudiantLdap");
		if(!StringUtils.hasText(value)) throw new NullPointerException("typeEtudiantLdap cannot be null !");
		return value;
	}

	/** Retourne la propriete ldap du contact désignant son type (typeEtudiantLdap ou pas )  */
	public static String getAttributLdapEtudiant(){
		String value = System.getProperty("context.attributLdapEtudiant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("attributLdapEtudiant cannot be null !");
		return value;
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
		if(!StringUtils.hasText(value)) throw new NullPointerException("uportal.groupes.autorises cannot be null !");
		for(String s : value.split(",")){
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
	
	/** Retourne l'extension au login pour l'e-mail des étudiants */
/*	public static String getExtensionMailEtudiant(){
		String value = System.getProperty("context.param.apogee.extesion.mail.etudiant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("param.apogee.extesion.mail.etudiant cannot be null !");
		return value;
	}*/
	
	/** Retourne l'url de l'application */
	/*public static String getAppUrlPath(){
		String value = System.getProperty("context.app.path");
		if(!StringUtils.hasText(value)) throw new NullPointerException("app.path cannot be null !");
		return value;
	}*/
	
	
	
	/*public static String getTemoinNotesEtudiant() {
		String value = System.getProperty("context.temoinNotesEtudiant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("temoinNotesEtudiant cannot be null !");
		return value;
	}*/

	

	/*public static String getTemoinNotesEnseignant() {
		String value = System.getProperty("context.temoinNotesEnseignant");
		if(!StringUtils.hasText(value)) throw new NullPointerException("temoinNotesEnseignant cannot be null !");
		return value;
	}*/

	/*public static boolean isAffRangEtudiant() {
		if(StringUtils.hasText(System.getProperty("context.afficherRangEtudiant"))
				&& System.getProperty("context.afficherRangEtudiant").equals("true")){
			return true;
		}
		return false;
	}*/

	/**
	 * retourne la liste des codes etape dont on affiche le rang même si affRangEtudiant=false
	 * @return
	 */
	/*public static List<String> getListeCodesEtapeAffichageRang() {
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.codesEtapeAffichageRang");
		if(StringUtils.hasText(value)){
			for(String s : value.split(",")){
				values.add(s);
			}
			return values;
		}
		return null;
	}*/

	/**
	 * Applique l'affichage de la descendance du semestre que s'il est à T
	 * @return
	 */
	/*public static boolean isTemNotesEtuSem() {
		if(StringUtils.hasText(System.getProperty("context.temNotesEtuSem"))
				&& System.getProperty("context.temNotesEtuSem").equals("true")){
			return true;
		}
		return false;
	}*/

/*	public static boolean isAffMentionEtudiant() {
		if(StringUtils.hasText(System.getProperty("context.affMentionEtudiant"))
				&& System.getProperty("context.affMentionEtudiant").equals("true")){
			return true;
		}
		return false;
	}*/

	/**
	 * retourne la liste des code type Epreuve (COD_TEP) dont on affiche toujours la note
	 * @return
	 */
/*	public static List<String> getTypesEpreuveAffichageNote() {
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.typesEpreuveAffichageNote");
		if(StringUtils.hasText(value)){
			for(String s : value.split(",")){
				values.add(s);
			}
			return values;
		}
		return null;
	}*/

	
	/**
	 * retourne la valeur du témoin (O ou N) temoinCtlValCadEpr (Témoin modalités contrôle validées) pour laquelle on veut que les notes aux épreuves soient visibles même si l'état de délibération n'est pas dans la liste de ceux définis. 
	 */
	/*public static String getTemoinCtlValCadEpr() {
		String value = System.getProperty("context.temoinCtlValCadEpr");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	/**
	 * retourne (O/N) Si temoinFictif est renseigné, seuls les éléments dont tem_fictif est égal à témoinFictif seront affichés dans l'écran du détail des notes
	 */
	/*public static String getTemoinFictif() {
		String value = System.getProperty("context.temoinFictif");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/

	
	/**
	 *  Liste des codes profil pour lesquels la generation de certificat est desactivee. (balises value)
	 * @return
	 */
	/*public static List<String> getListeCertScolProfilDesactive() {
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.certScolProfilDesactive");
		if(StringUtils.hasText(value)){
			for(String s : value.split(",")){
				values.add(s);
			}
		}
		return values;
	}*/
	
	/**
	 * Liste des codes CGE pour lesquels la generation de certificat est desactivee. (balises value) 
	 * @return
	 */
	/*public static List<String> getListeCertScolCGEDesactive() {
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.certScolCGEDesactive");
		if(StringUtils.hasText(value)){
			for(String s : value.split(",")){
				values.add(s);
			}
		}
		return values;
	}*/
	
	/**
	 * Liste des codes composante pour lesquels la generation de certificat est desactivee. (balises value) 
	 * @return
	 */
	/*public static List<String> getListeCertScolCmpDesactive() {
		LinkedList<String> values = new LinkedList<String>();
		String value = System.getProperty("context.certScolCmpDesactive");
		if(StringUtils.hasText(value)){
			for(String s : value.split(",")){
				values.add(s);
			}
		}
		return values;
	}*/
	
	

	
	
	
	/**
	 *  Affichage ou non des informations crédits ECTS par la page du détail des notes
	 * @return
	 */
	/*public static boolean isAffECTSEtudiant() {
		if(StringUtils.hasText(System.getProperty("context.affECTSEtudiant"))
				&& System.getProperty("context.affECTSEtudiant").equals("true")){
			return true;
		}
		return false;
	}*/
	
	
	
	
	/**
	 *  true pour que l'étudiant puisse modifier son adresse. False sinon
	 * @return
	 */
	/*public static boolean isModificationAdressesAutorisee() {
		if(StringUtils.hasText(System.getProperty("context.modificationAdresses"))
				&& System.getProperty("context.modificationAdresses").equals("true")){
			return true;
		}
		return false;
	}*/
	

	
	/**
	 *  true pour que l'étudiant puisse modifier son mail et son tel perso
	 * @return
	 */
	/*public static boolean isModificationCoordonneesPersoAutorisee() {
		if(StringUtils.hasText(System.getProperty("context.modificationCoordonneesContactPerso"))
				&& System.getProperty("context.modificationCoordonneesContactPerso").equals("true")){
			return true;
		}
		return false;
	}*/
	
	
	/**
	 * 
	 * @return le code du signataire des certificats de scolarité
	 */
	/*public static String getCertScolCodeSignataire() {
		String value = System.getProperty("context.certScolCodeSignataire");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	/**
	 *  true pour ajouter le logo de l'université dans les certificats de scolarité générés.
	 * @return
	 */
	/*public static boolean isCertScolUtiliseLogo() {
		if(StringUtils.hasText(System.getProperty("context.certScolUtiliseLogo"))
				&& System.getProperty("context.certScolUtiliseLogo").equals("true")){
			return true;
		}
		return false;
	}*/
	
	
	
	/**
	 * 
	 * @return  url vers le logo de l'université pour le pdf. A laisser vide pour ne pas importer de logo.
	 */
/*	public static String getLogoUniversitePdf() {
		String value = System.getProperty("context.logoUniversitePdf");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	
	
	/**
	 * 
	 * @return   url vers le header de l'université pour le certificats de scolarité (1240x176). A laisser vide pour ne pas importer de logo.
	 */
	/*public static String getCertScolHeaderUniv() {
		String value = System.getProperty("context.certScolHeaderUniv");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	/**
	 * 
	 * @return   url vers le footer pour le certificats de scolarité (1240x286). A laisser vide pour ne pas importer de logo
	 */
	/*public static String getCertScolFooter() {
		String value = System.getProperty("context.certScolFooter");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	/**
	 * 
	 * @return    Lieu d'édition des certificats de scolarité
	 */
	/*public static String getCertScolLieuEdition() {
		String value = System.getProperty("context.certScolLieuEdition");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	
	
	
	/**
	 * 
	 * @return    Tampon des certificats de scolarité 
	 */
	/*public static String getCertScolTampon() {
		String value = System.getProperty("context.certScolTampon");
		if(StringUtils.hasText(value)) {
			return value;
		}
		return null;
	}*/
	
	
	/**
	 *  Edition pdf des notes : true pour l'activer, false sinon 
	 * @return
	 */
	/*public static boolean isPdfNotesActive() {
		if(StringUtils.hasText(System.getProperty("context.notesPDF"))
				&& System.getProperty("context.notesPDF").equals("true")){
			return true;
		}
		return false;
	}
	*/
	
	/**
	 *  Vrai si on insere un filigrane dans les pdf des notes
	 * @return
	 */
	/*public static boolean isInsertionFiligranePdfNotes() {
		if(StringUtils.hasText(System.getProperty("context.insertionFiligranePdfNotes"))
				&& System.getProperty("context.insertionFiligranePdfNotes").equals("true")){
			return true;
		}
		return false;
	}*/
	
	
	
	/**
	 *  Affichage du numéro de place dans le calendrier des examens : true pour l'activer, false sinon
	 * @return
	 */
	/*public static boolean isAffNumPlaceExamen() {
		if(StringUtils.hasText(System.getProperty("context.affNumPlaceExamen"))
				&& System.getProperty("context.affNumPlaceExamen").equals("true")){
			return true;
		}
		return false;
	}*/
}
