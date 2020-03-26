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
package fr.univlorraine.mondossierweb.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;


/**
 * classe qui représente l'étudiant dont on consulte le dossier.
 * @author Charlie Dubois
 */
@Data
public class Etudiant {


	/**
	 * le code individu.
	 */
	private String cod_ind;
	/**
	 * le code etudiant.
	 */
	private String cod_etu;
	/**
	 * le code NNE.
	 */
	private String cod_nne;
	/**
	 * le nom.
	 */
	private String nom;
	/**
	 * code civilité
	 */
	private String codCiv;
	/**
	 * nom à afficher
	 */
	private String nomAffichage;
	/**
	 * Sexe de l'état-civil
	 */
	private String sexEtatCiv;
	/**
	 * Prénom de l'état-civil
	 */
	private String prenomEtatCiv;
	/**
	 * Temoin d'utilisation du prénom d'usage
	 */
	private boolean temPrUsage;
	/**
	 * url de la photo
	 */
	private String photo;
	/**
	 * email
	 */
	private String email;
	/**
	 * lieu de naissance.
	 */
	private String lieunaissance;
	/**
	 * département de naissance.
	 */
	private String departementnaissance;
	/**
	 * date de naissance.
	 */
	private String datenaissance;
	/**la nationalité.
	 * 
	 */
	private String nationalite;
	/**
	 * année de l'inscription universitaire.
	 */
	private String anneeInscriptionUniversitaire;
	/**
	 * établissement de l'inscription universitaire.
	 */
	private String etablissement;
	/**
	 * la liste des bac de l'étudiant.
	 */
	private ArrayList<BacEtatCivil> listeBac;
	/**
	 * mail perso
	 */
	private String emailPerso;
	/**
	 * tel portable
	 */
	private String telPortable;
	/**
	 * Le numéro de boursier
	 */
	private boolean boursier;
	/**
	 * affilié à la sécu pour l'année en cours
	 */
	private boolean affilieSso;
	/**
	 * régime d'inscription pour l'année en cours
	 */
	private String regimeIns;
	/**
	 * vrai si inscrit pour l'année en cours
	 */
	private boolean inscritPourAnneeEnCours;
	/**
	 * vrai si étudiant a un régime d'aménagement d'étude
	 */
	private boolean temAmenagementEtude;
	/**
	 * vrai si étudiant est salarié
	 */
	private boolean temSalarie;
	/**
	 * statut de l'étudiant
	 */
	private String statut;
	/**
	 * adresse annuelle
	 */
	private Adresse adresseAnnuelle;
	/**
	 * adresse fixe
	 */
	private Adresse adresseFixe;
	
	private String anneePremiereInscrip;
	
	private String etbPremiereInscrip;
	
	private String libEtablissement;
	/**
	 * liste des inscription dans l'établissement en cours.
	 */
	private List<Inscription> linsciae;
	/**
	 * liste des autres cursus suivis.
	 */
	private List<Inscription> linscdac;
	/**
	 * liste des examens prévus.
	 */
	private List<Examen> calendrier;
	/**
	 * vrai si on a deja tenté de récupérer le calendrier des examens
	 */
	private boolean calendrierRecupere;
	/**
	 * les diplomes avec les résultats obtenus.
	 */
	private List<Diplome> diplomes;
	/**
	 * les etapes avec les résultats obtenus.
	 */
	private List<Etape> etapes;
	/**
	 * vrai si l'étudiant n'est pas autorisé à consulter ses notes cad qu'il a un blocage spécifique
	 */
	private boolean NonAutoriseConsultationNotes;
	/**
	 * vrai si on affiche le rang de l'étudiant
	 */
	private boolean afficherRang;
	/**
	 * map qui contient les couples (indice,signification) pour les résultats.
	 */
	private Map allSignificationResultats;
	/**
	 * map qui contient les couples (indice,signification) pour les résultats à afficher.
	 */
	private Map significationResultats;
	/**
	 * le cache du résultat et des notes déjà récupérés.
	 */
	private CacheResultats cacheResultats;
	/**
	 * vrai si significationResultats n'est pas vide.
	 */
	private boolean significationResultatsUtilisee;
	/**
	 * la liste des éléments pédagogique (avec résultats) d'une étape choisie.
	 */
	private List<ElementPedagogique> elementsPedagogiques;
	/**
	 * vrai si les résultat à l'épreuve sont définitifs.
	 */
	private boolean deliberationTerminee;
	/**
	 * la liste des numérots d'anonymat pour l'année en cours
	 */
	private List<Anonymat> numerosAnonymat;
	/**
	 * les données d'affiliation à la sécu de l'étudiant 
	 */
	private AffiliationSSO affilition_sso;
	/**
	 * les données de quittance des droits universitaires payés
	 */
	private QuittanceDroitsUniversitaires quittance_sso;
	/**
	 * Vrai si on a réussi à récupérer les informations sur l'affiliation à la sso.
	 */
	private boolean recuperationInfosAffiliationSsoOk;
	/**
	 * Vrai si on a réussi à récupérer les informations sur la quittance des droits payés.
	 */
	private boolean recuperationInfosQuittanceOk;

	
	
	public Etudiant() {
		super();
		initAttributesValues();
	}
	
	
	public Etudiant(String cod_etu) {
		super();
		initAttributesValues();
		this.cod_etu= cod_etu;
	}
	
	private void initAttributesValues(){
		linsciae = new ArrayList<Inscription>();
		linscdac = new ArrayList<Inscription>();
		allSignificationResultats = new HashMap<String, String>();
		significationResultats = new HashMap<String, String>();
		diplomes = new ArrayList<Diplome>();
		etapes = new ArrayList<Etape>();
		cacheResultats = new CacheResultats();
		elementsPedagogiques = new ArrayList<ElementPedagogique>();
		
	}

	/**
	 * initialise l'étudiant car on va en 'charger' un nouveau.
	 * on ne fais pas new Etudiant() car on perdrait l'etudiantManager
	 * renseigné dans le fichier xml de config.
	 *
	 */
	public void reset() {
		
	}
	
	
	public boolean isSignificationResultatsUtilisee() {
		significationResultatsUtilisee = true;
		if (significationResultats==null || significationResultats.isEmpty()) {
			significationResultatsUtilisee = false;
		}
		return significationResultatsUtilisee;
	}

	public void setSignificationResultatsUtilisee(
			boolean significationResultatsUtilisee) {
		this.significationResultatsUtilisee = significationResultatsUtilisee;
	}


}
