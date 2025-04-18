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

import com.vaadin.server.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.views.AccesBloqueView;
import fr.univlorraine.mondossierweb.views.AccesRefuseView;
import fr.univlorraine.mondossierweb.views.AdminView;
import fr.univlorraine.mondossierweb.views.AdressesView;
import fr.univlorraine.mondossierweb.views.AssistanceView;
import fr.univlorraine.mondossierweb.views.CalendrierMobileView;
import fr.univlorraine.mondossierweb.views.CalendrierView;
import fr.univlorraine.mondossierweb.views.ErreurView;
import fr.univlorraine.mondossierweb.views.EtatCivilView;
import fr.univlorraine.mondossierweb.views.FavorisMobileView;
import fr.univlorraine.mondossierweb.views.FavorisView;
import fr.univlorraine.mondossierweb.views.InformationsAnnuellesMobileView;
import fr.univlorraine.mondossierweb.views.InformationsAnnuellesView;
import fr.univlorraine.mondossierweb.views.InscriptionsView;
import fr.univlorraine.mondossierweb.views.ListeInscritsMobileView;
import fr.univlorraine.mondossierweb.views.ListeInscritsView;
import fr.univlorraine.mondossierweb.views.NotesDetailMobileView;
import fr.univlorraine.mondossierweb.views.NotesMobileView;
import fr.univlorraine.mondossierweb.views.NotesView;
import fr.univlorraine.mondossierweb.views.RechercheArborescenteView;
import fr.univlorraine.mondossierweb.views.RechercheMobileView;
import fr.univlorraine.mondossierweb.views.RechercheRapideView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Charlie Dubois
 * 
 * Méthodes Utiles
 */
@Slf4j
public class Utils {

	
	/**
	 * type utilisateur admin.
	 */
	public static final String ADMIN_USER = "admin";
	/**
	 * type utilisateur étudiant.
	 */
	public static final String STUDENT_USER = "student";
	/**
	 * type correspondant à un utilisateur dont le login doit être exclu de l'application.
	 */
	public static final String LOGIN_EXCLU = "exclu";

	/**
	 * type utilisateur enseignant.
	 */
	public static final String TEACHER_USER = "teacher";
	
	/**
	 * type utilisateur gestionnaire.
	 */
	public static final String GEST_USER = "gestionnaire";

	/**
	 * type utilisateur non-autorisé.
	 */
	public static final String UNAUTHORIZED_USER = "unauthorized";
	
	
	

	public static final int NB_MAX_RESULT_QUICK_SEARCH=5;

	public static final int NB_MAX_RESULT_SEARCH=70;

	public static final String TYPE_ELP = "Elément pédagogique";

	public static final String ELP = "ELP";

	public static final String TYPE_COL = "Collection de groupes";

	public static final String COL = "COL";

	public static final String TYPE_GRP = "Groupe";

	public static final String GRP = "GRP";

	public static final String TYPE_VET = "Etape";

	public static final String VET = "VET";

	public static final String TYPE_CMP = "Composante";

	public static final String CMP = "CMP";

	public static final String TYPE_ETU = "Etudiant";

	public static final String ETU = "ETU";

	public static final String TYPE_VDI = "Diplôme";

	public static final String VDI = "VDI";

	public static final String ES_TYPE ="TYP_OBJ";

	//public static final String COD_SOC_BOURSIER ="BO";
	
	public static final String ETAT_IAE_EN_COURS ="E";
	
	public static final String ETAT_IAE_ANNULEE ="A";
	
	public static final String TEMOIN_EDITION_CARTE_EDITEE = "E";

	public static final String LIBELLE_WS_INSCRIPTION_PAYEE ="Paiement effectué";

	public static final String SEPARATEUR_CODE_GROUPE = ";";
	
	public static final String SEPARATEUR_VETS = ";";
	
	public static final String FRAGMENT_ACCES_DOSSIER_ETUDIANT = "accesDossierEtudiant";
	
	public static final String FRAGMENT_ACCES_PHOTO = "accesPhoto";

	public static final String SHOW_MESSAGE_NOTES_PREFERENCE= "SHOW_MESSAGE_NOTES";

	public static final String SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE= "SHOW_MESSAGE_MOBILE_NOTES";

	public static final String SHOW_MESSAGE_INTRO_PREFERENCE = "SHOW_MESSAGE_INTRO";

	public static final String SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE = "SHOW_MESSAGE_MOBILE_INTRO";
	
	public static final String PARAM_TYP_BOOLEAN = "BOOLEAN";
	
	public static final String PARAM_TYP_LIST = "LIST";
	
	public static final String PARAM_TYP_STRING = "STRING";
	
	public static final String DATA_IMAGE = "data:image";
	
	public static final String LDAP_ETUDIANT = "LDAP_ETUDIANT";
	
	public static final String LDAP_DOCTORANT = "LDAP_DOCTORANT";
	
	public static final String LDAP_GEST = "LDAP_GEST";
	
	public static final Object PROFIL_GEST = "GEST";
	
	public static final Object PROFIL_ENS = "ENS";

	/** Durée en heure de la durée maxi de validité d'un swap utilisateur */
	public static final int NB_HEURE_DUREE_SWAP_USER = 1;
	
	// témoin session unique dans la table VERSION_ETAPE
	public static final String TEM_SES_UNI = "O";
	
	// témoin session unique retourné par les WS
	public static final String COD_SES_UNI = "0";
	
	public static final String APOGEE_EXTRACTION = "Apogee-extraction";
	
	public static final String APOGEE = "Apogee";

	//liste des vues desktop
	private static final String[] LISTE_VIEWS_DESKTOP = {
		AccesBloqueView.NAME,
		AccesRefuseView.NAME,
		AdminView.NAME,
		AdressesView.NAME,
		AssistanceView.NAME,
		CalendrierView.NAME,
		ErreurView.NAME,
		EtatCivilView.NAME,
		FavorisView.NAME,
		InformationsAnnuellesView.NAME, 
		InscriptionsView.NAME,
		ListeInscritsView.NAME,
		NotesView.NAME,
		RechercheArborescenteView.NAME,
		RechercheRapideView.NAME
	};

	//liste des vues mobiles
	private static final String[] LISTE_VIEWS_MOBILE = {
		AccesBloqueView.NAME,
		AccesRefuseView.NAME,
		CalendrierMobileView.NAME,
		ErreurView.NAME,
		FavorisMobileView.NAME,
		InformationsAnnuellesMobileView.NAME, 
		ListeInscritsMobileView.NAME,
		NotesDetailMobileView.NAME,
		NotesMobileView.NAME,
		RechercheMobileView.NAME};

	//liste des vues enseignants dont l'accès est à protéger
	private static final String[] LISTE_VIEWS_ENSEIGNANT = {RechercheRapideView.NAME,RechercheArborescenteView.NAME, FavorisView.NAME,ListeInscritsView.NAME, AssistanceView.NAME,
		FavorisMobileView.NAME, ListeInscritsMobileView.NAME,RechercheMobileView.NAME};



	

	
	/** formatage d'une Localdate pour ne garder que jour, mois , annee*/
	public static String formatLocalDateTimeToString(LocalDateTime d){
		if(d!=null){
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			return d.format(formatter);
		}else{
			return null;
		}
	}
	
	/** formatage d'une date pour ne garder que jour, mois , annee*/
	public static String formatDateToString(Date d){
		if(d!=null){
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(d);
		}else{
			return null;
		}
	}

	public static String getAnneeUniversitaireEnCours(String annee){
		int anneeEnCours = Integer.parseInt(annee);
		return anneeEnCours+"/"+(anneeEnCours+1);
	}
	
	public static String getEctsToDisplay(BigDecimal ects) {
		String s = ects.toString();
		if(s!=null && s.length()>2 && s.substring(s.length()-2, s.length())!=null && s.substring(s.length()-2, s.length()).equals(".0")){
			s=s.replaceFirst("\\.0", "");
		}
		return s;
	}

	public static String convertTypeToDisplay(String type) {
		if(type!=null){
			if(type.equals(ELP))
				return Utils.TYPE_ELP;
			if(type.equals(CMP))
				return Utils.TYPE_CMP;
			if(type.equals(VDI))
				return Utils.TYPE_VDI;
			if(type.equals(VET))
				return Utils.TYPE_VET;
			if(type.equals(COL))
				return Utils.TYPE_COL;
			if(type.equals(GRP))
				return Utils.TYPE_GRP;
			if(type.equals(ETU))
				return Utils.TYPE_ETU;
		}
		return type;
	}

	public static String convertAnneeUnivToDisplay(String annee){
		if(StringUtils.hasText(annee)){
			if(Pattern.matches("^[0-9]{4}", annee)){
				int anneeUniv = Integer.parseInt(annee);
				anneeUniv++;
				annee += "/"+anneeUniv;
			}
		}
		return annee;
	}


	public static boolean isViewEnseignant(String viewName) {
		for(int i=0;i<LISTE_VIEWS_ENSEIGNANT.length;i++){
			if(LISTE_VIEWS_ENSEIGNANT[i].equals(viewName)){
				return true;
			}
		}
		return false;
	}



	public static boolean isViewDesktop(String viewName) {
		if(viewName!=null && viewName.contains("!")){
			viewName = viewName.replaceAll("!", "");
		}
		for(int i=0;i<LISTE_VIEWS_DESKTOP.length;i++){
			if(LISTE_VIEWS_DESKTOP[i].equals(viewName)){
				return true;
			}
		}
		return false;
	}

	public static boolean isViewMobile(String viewName) {
		if(viewName!=null && viewName.contains("!")){
			viewName = viewName.replaceAll("!", "");
		}
		for(int i=0;i<LISTE_VIEWS_MOBILE.length;i++){
			if(LISTE_VIEWS_MOBILE[i].equals(viewName)){
				return true;
			}
		}
		return false;
	}

	public static boolean getBooleanFromString(String chaine) {
		if(StringUtils.hasText(chaine) && chaine.equals("O")){
			return true;
		}
		return false;
	}

	//Formate bouton/lien pour appli mobile
	public static void setButtonStyle(Component b){
		b.setStyleName("v-nav-button");
		b.addStyleName("link"); 
		b.addStyleName("v-link");
	}

	//retourne vrai si les listes ont une valeur en commun
	public static boolean listHaveCommonValue(List<String> l1,List<String> l2) {
		if(l1 != null && !l1.isEmpty() && l2 != null && !l2.isEmpty()){
			for(String s1 : l1){
				for(String s2: l2){
					if(StringUtils.hasText(s1) && StringUtils.hasText(s2) && s1.equals(s2)){
						return true;
					}
				}
			}
		}
		
		return false;
	}

	//Découpe des string séparés par des points virgules pour retourner une liste de string
	public static List<String> splitStringFromSemiColon(String groupes){
		List<String> lgroupes = new LinkedList<String>();
		if(StringUtils.hasText(groupes)){
			String[] tgroupes = groupes.split(";");
			for(int i=0; i<tgroupes.length;i++){
				lgroupes.add(tgroupes[i]);
			}
		}
		return lgroupes;
	}

	public static Date formatDateFromString(String date) {
		if(StringUtils.hasText(date)){
			date = date.substring(0,10);
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date d;
			try {
				d = formatter.parse(date);
			} catch (ParseException e) {
				log.error("String to date : "+date+" en objet Date");
				return null;
			}
			return d;
		}else{
			return null;
		}
	}

	public static String getLibelleFromComboBox(String codes, ComboBox combobox) {
		StringBuilder bld = new StringBuilder();
		List<String> listeCodes = Utils.splitStringFromSemiColon(codes);
		for(String code : listeCodes){
			if(StringUtils.hasText(bld)){
				bld.append(", ");
			}
			bld.append(combobox.getItemCaption(code));
		}
		return bld.toString();
	}

	public static boolean telephoneValide(String telephone) {
		String pattern = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
		return Pattern.matches(pattern, telephone.replaceAll(" ", ""));
	}

	public static String getDateString() {
		Date d = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.format(d);
	}

    public static void ajoutLogoBandeau(String pathLogo, HorizontalLayout navbar) {
		if(StringUtils.hasText(pathLogo)) {
			FileResource resource = new FileResource(new File(pathLogo));
			Image logo = new Image(null, resource);
			logo.setStyleName("logo-mobile");
			navbar.addComponent(logo);
			navbar.setComponentAlignment(logo, Alignment.MIDDLE_LEFT);
		}
    }
	public static void ajoutLogoBandeauMenu(String pathLogo, CssLayout menu, String titre) {
		if(StringUtils.hasText(pathLogo)) {
			FileResource resource = new FileResource(new File(pathLogo));
			Image logo = new Image(null, resource);
			logo.setStyleName("logo-etu-menu");
			logo.setWidthUndefined();
			HorizontalLayout bandeau = new HorizontalLayout();
			bandeau.setWidthFull();
			HorizontalLayout contenu = new HorizontalLayout();
			contenu.setWidthUndefined();
			contenu.addComponent(logo);
			Label appLabel = new Label(titre);
			appLabel.setWidthFull();
			contenu.addComponent(appLabel);
			contenu.setExpandRatio(appLabel, 1);
			contenu.addStyleName("bandeau-etu-contenu");
			bandeau.addStyleName("bandeau-etu-menu");
			bandeau.addComponent(contenu);
			menu.addComponent(bandeau);
		}
	}

	public static void ajoutLogoBandeauEnseignant(String pathLogo, VerticalLayout vue, String titre) {

		CssLayout bandeau = new CssLayout();
		bandeau.addStyleName("bandeau-ens-menu");
		CssLayout contenu = new CssLayout();
		contenu.setWidthUndefined();
		contenu.addStyleName("contenu-ens-menu");

		if (StringUtils.hasText(pathLogo)) {
			FileResource resource = new FileResource(new File(pathLogo));
			Image logo = new Image(null, resource);
			logo.setStyleName("logo-ens-menu");
			logo.setWidthUndefined();
			contenu.addComponent(logo);
		}
		Label appLabel = new Label(titre);
		appLabel.setWidthFull();
		appLabel.setStyleName("label-ens-menu");
		contenu.addComponent(appLabel);

		bandeau.addComponent(contenu);
		vue.addComponent(bandeau);
	}

    public static boolean isEquals(String s1, String s2) {
		return s1 == null && s2 == null || (s1 != null && s2 != null && s1.equals(s2));
    }
}

