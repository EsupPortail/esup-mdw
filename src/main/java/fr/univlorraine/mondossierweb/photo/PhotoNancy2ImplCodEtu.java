/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.photo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univnancy2.PhotoClient.beans.Category;
import fr.univnancy2.PhotoClient.beans.PhotoClient;
import fr.univnancy2.PhotoClient.beans.TicketClient;
import fr.univnancy2.PhotoClient.exception.PhotoClientException;





/**
 * classe pour la gestion des photos (notament la récupération du ticket).
 * @author Charlie Dubois
 */
@Component(value="photoProvider")
@Scope(value="session", proxyMode=ScopedProxyMode.INTERFACES)
public class PhotoNancy2ImplCodEtu implements IPhoto {
	/**
	 * Un logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PhotoNancy2ImplCodEtu.class);
	
	
	@Resource
	private transient UserController userController;
	
	
	/**
	 * vrai si l'utilisateur est un enseignant.
	 */
	private boolean utilisateurEnseignant;

	/**
	 * d�lai avant expiration du ticket.
	 */
	private static final int DELAI_TICKET_SEC = 200;

	/**
	 * url du serveur de photo.
	 */
	private String ressourceurl;
	/**
	 * url du serveur de photo pour le ticket.
	 */
	private String ticketurl;
	/**
	 * vrai si le ticket en cours est pour le serveur.
	 */
	private boolean forserver;
	/**
	 * le code de l'application pour le serveur de photos.
	 */
	private String codeapp;
	/**
	 *  ticket pour le serveur de photos.
	 */
	private TicketClient tc;

	/**
	 * client photo.
	 */
	private PhotoClient photoClient;

	/**
	 * constructeur vide.
	 */
	public PhotoNancy2ImplCodEtu() {
		super();
		forserver = false;
		

	}

	public String toString(){
		return "Bean PhotoNancy2CodEtu : tc="+tc.getCode();
	}

	/**
	 * @see org.esupportail.mondossierweb.web.photo.IPhoto#urlPhoto(java.lang.String)
	 */
	public String getUrlPhoto(final String cod_ind, String cod_etu) {
		String url = "";

		if(cod_etu==null)
			cod_etu="00000000";

		checkTicket(cod_etu);

		if (tc != null && tc.isValid(DELAI_TICKET_SEC)) {

			try {
				url = photoClient.computeURLforCode(Category.ETUDIANT, cod_etu, tc);

			} catch (PhotoClientException e) {
				LOG.error("PhotoNancy2ImplCodEtu-Erreur getUrlPhoto cod_ind:"+cod_ind+" , cod_etu:"+cod_etu,e);
			}
		}

		return url;
	}

	public String getUrlPhotoTrombinoscopePdf(final String cod_ind, String cod_etu) {
		String url = "";

		if(cod_etu==null)
			cod_etu="00000000";
		//on refait le ticket pour le serveur qui genere le pdf:
		utilisateurEnseignant = userController.isEnseignant();
		String loginUser = userController.getCurrentUserName();

		if (!forserver || tc == null || (tc != null && !tc.isValid(DELAI_TICKET_SEC))) {
			initForServer(loginUser);
		}
		if (tc != null && tc.isValid(DELAI_TICKET_SEC)) {

			try {
				url = photoClient.computeURLforCode(Category.ETUDIANT, cod_etu, tc);

			} catch (PhotoClientException e) {
				LOG.error("PhotoNancy2ImplCodEtu-Erreur getUrlPhotoTrombinoscopePdf cod_ind : "+cod_ind+", cod_etu:"+cod_etu,e);
			}
		}
	

	//on retourne la nouvelle url:
	return url;
}

/**
 * @see org.esupportail.mondossierweb.web.photo.IPhoto#ticketBon(int)
 */
public void checkTicket(final String cod_etu) {

	if (forserver || tc == null || (tc != null && !tc.isValid(DELAI_TICKET_SEC))) {

		utilisateurEnseignant = userController.isEnseignant();
		String loginUser = userController.getCurrentUserName();

		if (!utilisateurEnseignant) {
			init(cod_etu, loginUser);
		} else {
			init(loginUser);
		}

	}

}


public void initForServer(String loginUser) {

	String hostadress = "";
	try {
		hostadress = InetAddress.getLocalHost().getHostAddress();

		tc = photoClient.getTicket(PhotoClient.MODE_NORMAL,
				hostadress, "ID", "NONE", loginUser);


	} catch (UnknownHostException e1) {
		LOG.error("PhotoNancy2ImplCodEtu-Erreur initForServer loginUser:"+loginUser,e1);
	} catch (PhotoClientException e) {
		LOG.error("PhotoNancy2ImplCodEtu-Erreur initForServer loginUser:"+loginUser,e);
		tc = null;

	}
	forserver = true;
}
/**
 * initialise un ticket pour la photo d'un étudiant.
 * @param cod_etu
 */
public void init(final String cod_etu, String loginUser) {



	
	String remoteadress = getRemoteAdresse();
	// Param�tres du client photos
	if (photoClient == null) {
		photoClient = new PhotoClient();
		photoClient.setTicketURLPattern(getTicketurl());
		photoClient.setRessourceURLPattern(getRessourceurl());
		photoClient.setApplicationCode(getCodeapp());

	}
	// Demande d'un ticket au serveur de photos
	if (forserver || tc == null || (tc != null && !tc.isValid(DELAI_TICKET_SEC))) {
		try {
			tc = photoClient.getTicket(PhotoClient.MODE_NORMAL,
					remoteadress, "ID", cod_etu, loginUser);


		} catch (PhotoClientException e) {
			LOG.error("PhotoNancy2ImplCodEtu-Erreur init cod_etu:"+cod_etu+" loginUser:"+loginUser,e);
			tc = null;

		}
	}
	forserver = false;

}


/**
 * initialise un ticket pour avoir accès à toutes les photos.
 */
public void init(String loginUser) {

	String remoteadress = getRemoteAdresse();

	// Paramètres du client photos
	if (photoClient == null) {
		photoClient = new PhotoClient();
		photoClient.setTicketURLPattern(getTicketurl());
		photoClient.setRessourceURLPattern(getRessourceurl());
		photoClient.setApplicationCode(getCodeapp());
	}
	// Demande d'un ticket au serveur de photos
	if (forserver || tc == null || (tc != null && !tc.isValid(DELAI_TICKET_SEC))) {
		try {
			
			tc = photoClient.getTicket(PhotoClient.MODE_NORMAL,
					remoteadress, "ID", "NONE", loginUser);


		} catch (PhotoClientException e) {
			LOG.error("PhotoNancy2ImplCodEtu-Erreur init loginUser:"+loginUser,e);
			tc = null;

		}
	}
	forserver = false;

}

/**
 * @return l'adresse ip de l'utilisateur
 */
public String getRemoteAdresse() {
	WebBrowser browser;
	String ip =  getIpAddr();
	System.out.println("IP client : "+ip);
	return ip;
		//Recuperation de l'IP
	/*if(GenericUI.getCurrent() instanceof MainUI){
		MainUI mainUI = MainUI.getCurrent();
		browser = mainUI.getPage().getWebBrowser();
		LOG.info("IP client MainUI : "+browser.getAddress());
		return browser.getAddress();
		
	}
	if(GenericUI.getCurrent() instanceof MdwTouchkitUI){
		MdwTouchkitUI mdwTouchkitUI = MdwTouchkitUI.getCurrent();
		browser = mdwTouchkitUI.getPage().getWebBrowser();
		LOG.info("IP client MdwTouchkitUI : "+browser.getAddress());
		return browser.getAddress();
		
	}
	
	return null;*/
}

public String getIpAddr() {      
	VaadinRequest vr = VaadinService.getCurrentRequest();

	   String ip = vr.getHeader("x-forwarded-for");      
	   if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	       ip = vr.getHeader("Proxy-Client-IP");      
	   }      
	   if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	       ip = vr.getHeader("WL-Proxy-Client-IP");      
	   }      
	   if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {      
	       ip = vr.getRemoteAddr();      
	   }      
	   return ip;      
	} 

/**
 * getter pour l'url de la ressource renseign� dans domain.xml.
 * @return l'url de la ressource.
 */
public String getRessourceurl() {
	if(ressourceurl==null){
		ressourceurl = System.getProperty("context.param.photoserver.ressourceurl");
	}
	return ressourceurl;
}

/**
 * setter pour l'url de la ressource renseign� dans domain.xml.
 * @param ressourceurl
 */
public void setRessourceurl(final String ressourceurl) {
	this.ressourceurl = ressourceurl;
}

/**
 * getter pour l'url de demande de ticket renseign� dans domain.xml.
 * @return l'url pour r�cup�rer le ticket.
 */
public String getTicketurl() {
	if(ticketurl==null){
		ticketurl=System.getProperty("context.param.photoserver.ticketurl");
	}
	return ticketurl;
}

/**
 * setter pour l'url de demande de ticket renseign� dans domain.xml.
 * @param ticketurl
 */
public void setTicketurl(final String ticketurl) {
	this.ticketurl = ticketurl;
}

/**
 * @return photoClient
 */
public PhotoClient getPhotoClient() {
	return photoClient;
}

/**
 * @param photoClient
 */
public void setPhotoClient(final PhotoClient photoClient) {
	this.photoClient = photoClient;
}

public boolean isUtilisateurEnseignant() {
	return utilisateurEnseignant;
}

public void setUtilisateurEnseignant(boolean utilisateurEnseignant) {
	this.utilisateurEnseignant = utilisateurEnseignant;
}

public String getCodeapp() {
	
	if(codeapp==null){
		codeapp=System.getProperty("context.param.photoserver.codeapp");
	}
		
	return codeapp;
}

public void setCodeapp(String codeapp) {
	this.codeapp = codeapp;
}




}
