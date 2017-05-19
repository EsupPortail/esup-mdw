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
package fr.univlorraine.mondossierweb.controllers;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationCategorie;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationValeurs;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationCategorieRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationValeursRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.UtilisateurSwapRepository;

/**
 * Gestion de la config en base de données
 */
@Component
public class ConfigController {

	private Logger LOG = LoggerFactory.getLogger(ConfigController.class);


	@Resource
	private PreferencesApplicationRepository preferencesApplicationRepository;

	@Resource
	private PreferencesApplicationValeursRepository preferencesApplicationValeursRepository;

	@Resource
	private PreferencesApplicationCategorieRepository preferencesApplicationCategorieRepository;

	@Resource
	private UtilisateurSwapRepository utilisateurSwapRepository;


	public List<PreferencesApplicationCategorie> getCategories(){
		return preferencesApplicationCategorieRepository.findAll();
	}
	
	public List<PreferencesApplicationCategorie> getCategoriesOrderByOrdre(){
		return preferencesApplicationCategorieRepository.findAllByOrderByOrdreAsc();
	}

	public boolean isCertificatScolaritePDF() {
		return getBooleanValueForParameter("certificatScolaritePDF");
	}
	
	public boolean isAttestationAffiliationSSO(){
		return getBooleanValueForParameter("attestationAffiliationSSO");
	}
	public boolean isAttestSsoAutorisePersonnel(){
		return getBooleanValueForParameter("attestSsoAutorisePersonnel");
	}

	public boolean isCertificatScolariteTouteAnnee() {
		return getBooleanValueForParameter("certificatScolariteTouteAnnee");
	}

	public boolean isCertScolAutorisePersonnel() {
		return getBooleanValueForParameter("certScolAutorisePersonnel");
	}

	public boolean isCertificatScolaritePiecesNonValidees() {
		return getBooleanValueForParameter("certificatScolaritePJinvalide");
	}

	public boolean isApplicationActive() {
		return getBooleanValueForParameter("applicationActive");
	}

	public boolean isApplicationMobileActive() {
		return getBooleanValueForParameter("applicationMobileActive");
	}

	public boolean isPartieEnseignantActive() {
		return getBooleanValueForParameter("partieEnseignantActive");
	}

	public boolean isPartieEtudiantActive() {
		return getBooleanValueForParameter("partieEtudiantActive");
	}

	public List<String> getListeLoginsBloques() {
		return getListValeurForParameter("listeLoginsBloques");

	}

	public List<String> getListeCertScolTypDiplomeDesactive() {
		return getListValeurForParameter("certScolTypDiplomeDesactive");

	}

	public boolean isAffRangEtudiant() {
		return getBooleanValueForParameter("afficherRangEtudiant");
	}

	public boolean isAffBtnCertifNouvelleLigne(){
		return getBooleanValueForParameter("afficherBoutonCertifNouvelleLigne");
	}
	
	public boolean isAffBtnAttestSsoNouvelleLigne(){
		return getBooleanValueForParameter("afficherBoutonAttestSsoNouvelleLigne");
	}
	
	public boolean isQuittanceDroitsPayes(){
		return getBooleanValueForParameter("quittanceDroitsPayes");
	}
	public boolean isQuittanceDroitsPayesAutorisePersonnel(){
		return getBooleanValueForParameter("quittanceDroitsPayesAutorisePersonnel");
	}
	
	public boolean isAffBtnQuittanceDroitsPayesNouvelleLigne(){
		return getBooleanValueForParameter("afficherBoutonQuittanceNouvelleLigne");
	}

	public boolean isLogoutCasPropose() {
		return getBooleanValueForParameter("logoutCasPropose");
	}

	public boolean isAffECTSEtudiant() {
		return getBooleanValueForParameter("affECTSEtudiant");
	}

	public boolean isModificationAdressesAutorisee() {
		return getBooleanValueForParameter("modificationAdresses");
	}

	public boolean isModificationCoordonneesPersoAutorisee() {
		return getBooleanValueForParameter("modificationCoordonneesContactPerso");
	}

	public boolean isAffMentionEtudiant() {
		return getBooleanValueForParameter("affMentionEtudiant");
	}
	
	public boolean isAffMessageNotesInformatives() {
		return getBooleanValueForParameter("affMessageNotesInformatives");
	}

	public boolean isTemNotesEtuSem() {
		return getBooleanValueForParameter("temNotesEtuSem");
	}

	public boolean isToujoursAfficherBareme() {
		return getBooleanValueForParameter("affBaremeEtudiant");
	}

	public boolean isCertScolUtiliseLogo() {
		return getBooleanValueForParameter("certScolUtiliseLogo");
	}

	public boolean isPdfNotesActive() {
		return getBooleanValueForParameter("notesPDF");
	}

	public boolean isInsertionFiligranePdfNotes() {
		return getBooleanValueForParameter("insertionFiligranePdfNotes");
	}

	public boolean isAffNumPlaceExamen() {
		return getBooleanValueForParameter("affNumPlaceExamen");
	}

	public boolean isAffDetailExamen() {
		return getBooleanValueForParameter("affDetailExamen");
	}

	public boolean isAffAdresseEnseignants() {
		return getBooleanValueForParameter("affAdressesEnseignants");
	}

	public boolean isAffInfosAnnuellesEnseignants() {
		return getBooleanValueForParameter("affInfosAnnuellesEnseignants");
	}

	public boolean isAffInfosContactEnseignants() {
		return getBooleanValueForParameter("affInfosContactEnseignants");
	}

	public boolean isAffCalendrierEpreuvesEnseignants() {
		return getBooleanValueForParameter("affCalendrierEpreuvesEnseignants");
	}

	public boolean isAffCalendrierEpreuvesEtudiants() {
		return getBooleanValueForParameter("affCalendrierEpreuvesEtudiants");
	}


	public List<String> getListeCertScolProfilDesactive(){
		return getListValeurForParameter("certScolProfilDesactive");
	}

	public List<String> getListeCertScolCGEDesactive() {
		return getListValeurForParameter("certScolCGEDesactive");
	}


	public List<String> getListeCertScolCmpDesactive() {
		return getListValeurForParameter("certScolCmpDesactive");
	}

	public List<String> getListeCertScolStatutDesactive(){
		return getListValeurForParameter("certScolStatutDesactive");
	}

	public List<String> getListeCodesEtapeAffichageRang() {
		return getListValeurForParameter("codesEtapeAffichageRang");
	}
	
	public List<String> getListeCodesBlocageAffichageNotes(){
		return getListValeurForParameter("codesBlocageAffichageNotes");
	}

	public List<String> getTypesEpreuveAffichageNote() {
		return getListValeurForParameter("typesEpreuveAffichageNote");
	}


	public String getAssistanceDocUrl() {
		return getValeurForParameter("assistanceDocUrl");
	}


	public String getAssistanceHelpdeskUrl() {
		return getValeurForParameter("assistanceHelpdeskUrl");
	}

	public String getAssistanceContactMail() {
		return getValeurForParameter("assistanceContactMail");
	}

	public String getTemoinEtatIaeNotesEnseignant() {
		return getValeurForParameter("temoinEtatIaeNotesEnseignant");
	}

	public String getTemoinEtatIaeNotesEtudiant() {
		return getValeurForParameter("temoinEtatIaeNotesEtudiant");
	}

	public String getTemoinNotesEtudiant() {
		return getValeurForParameter("temoinNotesEtudiant");
	}


	public String getTemoinNotesEnseignant() {
		return getValeurForParameter("temoinNotesEnseignant");
	}

	public String getTemoinCtlValCadEpr() {
		return getValeurForParameter("temoinCtlValCadEpr");
	}

	public String getTemoinFictif() {
		return getValeurForParameter("temoinFictif");
	}

	public String getCertScolCodeSignataire() {
		return getValeurForParameter("certScolCodeSignataire");
	}

	public String getLogoUniversitePdf() {
		return getValeurForParameter("logoUniversitePdf");
	}

	public String getCertScolHeaderUniv() {
		return getValeurForParameter("certScolHeaderUniv");
	}

	public String getCertScolFooter() {
		return getValeurForParameter("certScolFooter");
	}

	public String getCertScolLieuEdition() {
		return getValeurForParameter("certScolLieuEdition");
	}

	public String getCertScolTampon() {
		return getValeurForParameter("certScolTampon");
	}

	public int getTrombiMobileNbEtuParPage(){
		return  Integer.parseInt(getValeurForParameter("trombiMobileNbEtuParPage"));
	}

	public String getExtensionMailEtudiant(){
		return getValeurForParameter("extensionMailEtudiant");
	}
	public int getNotesNombreAnneesExtractionApogee() {
		return  Integer.parseInt(getValeurForParameter("notesNombreAnneesExtractionApogee"));
	}
	public boolean isAffichagePdfNotesFormatPortrait() {
		return getBooleanValueForParameter("notesPDFFormatPortrait");
	}
	public boolean isNotesPDFsignature() {
		return getBooleanValueForParameter("notesPDFSignature");
	}
	public String getNotesPDFLieuEdition() {
		return getValeurForParameter("notesPDFLieuEdition");
	}



	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type liste de valeurs
	 */
	private List<String> getListValeurForParameter(String parameter){
		LinkedList<String> values = new LinkedList<String>();
		PreferencesApplication pa = preferencesApplicationRepository.findOne(parameter);
		if(pa!=null && pa.getPrefId()!=null){
			if(pa.getPreferencesApplicationValeurs()!=null && pa.getPreferencesApplicationValeurs().size()>0){
				for(PreferencesApplicationValeurs valeur : pa.getPreferencesApplicationValeurs()){
					values.add(valeur.getValeur());
				}
			}
		}
		return values;
	}

	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type String
	 */
	private String getValeurForParameter(String parameter){
		PreferencesApplication pa = preferencesApplicationRepository.findOne(parameter);
		if(pa!=null && StringUtils.hasText(pa.getValeur())){
			return pa.getValeur();
		}
		return null;
	}


	/**
	 * 
	 * @return les parametres applicatifs en base
	 */
	public List<PreferencesApplication> getAppParameters(){
		return preferencesApplicationRepository.findAll();
	}

	/**
	 * 
	 * @return les parametres applicatifs en base pour une catégorie donnée
	 */
	public List<PreferencesApplication> getAppParametersForCatId(Integer catId){
		return preferencesApplicationRepository.findPreferencesApplicationFromCatId(catId);
	}

	/**
	 * 
	 * @return les parametres applicatifs en base
	 */
	public List<UtilisateurSwap> getSwapUtilisateurs(){
		return utilisateurSwapRepository.findAll();
	}

	/**
	 * 
	 * @return les parametres applicatifs en base
	 */
	public UtilisateurSwap getSwapUtilisateur(String login){
		return utilisateurSwapRepository.findOne(login);
	}

	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type booleen
	 */
	private boolean getBooleanValueForParameter(String parameter){
		PreferencesApplication pa = preferencesApplicationRepository.findOne(parameter);
		if(pa!=null && StringUtils.hasText(pa.getValeur())
				&& pa.getValeur().equals("true")){
			return true;
		}
		return false;
	}

	public void saveAppParameter(PreferencesApplication prefApp) {
		preferencesApplicationRepository.saveAndFlush(prefApp);
	}



	public void saveSwap(UtilisateurSwap swap) {
		utilisateurSwapRepository.saveAndFlush(swap);

	}





}
