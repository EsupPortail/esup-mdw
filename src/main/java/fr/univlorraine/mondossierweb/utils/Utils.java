package fr.univlorraine.mondossierweb.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Panel;

/**
 * @author Charlie Dubois
 * 
 * Méthodes Utiles
 */
public class Utils {

	static final Logger LOG = LoggerFactory.getLogger(Utils.class);


	public static final String TYPE_ELP = "Elément pédagogique";

	public static final String ELP = "ELP";

	public static final String TYPE_VET = "Etape";

	public static final String VET = "VET";

	public static final String TYPE_CMP = "Composante";

	public static final String CMP = "CMP";

	public static final String TYPE_ETU = "Etudiant";

	public static final String ETU = "ETU";

	public static final String TYPE_VDI = "Diplôme";

	public static final String VDI = "VDI";

	public static final String LIBELLE_WS_INSCRIPTION_PAYEE ="Paiement effectué";

	public static final String SEPARATEUR_CODE_GROUPE = ";";

	public static final String SHOW_MESSAGE_NOTES_PREFERENCE= "SHOW_MESSAGE_NOTES";

	public static final String SHOW_MESSAGE_INTRO_PREFERENCE = "SHOW_MESSAGE_INTRO";

	/** Durée en heure de la durée maxi de validité d'un swap utilisateur */
	public static final int NB_HEURE_DUREE_SWAP_USER = 1;

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
			if(type.equals(ETU))
				return Utils.TYPE_ETU;
		}
		return type;
	}

}

