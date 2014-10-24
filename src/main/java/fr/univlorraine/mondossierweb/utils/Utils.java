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

	public static final String TYPE_VET = "Version d'étape";

	public static final String TYPE_CMP = "Composante";

	public static final String TYPE_ETU = "Etudiant";
	
	public static final String TYPE_VDI = "Version de diplôme";
	
	public static final String LIBELLE_WS_INSCRIPTION_PAYEE ="Paiement effectué";
	
	
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

}

