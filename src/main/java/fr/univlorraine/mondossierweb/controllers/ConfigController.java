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

import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationCategorie;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationValeurs;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationCategorieRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationValeursRepository;
import fr.univlorraine.mondossierweb.repositories.mdw.UtilisateurSwapRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Gestion de la config en base de données
 */
@Component
public class ConfigController {

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
	
	public boolean isCertificatScolaritePdfMobile() {
		return getBooleanValueForParameter("certificatScolaritePdfMobile");
	}
	
	public boolean isAttestationAffiliationSSO(){
		return getBooleanValueForParameter("attestationAffiliationSSO");
	}
	
	public boolean isAttestSsoAutoriseGestionnaire(){
		return getBooleanValueForParameter("attestSsoAutoriseGestionnaire");
	}
	
	public boolean isAttestSsoAutoriseEnseignant(){
		return getBooleanValueForParameter("attestSsoAutoriseEnseignant");
	}

	public boolean isCertificatScolariteTouteAnnee() {
		return getBooleanValueForParameter("certificatScolariteTouteAnnee");
	}

	public boolean isCertScolAutoriseGestionnaire() {
		return getBooleanValueForParameter("certScolAutoriseGestionnaire");
	}
	
	public boolean isCertScolAutoriseEnseignant() {
		return getBooleanValueForParameter("certScolAutoriseEnseignant");
	}

	public boolean isCertificatScolaritePiecesNonValidees() {
		return getBooleanValueForParameter("certificatScolaritePJinvalide");
	}
	
	public boolean isCertificatScolariteDossierNonValide() {
		return getBooleanValueForParameter("certificatScolariteDossierNonValide");
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
	
	public boolean isProfilGestionnaireActif() {
		return getBooleanValueForParameter("profilGestionnaireActif");
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
	
	public boolean isAffResultatsEpreuves(){
		return getBooleanValueForParameter("affResultatsEpreuves");
	}

	public boolean isAffBtnCertifNouvelleLigne(){
		return getBooleanValueForParameter("afficherBoutonCertifNouvelleLigne");
	}
	
	public boolean isAffBtnCertificatCouleur() {
		return getBooleanValueForParameter("afficherBoutonCertifCouleur");
	}
	
	public boolean isAffBtnQuittanceCouleur() {
		return getBooleanValueForParameter("afficherBoutonQuittanceCouleur");
	}
	
	public boolean isAffBtnAttestSsoNouvelleLigne(){
		return getBooleanValueForParameter("afficherBoutonAttestSsoNouvelleLigne");
	}
	
	public boolean isQuittanceDossierNonValide() {
		return getBooleanValueForParameter("quittanceDossierNonValide");
	}

	public boolean isQuittanceDroitsPayes(){
		return getBooleanValueForParameter("quittanceDroitsPayes");
	}
	
	public boolean isQuittanceDroitsPayesAutoriseGestionnaire(){
		return getBooleanValueForParameter("quittanceDroitsPayesAutoriseGestionnaire");
	}
	
	public boolean isQuittanceDroitsPayesAutoriseEnseignant(){
		return getBooleanValueForParameter("quittanceDroitsPayesAutoriseEnseignant");
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
	
	public boolean isAffECTSIPEtudiant() {
		return getBooleanValueForParameter("affECTSIPEtudiant");
	}

	public boolean isMasqueECTSnull() {return getBooleanValueForParameter("masqueECTSnull"); }
	
	public boolean isMasqueSession2Vide() {
		return getBooleanValueForParameter("masqueSession2Vide");
	}
	
	public boolean isRenommeSession1Unique() {
		return getBooleanValueForParameter("renommeSession1Unique");
	}
	
	public boolean isMasqueECTSEtudiant() {
		return getBooleanValueForParameter("masqueECTSEtudiant");
	}
	
	public boolean isMasqueECTSIPEtudiant() {
		return getBooleanValueForParameter("masqueECTSIPEtudiant");
	}

	public boolean isModificationAdressesAutorisee() {
		return getBooleanValueForParameter("modificationAdresses");
	}

	public boolean isModificationAdresseAnnuelleAutorisee() { return getBooleanValueForParameter("modificationAdresseAnnuelle"); }
	
	public boolean isModificationTelephoneAutorisee() {
		return getBooleanValueForParameter("modificationTelephone");
	}
	
	public boolean isAffNumerosAnonymat() {
		return getBooleanValueForParameter("affNumerosAnonymat");
	}

	public boolean isModificationCoordonneesPersoAutorisee() {
		return getBooleanValueForParameter("modificationCoordonneesContactPerso");
	}

	public boolean isAffMentionEtudiant() {
		return getBooleanValueForParameter("affMentionEtudiant");
	}
	
	public boolean isAffMentionElpEtudiant() {
		return getBooleanValueForParameter("affMentionElpEtudiant");
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
	
	public boolean isCertScolRegimeIns(){
		return getBooleanValueForParameter("certScolRegimeIns");
	}
	
	public boolean isAffiliationSsoUsageEtatCivil() {
		return getBooleanValueForParameter("attestationAffiliationSSOUsageEtatCivil");
	}

	
	public boolean isQuittanceDroitsPayesUsageEtatCivil() {
		return getBooleanValueForParameter("quittanceDroitsPayesUsageEtatCivil");
	}
	
	public boolean isNotesPDFUsageEtatCivil() {
		return getBooleanValueForParameter("notesPDFUsageEtatCivil");
	}
	
	public boolean isCertScolUsageEtatCivil() {
		return getBooleanValueForParameter("certScolUsageEtatCivil");
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

	public boolean isAffAdressesGestionnaire() {
		return getBooleanValueForParameter("affAdressesGestionnaire");
	}
	
	public boolean isAffAdressesEnseignant() {
		return getBooleanValueForParameter("affAdressesEnseignant");
	}

	public boolean isAffInfosAnnuellesGestionnaire() {
		return getBooleanValueForParameter("affInfosAnnuellesGestionnaire");
	}
	
	public boolean isAffInfosAnnuellesEnseignant() {
		return getBooleanValueForParameter("affInfosAnnuellesEnseignant");
	}
	
	public boolean isAffBoursierGestionnaire() {
		return getBooleanValueForParameter("affBoursierGestionnaire");
	}
	
	public boolean isAffBoursierEnseignant() {
		return getBooleanValueForParameter("affBoursierEnseignant");
	}
	
	public boolean isAffSalarieGestionnaire() {
		return getBooleanValueForParameter("affSalarieGestionnaire");
	}
	
	public boolean isAffSalarieEnseignant() {
		return getBooleanValueForParameter("affSalarieEnseignant");
	}
	
	public boolean isAffAmenagementGestionnaire() {
		return getBooleanValueForParameter("affAmenagementGestionnaire");
	}
	
	public boolean isAffAmenagementEnseignant() {
		return getBooleanValueForParameter("affAmenagementEnseignant");
	}
	
	public boolean isAffInfosContactGestionnaire() {
		return getBooleanValueForParameter("affInfosContactGestionnaire");
	}

	public boolean isAffInfosContactEnseignant() {
		return getBooleanValueForParameter("affInfosContactEnseignant");
	}

	public boolean isAffCalendrierEpreuvesGestionnaire() {
		return getBooleanValueForParameter("affCalendrierEpreuvesGestionnaire");
	}
	
	public boolean isAffCalendrierEpreuvesEnseignant() {
		return getBooleanValueForParameter("affCalendrierEpreuvesEnseignant");
	}

	public boolean isAffCalendrierEpreuvesEtudiant() {
		return getBooleanValueForParameter("affCalendrierEpreuvesEtudiant");
	}
	
	public boolean isAffNotesEnseignant() {
		return getBooleanValueForParameter("affNotesEnseignant");
	}
	
	public boolean isAffNotesEtudiant() {
		return getBooleanValueForParameter("affNotesEtudiant");
	}
	
	public boolean isAffNotesGestionnaire() {
		return getBooleanValueForParameter("affNotesGestionnaire");
	}

	public boolean isAffInfoNaissanceEnseignant() {
		return getBooleanValueForParameter("affInfoNaissanceEnseignant");
	}
	public boolean isAffInfoNaissanceGestionnaire() {
		return getBooleanValueForParameter("affInfoNaissanceGestionnaire");
	}

	public boolean isCertificatScolariteCarteEditee() {
		return getBooleanValueForParameter("certificatScolariteEditionCarte");
	}

	public boolean isAffResAdmissibilite() {
	     return getBooleanValueForParameter("affResultatsAdmissibilite");
    }
	public boolean isAffichageDateNaissancePdfNotesPaysage() {
		return getBooleanValueForParameter("affDateNaissancePdfNotesPaysage");
	}
	public boolean isAffichageDateNaissancePdfNotesPortrait() {
		return getBooleanValueForParameter("affDateNaissancePdfNotesPortrait");
	}														

	public boolean isAffichageNNEPdfNotesPortrait() { return getBooleanValueForParameter("affNNEPdfNotesPortrait"); }																											  
	
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
	
	public List<String> getListeCodesBlocageAccesApplication(){
		return getListValeurForParameter("codesBlocageAccesApplication");
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


	public String getTemoinEtatIaeNotesGestionnaire() {
		return getValeurForParameter("temoinEtatIaeNotesGestionnaire");
	}
	
	public String getTemoinEtatIaeNotesEnseignant() {
		return getValeurForParameter("temoinEtatIaeNotesEnseignant");
	}

	public String getTemoinEtatIaeNotesEtudiant() {
		return getValeurForParameter("temoinEtatIaeNotesEtudiant");
	}
	
	/*
	public float getQuittancePdfPositionSignatureX() {
		return Float.parseFloat(getQuittancePdfPositionSignature().split("-")[0]);
	}
	
	public float getQuittancePdfPositionSignatureY() {
		return Float.parseFloat(getQuittancePdfPositionSignature().split("-")[1]);
	}
	
	private String getQuittancePdfPositionSignature() {
		return getValeurForParameter("quittancePdfPositionSignature");
	}*/
	
	
	public String getCertificatScolaritePdfPositionSignature() {
		return getValeurForParameter("certificatScolaritePdfPositionSignature");
	}
	
	public float getCertificatScolaritePdfPositionSignatureX() {
		return Float.parseFloat(getCertificatScolaritePdfPositionSignature().split("-")[0]);
	}
	
	public float getCertificatScolaritePdfPositionSignatureY() {
		return Float.parseFloat(getCertificatScolaritePdfPositionSignature().split("-")[1]);
	}
	
	
	public String getLogoUniversitePdfPortraitPosition() {
		return getValeurForParameter("logoUniversitePdfPortraitPosition");
	}
	
	public float getLogoUniversitePdfPortraitPositionX() {
		return Float.parseFloat(getLogoUniversitePdfPortraitPosition().split("-")[0]);
	}
	
	public float getLogoUniversitePdfPortraitPositionY() {
		return Float.parseFloat(getLogoUniversitePdfPortraitPosition().split("-")[1]);
	}
	
	
	public String getNotesPDFLogoUniversitePosition() {
		return getValeurForParameter("notesPDFLogoUniversitePosition");
	}
	
	public float getNotesPDFLogoUniversitePositionX() {
		return Float.parseFloat(getNotesPDFLogoUniversitePosition().split("-")[0]);
	}
	
	public float getNotesPDFLogoUniversitePositionY() {
		return Float.parseFloat(getNotesPDFLogoUniversitePosition().split("-")[1]);
	}
	
	
	
	public String getLogoUniversitePdfPaysagePosition() {
		return getValeurForParameter("logoUniversitePdfPaysagePosition");
	}
	
	public float getLogoUniversitePdfPaysagePositionX() {
		return Float.parseFloat(getLogoUniversitePdfPaysagePosition().split("-")[0]);
	}
	
	public float getLogoUniversitePdfPaysagePositionY() {
		return Float.parseFloat(getLogoUniversitePdfPaysagePosition().split("-")[1]);
	}
	
	
	
	
	
	
	
	public String getCertScolTamponPosition() {
		return getValeurForParameter("certScolTamponPosition");
	}
	
	public float getCertScolTamponPositionX() {
		return Float.parseFloat(getCertScolTamponPosition().split("-")[0]);
	}
	
	public float getCertScolTamponPositionY() {
		return Float.parseFloat(getCertScolTamponPosition().split("-")[1]);
	}
	
	public String getTemoinNotesEtudiant() {
		return getValeurForParameter("temoinNotesEtudiant");
	}


	public String getTemoinNotesGestionnaire() {
		return getValeurForParameter("temoinNotesGestionnaire");
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

	public boolean isQuittancePdfSignature() {
		return getBooleanValueForParameter("quittancePdfSignature");
	}
	
	public boolean isQuittancePdfNaissance() {
		return getBooleanValueForParameter("quittancePdfNaissance");
	}

	public String getQuittanceCodeSignataire() {
		return getValeurForParameter("quittanceCodeSignataire");
	}

	public boolean isQuittanceSignatureTampon() {
		return getBooleanValueForParameter("quittanceSignatureTampon");
	}
	
	public String getCertScolCodeSignataire() {
		return getValeurForParameter("certScolCodeSignataire");
	}
	
	public String getQuittanceDescSignataire() {
		return getValeurForParameter("quittanceDescSignataire");
	}
	
	public String getCertScolDescSignataire() {
		return getValeurForParameter("certScolDescSignataire");
	}
	
	public String getLogoUniversitePdf() {
		return getValeurForParameter("logoUniversitePdf");
	}

	public String getLogoUniversiteMobile() {
		return getValeurForParameter("logoUniversiteMobile");
	}
	public String getLogoUniversiteEns() {
		return getValeurForParameter("logoUniversiteEns");
	}
	public String getLogoUniversiteEtu() {
		return getValeurForParameter("logoUniversiteEtu");
	}
	public int getLogoUniversitePdfDimension(){
		return  Integer.parseInt(getValeurForParameter("logoUniversitePdfDimension"));
	}
	
	public String getHeaderPdf() {
		return getValeurForParameter("headerPdf");
	}

	public String getFooterPdf() {
		return getValeurForParameter("footerPdf");
	}
	
	public int getDimensionPDFHeaderFooter(){
		return  Integer.parseInt(getValeurForParameter("dimensionPDFHeaderFooter"));
	}

	public String getCertScolLieuEdition() {
		return getValeurForParameter("certScolLieuEdition");
	}

	public String getCertScolTampon() {
		return getValeurForParameter("certScolTampon");
	}
	
	public int getCertScolTamponDimension(){
		return  Integer.parseInt(getValeurForParameter("certScolTamponDimension"));
	}

	public int getDimensionPDFSignature(){
		return  Integer.parseInt(getValeurForParameter("dimensionPDFSignature"));
	}
	
	public int getNotePDFSignatureDimension() {
		return  Integer.parseInt(getValeurForParameter("notePDFSignatureDimension"));
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
	public String getNotesAnneePivotExtractionApogee() {
		return  getValeurForParameter("notesAnneePivotExtractionApogee");
	}
	public boolean isIndentNiveauElpMobile() {
		return  getBooleanValueForParameter("indentNiveauElpMobile");
	}
	public boolean isNotesAnneeOuverteResExtractionApogee() {
		return getBooleanValueForParameter("notesAnneeOuverteResExtractionApogee");
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
	
	public String[] getHeaderColorPdf() {
		String color = getValeurForParameter("headerColorPdf");
		return color.split(",");
	}

	public String getRegexMailUniv() {
		return getValeurForParameter("regexMailUniv");
	}
	
	public int getNbAnneesInfosAnnuelles() {
		return Integer.parseInt(getValeurForParameter("nbAnneesInfosAnnuelles"));
	}
	
	
	public boolean isSignaturePdfCalendrier() {
		return getBooleanValueForParameter("calendrierSignature");
	}
	public boolean isSignaturePdfCertificat() {
		return getBooleanValueForParameter("certificatSignature");
	}
	public boolean isSignaturePdfQuittance() {
		return getBooleanValueForParameter("quittanceSignature");
	}
	public boolean isSignaturePdfResumeNote() {
		return getBooleanValueForParameter("resumeNoteSignature");
	}
	public boolean isSignaturePdfDetailNote() {
		return getBooleanValueForParameter("detailNoteSignature");
	}
	public boolean isAffInscriptionsAutreCursus()  { return getBooleanValueForParameter("affInscriptionsAutreCursus"); 	}
	public boolean isSignatureAltPdfCalendrier() {
		return getBooleanValueForParameter("calendrierSignatureAlt");
	}
	public boolean isSignatureAltPdfCertificat() {
		return getBooleanValueForParameter("certificatSignatureAlt");
	}
	public boolean isSignatureAltPdfQuittance() {
		return getBooleanValueForParameter("quittanceSignatureAlt");
	}
	public boolean isSignatureAltPdfResumeNote() {
		return getBooleanValueForParameter("resumeNoteSignatureAlt");
	}
	public boolean isSignatureAltPdfDetailNote() {
		return getBooleanValueForParameter("detailNoteSignatureAlt");
	}
	
	public String[] getSignatureAltPositionCalendrier() {
		return getSplitValuesForParameter("calendrierSignatureAltPosition");
	}
	public String[] getSignatureAltPositionCertificat() {
		return getSplitValuesForParameter("certificatSignatureAltPosition");
	}
	public String[] getSignatureAltPositionQuittance() {
		return getSplitValuesForParameter("quittanceSignatureAltPosition");
	}
	public String[] getSignatureAltPositionResumeNote() {
		return getSplitValuesForParameter("resumeNoteSignatureAltPosition");
	}
	public String[] getSignatureAltPositionDetailNote() {
		return getSplitValuesForParameter("detailNoteSignatureAltPosition");
	}

	
	private String[] getSplitValuesForParameter(String parameter) {
		String value = getValeurForParameter(parameter);
		if(!StringUtils.hasText(value)) return null;
		return value.split("-");
	}

	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type liste de valeurs
	 */
	private List<String> getListValeurForParameter(String parameter){
		LinkedList<String> values = new LinkedList<String>();
		PreferencesApplication pa = preferencesApplicationRepository.findById(parameter).orElse(null);
		if(pa!=null && pa.getPrefId()!=null){
			if(pa.getPreferencesApplicationValeurs()!=null && !pa.getPreferencesApplicationValeurs().isEmpty()){
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
		PreferencesApplication pa = preferencesApplicationRepository.findById(parameter).orElse(null);
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
		return utilisateurSwapRepository.findById(login).orElse(null);
	}

	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type booleen
	 */
	private boolean getBooleanValueForParameter(String parameter){
		PreferencesApplication pa = preferencesApplicationRepository.findById(parameter).orElse(null);
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
