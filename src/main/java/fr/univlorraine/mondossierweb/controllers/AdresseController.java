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

import fr.univlorraine.apowsutils.ServiceProvider;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.AdresseMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CommuneMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TypeHebergementDTO;
import gouv.education.apogee.commun.client.ws.GeographieMetier.CommuneDTO2;
import gouv.education.apogee.commun.client.ws.GeographieMetier.GeographieMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.GeographieMetier.PaysDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Gestion des adresses
 */
@Component
public class AdresseController {

	private Logger LOG = LoggerFactory.getLogger(AdresseController.class);

	private static final String COD_HEBERG_DOMICILE_PARENTAL = "4";
	private static final String COD_PAY_FRANCE = "100";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient EtudiantController etudiantController;

	/**
	 * proxy pour faire appel aux infos concernant un étudiant.
	 */
	private final EtudiantMetierServiceInterface etudiantService = ServiceProvider.getService(EtudiantMetierServiceInterface.class);

	/** proxy pour faire appel aux infos géographique du WS  */
	private final GeographieMetierServiceInterface geographieService = ServiceProvider.getService(GeographieMetierServiceInterface.class);

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	protected AdministratifMetierServiceInterface administratifService = ServiceProvider.getService(AdministratifMetierServiceInterface.class);

	List<TypeHebergementDTO> listeTypeHebergement;

	List<PaysDTO> listePays;

	public List<TypeHebergementDTO> getTypesHebergement(){
		if(listeTypeHebergement==null || listeTypeHebergement.isEmpty()){
			try {
				listeTypeHebergement=etudiantService.recupererTypeHebergement(null, null, null);
			} catch (Exception e) {
				LOG.error("Problème lors de getTypesHebergement", e);
			}
		}
		return listeTypeHebergement;
	}

	public List<PaysDTO> getPays(){
		if(listePays == null || listePays.isEmpty()){
			try {
				listePays = geographieService.recupererPays(null, "O");
			} catch (Exception e) {
				LOG.error("Problème lors de getTypesHebergement", e);
			}
		}
		return listePays;
	}


	public List<CommuneDTO2> getVilles(String codePostal) {
		List<CommuneDTO2> lvilles  = new LinkedList<CommuneDTO2>();
		try{
			if (Pattern.matches("^[0-9]{5}", codePostal)) { 
				List<CommuneDTO2> lcdto =geographieService.recupererCommuneV2(codePostal,  "O", "O","O");
				if(lcdto!=null && !lcdto.isEmpty()){
					for (CommuneDTO2 commune : lcdto) {
						// Si TEM_EN_SVE_CBD = O
						if(commune!=null && commune.getTemSevBureauDis()!=null && commune.getTemSevBureauDis().equals("O")){
							boolean insere = false;
							int j = 0;
							while (!insere && j < lvilles.size()) {
								if (lvilles.get(j).getLibCommune().compareTo(commune.getLibCommune()) > 0) {
									lvilles.add(j,commune);
									insere = true;
								}
								if (lvilles.get(j).getLibCommune().equals(commune.getLibCommune())) {
									insere = true;
								}
								if (!insere) {
									j++;
								}
							}
							if (!insere) {
								lvilles.add(commune);
							}
						}
					}
				}
			} 
		}catch(Exception e ){
			LOG.info("Problème à la récupération de communes pour le code postal : "+codePostal,e);
		}
		return lvilles;
	}

	public List<String> majAdresses(Adresse adresseAnnuelle, Adresse adresseFixe, boolean modificationTelephoneAutorisee) {
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
			if (modificationTelephoneAutorisee && StringUtils.hasText(adresseAnnuelle.getNumerotel()) && !Utils.telephoneValide(adresseAnnuelle.getNumerotel())){
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
		if (adresseFixe.getNumerotel() != null && !Utils.telephoneValide(adresseFixe.getNumerotel())){
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
		try {
			//recup de l'ancienne et modif dessus:
			List<String> annees =  administratifService.recupererAnneesIa(cod_etu, null);
			//récupération de l'année la plus récente
			String annee = "0";
			if(annees!=null && !annees.isEmpty()) {
				for(String a : annees){
					if (Integer.parseInt(a)>Integer.parseInt(annee)){
						annee = a;
					}
				}
			}
			CoordonneesDTO2 cdto = etudiantService.recupererAdressesEtudiantV2(cod_etu, annee , "N");


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
			// Si email = null le WS Apogée génère un NPE
			cdtomaj.setEmail(cdto.getEmail() != null ? cdto.getEmail() : "");
			cdtomaj.setNumTelPortable(cdto.getNumTelPortable());
			cdtomaj.setAdresseAnnuelle(adanmaj);
			cdtomaj.setAdresseFixe(adfixmaj);

			LOG.debug("==== MAJ ADRESSE ==="+cdto.getAnnee()+" "+adresseAnnuelle.getType());
			etudiantService.mettreAJourAdressesEtudiant(cdtomaj, cod_etu);

			ok = true;
		} catch (Exception ex) {
			if(ex != null && ex.getMessage() != null && (ex.getMessage().contains("technical.data.nullretrieve") || ex.getMessage().contains("technical.parameter.nonpresentinput"))) {
				LOG.warn("Probleme " + ex.getMessage() + " lors de la maj des adresses de l'etudiant dont codetu est : " + cod_etu);
			}else {
				LOG.error("Probleme avec le WS lors de la maj des adresses de l'etudiant dont codetu est : " + cod_etu,ex);
			}
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

		try {
			List<CommuneDTO2> lcdto = geographieService.recupererCommuneV2(codepostal, "O", "O","O");

			if(lcdto!=null && !lcdto.isEmpty()) {
				for (CommuneDTO2 c : lcdto) {
					// Si TEM_EN_SVE_CBD = O
					if(c!=null && c.getTemSevBureauDis()!=null && c.getTemSevBureauDis().equals("O")){
						if (c.getLibCommune().equals(nom)){
							return c.getCodeCommune();
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.info("Probleme avec le WS lors de la getCodeInseeVille : "+codepostal ,e);
		}
		return null;

	}




}
