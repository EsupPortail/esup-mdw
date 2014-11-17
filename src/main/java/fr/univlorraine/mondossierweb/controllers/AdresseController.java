package fr.univlorraine.mondossierweb.controllers;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import gouv.education.apogee.commun.client.ws.etudiantmetier.EtudiantMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.client.ws.geographiemetier.GeographieMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.client.ws.administratifmetier.AdministratifMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.servicesmetiers.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.servicesmetiers.GeographieMetierServiceInterface;
import gouv.education.apogee.commun.servicesmetiers.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.transverse.dto.etudiant.AdresseMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CommuneMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.TypeHebergementDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.CommuneDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.PaysDTO;
import gouv.education.apogee.commun.transverse.exception.WebBaseException;

import java.util.LinkedList;
import java.util.List;
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
				monProxyEtu = new EtudiantMetierServiceInterfaceProxy();
			listeTypeHebergement=monProxyEtu.recupererTypeHebergement(null, null, null);
		}

		return listeTypeHebergement;
	}


	public PaysDTO[] getPays(){
		if(listePays == null || listePays.length == 0){
			if(monProxyGeo==null)
				monProxyGeo = new GeographieMetierServiceInterfaceProxy();
			listePays = monProxyGeo.recupererPays(null, "O");
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
		}catch(WebBaseException e ){
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
			message = "Veuillez indiquer un type d'hébergement pour l'adresse annuelle";
			retour.add(message);
			erreur = true;
		}else{
			adresseIdentique =  adresseAnnuelle.getType().equals(COD_HEBERG_DOMICILE_PARENTAL);
		}
		boolAnEtranger = !adresseAnnuelle.getCodPays().equals(COD_PAY_FRANCE);
		boolFixeEtranger= !adresseFixe.getCodPays().equals(COD_PAY_FRANCE);

		if (!adresseIdentique) {
			if (!StringUtils.hasText(adresseAnnuelle.getAdresse1())) {
				message = "Veuillez remplir le premier champ de l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getCodePostal()) && !boolAnEtranger) {
				message = "Veuillez indiquer un code postal pour l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getVille()) && !boolAnEtranger) {
				message = "Veuillez sélectionner une ville pour l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getCodPays())) {
				message = "Veuillez sélectionner un pays pour l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}
			if (adresseAnnuelle.getNumerotel()!=null && (!Pattern.matches("[0-9[.]]*", adresseAnnuelle.getNumerotel()))){
				message = "Veuillez indiquer un numéro de téléphone pour l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}
			if (!StringUtils.hasText(adresseAnnuelle.getAdresseetranger()) && boolAnEtranger) {
				message = "Veuillez indiquer une ville pour l'adresse annuelle";
				retour.add(message);
				erreur = true;
			}

		}

		if (!StringUtils.hasText(adresseFixe.getAdresse1())) {
			message = "Veuillez remplir le premier champ de l'adresse fixe";
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getCodePostal()) && !boolFixeEtranger) {
			message = "Veuillez indiquer un code postal pour l'adresse fixe";
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getVille()) && !boolFixeEtranger) {
			message = "Veuillez sélectionner une ville pour l'adresse fixe";
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getCodPays())) {
			message = "Veuillez sélectionner un pays pour l'adresse fixe";
			retour.add(message);
			erreur = true;
		}
		if (adresseFixe.getNumerotel()!=null && (!Pattern.matches("[0-9[.]]*", adresseFixe.getNumerotel()))){
			message = "Veuillez indiquer un numéro de téléphone pour l'adresse fixe";
			retour.add(message);
			erreur = true;
		}
		if (!StringUtils.hasText(adresseFixe.getAdresseetranger()) && boolFixeEtranger) {
			message = "Veuillez indiquer une ville pour l'adresse fixe";
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
				message = "Un problème est survenu pendant la mise à jour. Veuillez réessayer ultérieurement";
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
			monProxyEtu = new EtudiantMetierServiceInterfaceProxy();
		if(monProxyAdministratif==null)
			monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();

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

			System.out.println("==== MAJ ADRESSE ==="+cdto.getAnnee()+" "+adresseAnnuelle.getType());
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
			monProxyGeo = new GeographieMetierServiceInterfaceProxy();

		CommuneDTO[]cdto = monProxyGeo.recupererCommune(codepostal, "O", "T");
		for (int i = 0; i < cdto.length; i++) {
			CommuneDTO c = cdto[i];
			if (c.getLibCommune().equals(nom)){
				return c.getCodeCommune();
			}
		}

		return null;

	}
	



}
