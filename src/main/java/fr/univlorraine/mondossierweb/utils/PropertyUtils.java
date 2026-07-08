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

import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Accès aux propriétés de configuration de l'application (application.yml).
 * L'{@link Environment} Spring est injecté très tôt au démarrage par {@link PropertyUtilsInitializer}.
 */
public class PropertyUtils {

	public final static String AFFICHAGE_NOM_BASIQUE = "basique";

	public final static String AFFICHAGE_NOM_STANDARD = "standard";

	private static Environment environment;

	static void setEnvironment(final Environment env) {
		environment = env;
	}

	private static String get(final String key) {
		return environment.getProperty(key);
	}

	private static String getRequired(final String key) {
		final String value = get(key);
		if (!StringUtils.hasText(value)) {
			throw new NullPointerException(key + " cannot be null !");
		}
		return value;
	}

	private static String getOrNull(final String key) {
		final String value = get(key);
		return StringUtils.hasText(value) ? value : null;
	}

	private static boolean getBoolean(final String key) {
		return "true".equals(get(key));
	}

	private static List<String> getListOrNull(final String key) {
		final String value = get(key);
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return Arrays.asList(value.split(";"));
	}

	/** Retourne l'url de l'application */
	public static String getAppUrl() {
		return getRequired("app.url");
	}

	/** Retourne l'url de serveur elasticSearch */
	public static String getElasticSearchUrl() {
		return getRequired("param.elasticsearch.url");
	}

	/** Retourne le nom du cluster elasticSearch */
	public static String getElasticSearchCluster() {
		return getRequired("param.elasticsearch.cluster");
	}

	/** Retourne l'index elasticSearch */
	public static String getElasticSearchIndex() {
		return getRequired("param.elasticsearch.index");
	}

	/** Retourne le champ de recherche de l'index elasticSearch */
	public static String getElasticSearchChampRecherche() {
		return getRequired("param.elasticsearch.index.champrecherche");
	}

	/** Retourne le champ de l'index elasticSearch correspondant au code de l'objet */
	public static String getElasticSearchChampCodeObjet() {
		return getRequired("param.elasticsearch.index.champcodeobjet");
	}

	/** Retourne le champ de l'index elasticSearch correspondant à la version de l'objet */
	public static String getElasticSearchChampVersionObjet() {
		return getRequired("param.elasticsearch.index.champversionobjet");
	}

	/** Retourne le champ de l'index elasticSearch correspondant au libellé de l'objet */
	public static String getElasticSearchChampLibelleObjet() {
		return getRequired("param.elasticsearch.index.champlibelleobjet");
	}

	/** Retourne le port ElasticSearch */
	public static int getElasticSearchPort() {
		return Integer.parseInt(getRequired("param.elasticsearch.port"));
	}

	/** Retourne vrai on doit aller voir dans la table utilisateur d'apogée pour gérer les accès à l'appli */
	public static boolean isLoginApogee() {
		return getBoolean("loginApogee");
	}

	/** Retourne vrai si on effectue des tests de dev en local */
	public static boolean isLocalDevModeEnabled() {
		return getBoolean("localDevMode");
	}

	/** Retourne vrai si on doit tester le témoin EN SERVICE de la table utilisateur d'apogée pour gérer les accès à l'appli */
	public static boolean isCheckTesUtilisateurApogee() {
		return getBoolean("checkTesUtilisateurApogee");
	}

	/** Retourne le profil à attribuer aux utilisateurs Apogée (ENS ou GEST) */
	public static String getProfilUtilisateurApogee() {
		final String value = get("profilUtilisateurApogee");
		if (StringUtils.hasText(value)
			&& (value.equals(Utils.PROFIL_ENS) || value.equals(Utils.PROFIL_GEST))) {
			return value;
		}
		return null;
	}

	/** Retourne vrai on doit activer l'encryption sur les pdf générés */
	public static boolean isEnablePdfSecurity() {
		return getBoolean("EnablePdfSecurity");
	}

	/** Retourne vrai on doit affiche le l'indicateur de loading entre certains écrans */
	public static boolean isShowLoadingIndicator() {
		return getBoolean("showLoadingIndicator");
	}

	/** Retourne la clé apogée pour décrypter les blob de la base apogée */
	public static String getClefApogeeDecryptBlob() {
		return getRequired("clefApogeeDecryptBlob");
	}

	/** Retourne le type Etudiant dans Ldap */
	public static List<String> getTypeEtudiantLdap() {
		return Arrays.asList(getRequired("typeEtudiantLdap").split(";"));
	}

	/** Retourne la propriete ldap designant l'uid */
	public static String getAttributLdapUid() {
		return getRequired("ldap.uid.attribute");
	}

	/** Retourne la propriete ldap du contact désignant son type (typeEtudiantLdap ou pas ) */
	public static String getAttributLdapEtudiant() {
		return getRequired("attributLdapEtudiant");
	}

	/** Retourne l'attribut ldap permettant de caractériser un doctorant (l'application vérifira l'accès "enseignant" en priorité ) */
	public static String getAttributLdapDoctorant() {
		return get("attributLdapDoctorant");
	}

	/** Retourne les valeurs de l'attribut ldap permettant de caractériser un doctorant */
	public static List<String> getValeursAttributLdapDoctorant() {
		return getListOrNull("valeursAttributLdapDoctorant");
	}

	/** Retourne l'attribut ldap permettant de caractériser un gestionnaire */
	public static String getAttributLdapGestionnaire() {
		return get("attributLdapGestionnaire");
	}

	/** Retourne les valeurs de l'attribut ldap permettant de caractériser un gestionnaire */
	public static List<String> getValeursAttributLdapGestionnaire() {
		return getListOrNull("valeursAttributLdapGestionnaire");
	}

	/** Retourne la propriete ldap du contact désignant ses groupes */
	public static String getAttributGroupeLdap() {
		return get("attributGroupeLdap");
	}

	/** Retourne la liste des groupes ldap autorisés en tant qu'admin */
	public static List<String> getListeGroupesLdapAdmin() {
		return getListOrNull("listeGroupesLdapAdmin");
	}

	/** Retourne la liste des groupes ldap autorisés */
	public static List<String> getListeGroupesLdapAutorises() {
		return getListOrNull("listeGroupesLdap");
	}

	/** Retourne la propriete ldap du contact désignant son code etudiant */
	public static String getAttributLdapCodEtu() {
		return getRequired("attributLdapCodEtu");
	}

	/** Retourne la liste des groupes uportal autorisés */
	public static List<String> getListeGroupesUportalAutorises() {
		return getListOrNull("uportal.groupes.autorises");
	}

	/** Retourne vrai si les données de l'état-civil doivent être récupérées dans l'annuaire */
	public static boolean isRecupMailAnnuaireApogee() {
		return getBoolean("param.apogee.mail.annuaire");
	}

	public static String getSourceResultats() {
		return getRequired("sourceResultats");
	}

	/** Retourne la liste des erreurs à ignorer */
	public static List<String> getListeErreursAIgnorer() {
		final LinkedList<String> values = new LinkedList<>();
		final String value = get("liste.erreur.a.ignorer");
		if (!StringUtils.hasText(value)) {
			return values;
		}
		values.addAll(Arrays.asList(value.split(";")));
		return values;
	}

	/** Façon d'afficher le nom des étudiants */
	public static String getTypeAffichageNomEtatCivil() {
		return getRequired("etatcivil.nom.affichage");
	}

	/** Retourne le username de l'utilisateur des WebServices Apogée à utiliser pour MDW */
	public static String getApoWsUsername() {
		return getOrNull("apoWsUsername");
	}

	/** Retourne le password de l'utilisateur des WebServices Apogée à utiliser pour MDW */
	public static String getApoWsPassword() {
		return getOrNull("apoWsPassword");
	}

	/** Retourne si oui ou non on affiche le sql des requêtes directes sur Apogée */
	public static boolean getShowSqlApogee() {
		return getBoolean("apoShowSql");
	}

	public static boolean isEnableRechercheAutoElp() {
		return getBoolean("recherche.autocompletion.elp");
	}

	public static String getPdfSignatureKeystorePath() {
		return getOrNull("pdf.sign.keystore.path");
	}

	public static String getPdfSignaturePassword() {
		return getOrNull("pdf.sign.password");
	}

	public static String getPdfSignatureCreator() {
		return getOrNull("pdf.sign.creator");
	}

	public static String getPdfSignatureReason() {
		return getOrNull("pdf.sign.reason");
	}

	public static String getPdfSignatureLocation() {
		return getOrNull("pdf.sign.location");
	}

	public static String getPdfSignatureContact() {
		return getOrNull("pdf.sign.contact");
	}

	public static int getPdfSignatureVisibleLlX(String[] position) {
		return Integer.parseInt(position[0]);
	}

	public static int getPdfSignatureVisibleLlY(String[] position) {
		return Integer.parseInt(position[1]);
	}

	public static int getPdfSignatureVisibleUrX(String[] position) {
		return Integer.parseInt(position[2]);
	}

	public static int getPdfSignatureVisibleUrY(String[] position) {
		return Integer.parseInt(position[3]);
	}

	public static String getPdfSignatureTsaUrl() {
		return getOrNull("pdf.sign.alt.tsa.url");
	}

	public static String getPdfSignatureTsaUsername() {
		return getOrNull("pdf.sign.alt.tsa.username");
	}

	public static String getPdfSignatureTsaPassword() {
		return getOrNull("pdf.sign.alt.tsa.password");
	}

	public static int getPdfSignatureTsaTokenSize() {
		return Integer.parseInt(getRequired("pdf.sign.alt.tsa.tokensize"));
	}

	public static int getRateLimiterMaxRequest() {
		final String value = get("ratelimiter.maxrequest");
		if (!StringUtils.hasText(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}
}
