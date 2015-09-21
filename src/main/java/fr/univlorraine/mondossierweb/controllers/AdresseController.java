/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.controllers;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import gouv.education.apogee.commun.client.utils.WSUtils;
import gouv.education.apogee.commun.client.ws.administratifmetier.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.etudiantmetier.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.geographiemetier.GeographieMetierServiceInterface;
import gouv.education.apogee.commun.transverse.dto.etudiant.AdresseMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CommuneMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.TypeHebergementDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.CommuneDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.PaysDTO;
import gouv.education.apogee.commun.transverse.exception.WebBaseException;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Gestion des adresses
 */
@Component
public class AdresseController {

	private Logger LOG = LoggerFactory.getLogger(AdresseController.class);

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;

	private static final String COD_HEBERG_DOMICILE_PARENTAL = "4";
	private static final String COD_PAY_FRANCE = "100";


	/**
	 * proxy pour faire appel aux infos concernant un étudiant.
	 */
	private EtudiantMetierServiceInterface monProxyEtu;

	/**
	 * proxy pour faire appel aux infos sur les résultats du WS .
	 */
	private GeographieMetierServiceInterface monProxyGeo;

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	protected AdministratifMetierServiceInterface monProxyAdministratif;



	TypeHebergementDTO[] listeTypeHebergement;

	PaysDTO[] listePays;



	public TypeHebergementDTO[] getTypesHebergement(){

		if(listeTypeHebergement==null || listeTypeHebergement.length==0){
			if(monProxyEtu==null)
				monProxyEtu = (EtudiantMetierServiceInterface) WSUtils.getService(WSUtils.ETUDIANT_SERVICE_NAME);
			try {
				listeTypeHebergement=monProxyEtu.recupererTypeHebergement(null, null, null);
			} catch (RemoteException | WebBaseException e) {
				LOG.error("Problème lors de getTypesHebergement", e);
			}
		}

		return listeTypeHebergement;
	}


	public PaysDTO[] getPays(){
		if(listePays == null || listePays.length == 0){
			if(monProxyGeo==null)
				monProxyGeo = (GeographieMetierServiceInterface) WSUtils.getService(WSUtils.GEOGRAPHIE_SERVICE_NAME);
			try {
				listePays = monProxyGeo.recupererPays(null, "O");
			} catch (RemoteException | WebBaseException e) {
				LOG.error("Problème lors de getTypesHebergement", e);
			}
		}
		return listePays;
	}


	public List<CommuneDTO> getVilles(String codePostal) {
		List<CommuneDTO> lvilles  = new LinkedList<CommuneDTO>();
		try{
			//if (Pattern.matches("^[0-9]{2}[0-9]*", codePostal)) { 
			if (Pattern.matches("^[0-9]{5}", codePostal)) { 
				CommuneDTO[] cdto =monProxyGeo.recupererCommune(codePostal,  "O", "T");
				if(cdto!=null){
					for (int i = 0; i < cdto.length; i++) {
						boolean insere = false;
						int j = 0;
						while (!insere && j < lvilles.size()) {
							if (lvilles.get(j).getLibCommune().compareTo(cdto[i].getLibCommune()) > 0) {
								lvilles.add(j,cdto[i]);
								insere = true;
							}
							if (lvilles.get(j).getLibCommune().equals(cdto[i].getLibCommune())) {
								insere = true;
							}
							if (!insere) {
								j++;
							}
						}
						if (!insere) {
							lvilles.add(cdto[i]);
						}
					}
				}
			} 
		}catch(WebBaseException | RemoteException e ){
			LOG.error("Problème à la récupération de communes pour le code postal : "+codePostal,e);
		}
		return lvilles;
	}


	public List<String> majAdresses(Adresse adresseAnnuelle, Adresse adresseFixe) {
		List<String> retour = new LinkedList<String>();

		//1-vérification des parametres
		boolean erreur = false;
		boolean succes = false;
		boolean adresseIdentique=false;
		boolean boolAnEtranger = false;
		boolean boolFixeEtranger = false;
		String message = "";
		if (adresseAnnuelle.getType()==null || adresseAnnuelle.getType().equals("")) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.hebergement", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}else{
			adresseIdentique =  adresseAnnuelle.getType().equals(COD_HEBERG_DOMICILE_PARENTAL);
		}
		boolAnEtranger = !adresseAnnuelle.getCodPays().equals(COD_PAY_FRANCE);
		boolFixeEtranger= !adresseFixe.getCodPays().equals(COD_PAY_FRANCE);

		if (!adresseIdentique) {
			if (!StringUtils.hasText(adresseAnnuelle.getAdresse1())) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.detail", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getCodePostal()) && !boolAnEtranger) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.codepostal", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getVille()) && !boolAnEtranger) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.ville", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getCodPays())) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.pays", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}
			if (adresseAnnuelle.getNumerotel()!=null && (!Pattern.matches("[0-9[.]]*", adresseAnnuelle.getNumerotel()))){
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.telephone", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getAdresseetranger()) && boolAnEtranger) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.an.villeetrangere", null, Locale.getDefault());
				retour.add(message);
				erreur = true;
			}

		}

		if (!StringUtils.hasText(adresseFixe.getAdresse1())) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.detail", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getCodePostal()) && !boolFixeEtranger) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.codepostal", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getVille()) && !boolFixeEtranger) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.ville", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getCodPays())) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.pays", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if (adresseFixe.getNumerotel()!=null && (!Pattern.matches("[0-9[.]]*", adresseFixe.getNumerotel()))){
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.telephone", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getAdresseetranger()) && boolFixeEtranger) {
			message = applicationContext.getMessage("modificationAdressesWindow.erreur.af.villeetrangere", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}

		//2-maj les attribut utiles au changement d'adresse du bean etudiant
		if (!erreur) {

			//Si les deux adresses sont identiques
			if (adresseIdentique) {
				//on rapatrie le type d'hebergement dans adresse fixe
				adresseFixe.setType(adresseAnnuelle.getType());	
				//on fait la maj dans apogee en utilisant l'adresse fixe
				succes = majAdressesApogee(adresseFixe,adresseFixe, MainUI.getCurrent().getEtudiant().getCod_etu());
			}else{
				//on fait la maj dans apogee en utilisant l'adresse fixe et l'adresse annuelle
				succes = majAdressesApogee(adresseAnnuelle,adresseFixe, MainUI.getCurrent().getEtudiant().getCod_etu());
			}


			if (!succes) {
				message = applicationContext.getMessage("modificationAdressesWindow.erreur.ws", null, Locale.getDefault());
				retour.add(message);
			}else{
				retour.add("OK");
			}

		} 



		return retour;
	}


	private boolean majAdressesApogee(Adresse adresseAnnuelle,Adresse adresseFixe, String cod_etu) {
		boolean ok = false;
		if(monProxyEtu==null)
			monProxyEtu = (EtudiantMetierServiceInterface) WSUtils.getService(WSUtils.ETUDIANT_SERVICE_NAME);
		if(monProxyAdministratif==null)
			monProxyAdministratif = (AdministratifMetierServiceInterface) WSUtils.getService(WSUtils.ADMINISTRATIF_SERVICE_NAME);

		try {
			//recup de l'ancienne et modif dessus:
			String[] annees =  monProxyAdministratif.recupererAnneesIa(cod_etu, null);
			//récupération de l'année la plus récente
			String annee = "0";
			for(int i=0; i<annees.length;i++){
				if (Integer.parseInt(annees[i])>Integer.parseInt(annee)){
					annee = annees[i];
				}
			}
			CoordonneesDTO2 cdto = monProxyEtu.recupererAdressesEtudiant_v2(cod_etu, annee , "N");


			AdresseMajDTO adanmaj = new AdresseMajDTO();
			AdresseMajDTO adfixmaj = new AdresseMajDTO();

			adanmaj.setLibAd1(adresseAnnuelle.getAdresse1());
			adanmaj.setLibAd2(adresseAnnuelle.getAdresse2());
			adanmaj.setLibAd3(adresseAnnuelle.getAdresse3());
			adanmaj.setNumTel(adresseAnnuelle.getNumerotel());
			adanmaj.setCodPays(adresseAnnuelle.getCodPays());
			if (adresseAnnuelle.getCodPays().equals(COD_PAY_FRANCE) ) {
				CommuneMajDTO comanmaj = new CommuneMajDTO();
				comanmaj.setCodeInsee(getCodeInseeVille(adresseAnnuelle.getCodePostal(), adresseAnnuelle.getVille()));
				comanmaj.setCodePostal(adresseAnnuelle.getCodePostal());
				adanmaj.setCommune(comanmaj);
				adanmaj.setLibAde(null);
			} else {
				adanmaj.setCommune(null);
				adanmaj.setLibAde(adresseAnnuelle.getAdresseetranger());
			}



			adfixmaj.setLibAd1(adresseFixe.getAdresse1());
			adfixmaj.setLibAd2(adresseFixe.getAdresse2());
			adfixmaj.setLibAd3(adresseFixe.getAdresse3());
			adfixmaj.setNumTel(adresseFixe.getNumerotel());
			adfixmaj.setCodPays(adresseFixe.getCodPays());
			if (adresseFixe.getCodPays().equals(COD_PAY_FRANCE) ) {
				CommuneMajDTO comfixmaj = new CommuneMajDTO();
				comfixmaj.setCodeInsee(getCodeInseeVille(adresseFixe.getCodePostal(), adresseFixe.getVille()));
				comfixmaj.setCodePostal(adresseFixe.getCodePostal());
				adfixmaj.setCommune(comfixmaj);
				adfixmaj.setLibAde(null);
			} else {
				adfixmaj.setCommune(null);
				adfixmaj.setLibAde(adresseFixe.getAdresseetranger());
			}


			CoordonneesMajDTO cdtomaj = new CoordonneesMajDTO();
			cdtomaj.setAnnee(annee);
			cdtomaj.setTypeHebergement(adresseAnnuelle.getType());
			cdtomaj.setEmail(cdto.getEmail());
			cdtomaj.setNumTelPortable(cdto.getNumTelPortable());
			cdtomaj.setAdresseAnnuelle(adanmaj);
			cdtomaj.setAdresseFixe(adfixmaj);

			LOG.debug("==== MAJ ADRESSE ==="+cdto.getAnnee()+" "+adresseAnnuelle.getType());
			monProxyEtu.mettreAJourAdressesEtudiant(cdtomaj, cod_etu);

			ok = true;
		} catch (WebBaseException ex) {
			LOG.error("Probleme avec le WS lors de la maj des adresses de l'etudiant dont codetu est : " + cod_etu,ex);
		} catch (Exception ex) {
			LOG.error("Probleme avec le WS lors de la maj des adresses de l'etudiant dont codetu est : " + cod_etu,ex);
		}
		return ok;
	}

	/**
	 * 
	 * @param codepostal
	 * @param nom
	 * @return l'identifiant de la ville dont code et nom sont en parametres
	 */
	private String getCodeInseeVille(final String codepostal, final String nom) {

		if(monProxyGeo==null)
			monProxyGeo = (GeographieMetierServiceInterface) WSUtils.getService(WSUtils.GEOGRAPHIE_SERVICE_NAME);

	
		try {
			CommuneDTO[] cdto = monProxyGeo.recupererCommune(codepostal, "O", "T");

			for (int i = 0; i < cdto.length; i++) {
				CommuneDTO c = cdto[i];
				if (c.getLibCommune().equals(nom)){
					return c.getCodeCommune();
				}
			}
		} catch (RemoteException | WebBaseException e) {
			LOG.error("Probleme avec le WS lors de la getCodeInseeVille : "+codepostal ,e);
		}
		return null;

	}




}
