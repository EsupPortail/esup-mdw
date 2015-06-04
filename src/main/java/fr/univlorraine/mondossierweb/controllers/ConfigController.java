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
import fr.univlorraine.mondossierweb.repositories.mdw.PreferencesApplicationRepository;

/**
 * Gestion de la config en base de données
 */
@Component
public class ConfigController {

	private Logger LOG = LoggerFactory.getLogger(ConfigController.class);

	
	@Resource
	private PreferencesApplicationRepository preferencesApplicationRepository;


	/**
	 * Edition pdf des certificats de scolarité : true pour l'activer, false sinon
	 * @return
	 */
	public boolean isCertificatScolaritePDF() {
		return getBooleanValueForParameter("certificatScolaritePDF");
	}
	
	public boolean isCertificatScolariteTouteAnnee() {
		return getBooleanValueForParameter("certificatScolariteTouteAnnee");
	}
	
	public boolean isCertScolAutorisePersonnel() {
		return getBooleanValueForParameter("certScolAutorisePersonnel");
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
	
	public List<String> getListeCertScolTypDiplomeDesactive() {
		return getListValeurForParameter("certScolTypDiplomeDesactive");
		
	}
	
	public boolean isAffRangEtudiant() {
		return getBooleanValueForParameter("afficherRangEtudiant");
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
	
	public boolean isTemNotesEtuSem() {
		return getBooleanValueForParameter("temNotesEtuSem");
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
				

	
	public List<String> getListeCertScolProfilDesactive(){
		return getListValeurForParameter("certScolProfilDesactive");
	}
	
	public List<String> getListeCertScolCGEDesactive() {
		return getListValeurForParameter("certScolCGEDesactive");
	}
	
	
	public List<String> getListeCertScolCmpDesactive() {
		return getListValeurForParameter("certScolCmpDesactive");
	}
	
	public List<String> getListeCodesEtapeAffichageRang() {
		return getListValeurForParameter("codesEtapeAffichageRang");
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
		
		
	/**
	 * 
	 * @param parameter
	 * @return la valeur d'un parametre de type liste de valeurs
	 */
	private List<String> getListValeurForParameter(String parameter){
		LinkedList<String> values = new LinkedList<String>();
		PreferencesApplication pa = preferencesApplicationRepository.findOne(parameter);
		if(pa!=null && StringUtils.hasText(pa.getValeur())){
			for(String s : pa.getValeur().split(",")){
				values.add(s);
			}
			return values;
		}
		return null;
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



}
