package fr.univlorraine.mondossierweb;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;

import org.springframework.util.StringUtils;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.mondossierweb.photo.PhotoNancy2ImplCodEtu;
import fr.univlorraine.tools.vaadin.GoogleAnalyticsTracker;
import fr.univlorraine.tools.vaadin.IAnalyticsTracker;

/**
 * UI générique, pour gérer les attributs communs aux UI desktop ou Mobile sans se soucier de la version de l'UI appelée
 * @author charlie dubois
 *
 */
public class GenericUI  extends UI {

	private static final long serialVersionUID = 7686258492589590192L;


	//login de l'utilisateur
	@Setter
	@Getter
	protected String username;

	//'O' ou 'N' si l'utilisateur est un enseignant ou pas
	@Setter
	@Getter
	protected String userIsEnseignant;

	//'O' ou 'N' si l'utilisateur est un étudiant ou pas
	@Setter
	@Getter
	protected String userIsEtudiant;

	//Adresse IP du client
	@Setter
	protected String ipClient;

	//Etudiant dont on consulte le dossier
	@Setter
	@Getter
	protected Etudiant etudiant;

	//Vrai si on a réussi à récupérer les inscriptions via le WS.
	@Setter
	@Getter
	protected boolean recuperationWsInscriptionsOk;

	//vrai si on consulte les notes en vue enseignant
	@Setter
	@Getter
	protected boolean vueEnseignantNotesEtResultats;

	@Setter
	@Getter
	private List<String> ListeAnneeInscrits;

	//l'année correspondant à liste des inscrits en cours.
	@Setter
	@Getter
	private String anneeInscrits;

	//l'étape correspondant à la liste des inscrits si c'est une liste d'inscrits à une étape.
	@Setter
	@Getter
	private Etape etapeListeInscrits;

	//l'elp correspondant à la liste des inscrits si c'est une liste d'inscrits à un elp
	@Setter
	@Getter
	private ElementPedagogique elpListeInscrits;

	//la liste des étapes affichées dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private List<VersionEtape> listeEtapesInscrits;

	//l'identifiant de l'étape sélectionnée dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private String etapeInscrits;

	//la liste des groupes affichés dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private List<ElpDeCollection> listeGroupesInscrits;

	//l'identifiant du groupe sélectionné dans la liste des inscrits quand on consulte les inscrits à un ELP
	@Setter
	@Getter
	private String groupeInscrits;

	//annee universitaire en cours
	@Setter
	@Getter
	private String anneeUnivEnCours;

	//code de l'obj dont on affiche la liste des inscrits
	@Setter
	@Getter
	private String codeObjListInscrits;

	//type de l'obj dont on affiche la liste des inscrits
	@Setter
	@Getter
	private String typeObjListInscrits;

	//la liste des inscrits.
	@Setter
	@Getter
	private List<Inscrit> listeInscrits;

	@Getter
	@Setter
	protected IAnalyticsTracker analyticsTracker = new GoogleAnalyticsTracker(this);;
	
	/*@Getter
	@Setter
	protected GoogleAnalyticsTracker googleAnalyticsTracker = new GoogleAnalyticsTracker(this);
	
	@Getter
	@Setter
	protected PiwikAnalyticsTracker piwikAnalyticsTracker = new PiwikAnalyticsTracker(this);*/

	//Type (rôle) de l'utilisateur connecté
	@Setter
	@Getter
	protected String typeUser;

	//Le photo provider
	@Setter
	@Getter
	private IPhoto photoProvider= new PhotoNancy2ImplCodEtu();

	@Override
	protected void init(VaadinRequest request) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.vaadin.ui.UI#getCurrent()
	 */
	public static GenericUI getCurrent() {
		return (GenericUI) UI.getCurrent();
	}

	
	public String getIpClient() {     

		if(!StringUtils.hasText(ipClient)){

			VaadinRequest vr = VaadinService.getCurrentRequest();

			VaadinServletRequest vsRequest = (VaadinServletRequest)vr;
			HttpServletRequest hsRequest = vsRequest.getHttpServletRequest();

			String ip = hsRequest.getHeader("x-forwarded-for");    
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("X_FORWARDED_FOR");      
			}else{
				//Si x-forwarded-for contient plusieurs IP, on prend la deuxième
				if(ip.contains(",")){
					ip = ip.split(",")[1];
				}
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("X-Forwarded-For");      
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("HTTP_X_FORWARDED_FOR");      
			}
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("Proxy-Client-IP");      
			}   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getHeader("WL-Proxy-Client-IP");      
			}   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
				ip = hsRequest.getRemoteAddr();     
			} 
			
			ipClient = ip;
			
		}

		return ipClient;
	} 
}
