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

import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import fr.univlorraine.apowsutils.ServiceProvider;
import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.InfosAnnuelles;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.DiplomeApogee;
import fr.univlorraine.mondossierweb.entities.apogee.InfoUsageEtatCivil;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.DiplomeApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.DiplomeApogeeServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionService;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.SsoApogeeService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.CursusExterneDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.CursusExternesEtTransfertsDTO;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmAnuDTO2;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.InsAdmEtpDTO3;
import gouv.education.apogee.commun.client.ws.AdministratifMetier.TableauCursusExterneDto;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.AdresseDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.AdresseMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CommuneMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.CoordonneesMajDTO;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.IdentifiantsEtudiantDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.IndBacDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.InfoAdmEtuDTO4;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TableauIndBacDTO2;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TypeHebergementCourtDTO;
import gouv.education.apogee.commun.client.ws.ScolariteMetier.OptionBacDTO2;
import gouv.education.apogee.commun.client.ws.ScolariteMetier.ScolariteMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.ScolariteMetier.SpecialiteBacDTO2;
import gouv.education.apogee.commun.client.ws.ScolariteMetier.WebBaseException_Exception;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * Gestion de l'étudiant dont on consulte le dossier
 */
@Component
@Slf4j
public class EtudiantController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	/** {@link DiplomeApogeeServiceImpl} */
	@Resource
	private DiplomeApogeeService diplomeService;
	/** {@link InscriptionServiceImpl} */
	@Resource
	private InscriptionService inscriptionService;
	/** {@link ComposanteServiceImpl} */
	@Resource
	private ComposanteService composanteService;
	@Resource
	private SsoApogeeService ssoApogeeService;
	@Resource
	private ElementPedagogiqueService elementPedagogiqueService;
	@Resource
	private transient UiController uiController;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;

	@Resource(name="${emailConverter.implementation}")
	private transient EmailConverterInterface emailConverter;




	private HashMap<String, String> listeOptBac;

	private HashMap<String, String> listeSpeBac;

	/**
	 * proxy pour faire appel aux infos concernant un étudiant.
	 */
	private final EtudiantMetierServiceInterface etudiantService = ServiceProvider.getService(EtudiantMetierServiceInterface.class);

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	private final AdministratifMetierServiceInterface administratifService = ServiceProvider.getService(AdministratifMetierServiceInterface.class);

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	private final ScolariteMetierServiceInterface scolariteService = ServiceProvider.getService(ScolariteMetierServiceInterface.class);


	@Resource
	private MultipleApogeeService multipleApogeeService;

	@Resource
	private transient SsoController ssoController;

	/*@Resource
	private SessionController sessionController;*/


	public boolean isEtudiantExiste(String codetu){

		try {
			//informations générales :
			IdentifiantsEtudiantDTO2 idetu;

			if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
				//idetu = monProxyEtu.recupererIdentifiantsEtudiant_v2(codetu, null, null, null, null, null, null, null, null, "N");
				idetu = etudiantService.recupererIdentifiantsEtudiantV2(codetu, null, null, null, null, null, null, null, "N");
			} else {
				idetu = etudiantService.recupererIdentifiantsEtudiantV2(codetu, null, null, null, null, null, null, null, "O");
			}
			if(idetu!=null && idetu.getCodInd()!=0 && StringUtils.hasText(idetu.getCodInd().toString())){
				return true;
			}
			return false;
		} catch (Exception ex) {
			return false;
		}
	}

	public void recupererEtatCivil() {

		if(GenericUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(GenericUI.getCurrent().getEtudiant().getCod_etu())){
			try {
				//informations générales :
				IdentifiantsEtudiantDTO2 idetu;

				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					idetu = etudiantService.recupererIdentifiantsEtudiantV2(GenericUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, "N");
				} else {
					idetu = etudiantService.recupererIdentifiantsEtudiantV2(GenericUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, "O");
				}

				String codInd = idetu.getCodInd().toString();
				GenericUI.getCurrent().getEtudiant().setCod_ind(codInd);

				//Gestion des codine null
				//if(idetu.getNumeroINE() != null && idetu.getCleINE() != null ){
				if(idetu.getNumeroINE() != null ){
					//GenericUI.getCurrent().getEtudiant().setCod_nne(idetu.getNumeroINE() + idetu.getCleINE());
					GenericUI.getCurrent().getEtudiant().setCod_nne(idetu.getNumeroINE());
				}else{
					GenericUI.getCurrent().getEtudiant().setCod_nne("");
				}


				GenericUI.getCurrent().getEtudiant().setPhoto(GenericUI.getCurrent().getPhotoProvider().getUrlPhoto(GenericUI.getCurrent().getEtudiant().getCod_ind(),GenericUI.getCurrent().getEtudiant().getCod_etu(), userController.isEnseignant(),userController.getCurrentUserName()));


				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					// on passe par emailConverter pour récupérer l'e-mail.
					GenericUI.getCurrent().getEtudiant().setEmail(emailConverter.getMail(GenericUI.getCurrent().getEtudiant().getCod_etu()));
				} else {
					//on récupérer l'e-mail grâce au WS.
					GenericUI.getCurrent().getEtudiant().setEmail(idetu.getEmailAnnuaire());
				}

				//InfoAdmEtuDTO iaetu = monProxyEtu.recupererInfosAdmEtu(GenericUI.getCurrent().getEtudiant().getCod_etu());
				//InfoAdmEtuDTO2 iaetu = etudiantService.recupererInfosAdmEtuV2(GenericUI.getCurrent().getEtudiant().getCod_etu());
				InfoAdmEtuDTO4 iaetu = etudiantService.recupererInfosAdmEtuV4(GenericUI.getCurrent().getEtudiant().getCod_etu());

				InfoUsageEtatCivil iuec= multipleApogeeService.getInfoUsageEtatCivilFromCodInd(codInd);
				log.debug("InfoUsageEtatCivil codCiv:"+iuec.getCodCiv()+" temPrUsage:"+ iuec.isTemPrUsage()+ " codSexEtaCiv:" +iuec.getCodSexEtatCiv()+" libPrEtaCiv:"+iuec.getLibPrEtaCiv());
				GenericUI.getCurrent().getEtudiant().setCodCiv(iuec.getCodCiv());
				GenericUI.getCurrent().getEtudiant().setSexEtatCiv(iuec.getCodSexEtatCiv());
				GenericUI.getCurrent().getEtudiant().setPrenomEtatCiv(iuec.getLibPrEtaCiv());
				GenericUI.getCurrent().getEtudiant().setTemPrUsage(iuec.isTemPrUsage());

				//Utilisant du nom patronymique
				GenericUI.getCurrent().getEtudiant().setNomAffichage(iaetu.getNomPatronymique());

				//Si afichage utilisant le nom usuel
				if(PropertyUtils.getTypeAffichageNomEtatCivil().equals(PropertyUtils.AFFICHAGE_NOM_BASIQUE)
					&& iaetu.getNomUsuel() != null && !iaetu.getNomUsuel().equals("")){
					GenericUI.getCurrent().getEtudiant().setNomAffichage(iaetu.getNomUsuel());
				}else if(PropertyUtils.getTypeAffichageNomEtatCivil().equals(PropertyUtils.AFFICHAGE_NOM_STANDARD)
					&& iaetu.getNomUsuel() != null && !iaetu.getNomUsuel().equals("") && !iaetu.getNomUsuel().equals(iaetu.getNomPatronymique())){
					//Si affichage avec nom patronymique ET usuel et si nom usuel non null et différent du nom patronymique
					GenericUI.getCurrent().getEtudiant().setNomAffichage(iaetu.getNomPatronymique()+ " ("+iaetu.getNomUsuel()+")");
				}

				GenericUI.getCurrent().getEtudiant().setNom(iaetu.getPrenom1()+ " "+GenericUI.getCurrent().getEtudiant().getNomAffichage());

				//informations sur la naissance :
				//la nationalité:
				if (iaetu.getNationaliteDTO() != null) {
					GenericUI.getCurrent().getEtudiant().setNationalite(iaetu.getNationaliteDTO().getLibNationalite());
				} else {
					GenericUI.getCurrent().getEtudiant().setNationalite("");
				}
				//la date de naissance:
				if (iaetu.getDateNaissance() != null) {
					GenericUI.getCurrent().getEtudiant().setDatenaissance(Utils.formatLocalDateTimeToString(iaetu.getDateNaissance()));
				} else {
					GenericUI.getCurrent().getEtudiant().setDatenaissance("");
				}
				//la ville de naissance:
				GenericUI.getCurrent().getEtudiant().setLieunaissance(iaetu.getLibVilleNaissance());

				//récupération du département ou du pays de naissance:
				if (iaetu.getDepartementNaissance() != null ) {
					GenericUI.getCurrent().getEtudiant().setDepartementnaissance(iaetu.getDepartementNaissance().getLibDept());
				} else {
					if (iaetu.getPaysNaissance() != null) {
						GenericUI.getCurrent().getEtudiant().setDepartementnaissance(iaetu.getPaysNaissance().getLibPay());
					} else {
						GenericUI.getCurrent().getEtudiant().setDepartementnaissance("");
					}
				}

				//informations sur l'inscription universitaire :
				GenericUI.getCurrent().getEtudiant().setAnneeInscriptionUniversitaire(iaetu.getAnneePremiereInscEnsSup());

				if (iaetu.getEtbPremiereInscUniv() != null) {
					GenericUI.getCurrent().getEtudiant().setEtablissement(iaetu.getEtbPremiereInscUniv().getLibEtb());
				} else {
					GenericUI.getCurrent().getEtudiant().setEtablissement("");
				}


				//informations sur le(s) bac(s) :
				if (GenericUI.getCurrent().getEtudiant().getListeBac() != null && !GenericUI.getCurrent().getEtudiant().getListeBac().isEmpty()) {
					GenericUI.getCurrent().getEtudiant().getListeBac().clear();
				} else {
					GenericUI.getCurrent().getEtudiant().setListeBac(new ArrayList<BacEtatCivil>());
				}

				GenericUI.getCurrent().setAnneeUnivEnCours(multipleApogeeService.getAnneeEnCours());
				log.debug("anneeUnivEnCours : "+GenericUI.getCurrent().getAnneeUnivEnCours());
				GenericUI.getCurrent().getEtudiant().setInfosAnnuelles(new LinkedList<InfosAnnuelles> ());
				try{
					// Initialisation des paramètres
					int anneeApo = Integer.parseInt(GenericUI.getCurrent().getAnneeUnivEnCours());
					int anneeMin = anneeApo - configController.getNbAnneesInfosAnnuelles() + 1;
					
					//Appel du WebService
					List<InsAdmAnuDTO2> iaad2 = administratifService.recupererIAAnnuellesV2(GenericUI.getCurrent().getEtudiant().getCod_etu(), 
						configController.getNbAnneesInfosAnnuelles() == 1 ? GenericUI.getCurrent().getAnneeUnivEnCours() : "toutes", "ARE");
					
					// Si on a récupéré des données
					if(iaad2!=null){
						
						log.debug("nb ia pour annee en cours : "+iaad2.size());
						
						// Liste des années pour lesquelles une IA valide a été trouvée
						List<Integer> insOkTrouvee= new LinkedList<Integer> ();
						// Vrai s'il sagit de l'année universitaire en cours dans Apogée
						boolean iaAnneeApoEnCours = false;
						
						// Pour chaque IAA récupérée
						for(InsAdmAnuDTO2 iaad : iaad2){
							String anneeIaa = iaad.getAnneeIAA();
							int anneeIaaEnCours = Integer.parseInt(anneeIaa);
							// Si l'année de l'IAA doit être traitée
							if(anneeIaaEnCours >= anneeMin) {
								// S'il s'agit de l'année active dans Apogée
								if(anneeIaaEnCours == anneeApo) {
									iaAnneeApoEnCours = true;
								}
								//Si IA non annulée et qu'aucune IA valide pour l'année n'a pas déjà été trouvée
								if(!insOkTrouvee.contains(anneeIaaEnCours) && iaad.getEtatIaa()!=null && iaad.getEtatIaa().getCodeEtatIAA()!=null && !iaad.getEtatIaa().getCodeEtatIAA().equals("A") ){
									
									// Ajout de l'année à la liste des années récupérées
									insOkTrouvee.add(anneeIaaEnCours);

									// Si l'IA est sur l'année en cours dans Apogée
									if(iaAnneeApoEnCours) {
										// On met à jour le témoin étudiant inscrit pour l'année en cours
										GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(true);

										// Récuperation du statut
										if(iaad.getStatut() != null && iaad.getStatut().getCode() != null){
											GenericUI.getCurrent().getEtudiant().setStatut(iaad.getStatut().getCode());
										}

										// Récuperation du témoin d'affiliation à la sécu
										GenericUI.getCurrent().getEtudiant().setAffilieSso(iaad.getTemoinAffiliationSS() != null && iaad.getTemoinAffiliationSS().equals("O"));

										// Récuperation du régime d'inscription
										if(iaad.getRegimeIns() != null && StringUtils.hasText(iaad.getRegimeIns().getLibRgi())){
											GenericUI.getCurrent().getEtudiant().setRegimeIns(iaad.getRegimeIns().getLibRgi());
										}

										// Récuperation du témoin dossier d'inscription validé
										GenericUI.getCurrent().getEtudiant().setTemDossierInscriptionValide(iaad.getEtatIaa().getTemDosIAA() != null && iaad.getEtatIaa().getTemDosIAA().equals("O"));
									}

									// Récupération informations pour l'onglet "Informations annuelles"
									InfosAnnuelles infos = new InfosAnnuelles();
									infos.setAnnee(anneeIaa);
									infos.setAnneeEnCours(iaAnneeApoEnCours);
									infos.setLibelle(Utils.getAnneeUniversitaireEnCours(anneeIaa));

									// Si boursier
									infos.setBoursier(multipleApogeeService.isBoursier(GenericUI.getCurrent().getEtudiant().getCod_ind(), anneeIaa));

									// Si témoin aménagement d'étude valué à O
									infos.setTemAmenagementEtude(iaad.getTemRgmAmgEtuIAA()!=null && iaad.getTemRgmAmgEtuIAA().equals("O"));

									// Si salarie
									infos.setTemSalarie(multipleApogeeService.isSalarie(GenericUI.getCurrent().getEtudiant().getCod_ind(), anneeIaa));

									if(configController.isAffNumerosAnonymat()) {
										//On recupere les numeros d'anonymat
										infos.setNumerosAnonymat(multipleApogeeService.getNumeroAnonymat(GenericUI.getCurrent().getEtudiant().getCod_etu(), anneeIaa));
									}

									// Ajout des informations dans la liste
									GenericUI.getCurrent().getEtudiant().getInfosAnnuelles().add(0, infos);
								}
							}
						}
						// Si aucune inscription valide pour l'année en cours dans Apogée n'a été trouvée
						if(!insOkTrouvee.contains(anneeApo)){
							GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
						}
					}else{
						GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					}
				} catch (Exception ex) {
					GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					log.info("Aucune IA remontée par le WS pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu()+" pour l'année "+GenericUI.getCurrent().getAnneeUnivEnCours());
				} 

				TableauIndBacDTO2 bacvo = iaetu.getListeBacs();
				//Si on a récupéré des bacs
				if (bacvo != null && bacvo.getItem() != null && !bacvo.getItem().isEmpty()) {
					// Pour chaque BAC
					for (IndBacDTO2 bac : bacvo.getItem()) {
						// Si le bac est valué
						if (bac != null) {
							// Création de l'objet BacEtatCivil
							BacEtatCivil bec = new BacEtatCivil();

							bec.setLib_bac(bac.getLibelleBac());
							bec.setCod_bac(bac.getCodBac());
							bec.setDaa_obt_bac_iba(bac.getAnneeObtentionBac());

							if (bac.getDepartementBac() != null ) {
								bec.setCod_dep(bac.getDepartementBac().getLibDept());
							} else {
								bec.setCod_dep("");
							}
							if (bac.getMentionBac() != null) {
								bec.setCod_mnb(bac.getMentionBac().getLibMention());
							} else {
								bec.setCod_mnb("");
							}
							if (bac.getTypeEtbBac() != null) {
								bec.setCod_tpe(bac.getTypeEtbBac().getLibLongTpe());
							} else { 
								bec.setCod_tpe("");
							}
							if (bac.getEtbBac() != null) {
								bec.setCod_etb(bac.getEtbBac().getLibEtb());
							} else {
								bec.setCod_etb("");
							}
							bec.setLicOpt1Bac(getOptionBac(bac.getCodOpt1Bac()));
							bec.setLicOpt2Bac(getOptionBac(bac.getCodOpt2Bac()));
							bec.setLicOpt3Bac(getOptionBac(bac.getCodOpt3Bac()));
							bec.setLicOpt4Bac(getOptionBac(bac.getCodOpt4Bac()));
							bec.setLicSpeBacPre(getSpecialiteBac(bac.getCodSpeBacPre()));
							bec.setLicSpe1Bac(getSpecialiteBac(bac.getCodSpe1Bac()));
							bec.setLicSpe2Bac(getSpecialiteBac(bac.getCodSpe2Bac()));
							GenericUI.getCurrent().getEtudiant().getListeBac().add(bec);
						}
					}
				} else {
					log.info("Probleme avec le WS: AUCUN BAC RETOURNE, lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu());
					BacEtatCivil bec = new BacEtatCivil();
					bec.setLib_bac("/");
					GenericUI.getCurrent().getEtudiant().getListeBac().add(bec);
				}

				//On vérifie si l'étudiant est interdit de consultation de ses notes
				List<String> lcodesBloquant = configController.getListeCodesBlocageAffichageNotes();
				//Si on a paramétré des codes bloquant
				if(lcodesBloquant!=null && !lcodesBloquant.isEmpty()){
					//Récupération des éventuels blocage pour l'étudiant
					List<String> lblo = multipleApogeeService.getListeCodeBlocage(GenericUI.getCurrent().getEtudiant().getCod_etu());
					// Si l'étudiant a des blocages
					if(lblo!=null && !lblo.isEmpty()){
						//Parcours des blocage
						for(String codblo : lblo){
							//Si le blocage est dans la liste des blocages configurés comme bloquant
							if(codblo != null && lcodesBloquant.contains(codblo)){
								//étudiant non autorise a consulter ses notes
								GenericUI.getCurrent().getEtudiant().setNonAutoriseConsultationNotes(true);
							}
						}
					}
				}

				//On appel recupererAdresses pour récupérer le mail perso et le tel portable de l'étudiant
				recupererAdresses();

			} catch (ServerSOAPFaultException ssx) {
				//Erreur côté WebService (ex : data.nullretrieve)
				log.info("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ssx);
				GenericUI.getCurrent().setEtudiant(null);
			} catch (ClientTransportException cte) {
				//Erreur Bad Gateway
				log.info("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),cte);
				GenericUI.getCurrent().setEtudiant(null);
			} catch (Exception ex) {
				if(ex != null && ex.getMessage() != null && ex.getMessage().contains("technical.data.nullretrieve")) {
					log.warn("Probleme " + ex.getMessage() + " lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu());
				}else {
					log.error("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}
				//On met l'étudiant à null pour remonter le problème
				GenericUI.getCurrent().setEtudiant(null);
			}
		}

	}


	private String getSpecialiteBac(String codSpe) {
		log.debug("Recuperation lib SPE BAC from code : "+codSpe);
		if(codSpe!=null) {
			if(listeSpeBac ==null || listeSpeBac.isEmpty()) {
				recuperSpeBacApogee();
			}
			if(listeSpeBac!=null && !listeSpeBac.isEmpty()) {
				return listeSpeBac.get(codSpe);
			}
		}
		return null;
	}

	private void recuperSpeBacApogee() {
		try {
			log.debug("Recuperation SPE BAC");
			List<SpecialiteBacDTO2>  liste = scolariteService.recupererSpeBacWS(null, null);
			if(liste!=null && !liste.isEmpty()) {
				log.debug(liste.size()+" SPE BAC");
				listeSpeBac = new HashMap<String, String> ();
				for(SpecialiteBacDTO2 spe : liste) {
					listeSpeBac.put(spe.getCodSpeBac(), spe.getLibSpeBac());
				}
			} else {
				log.warn("Aucune SPE BAC récupérée dans Apogée");
			}
		} catch (WebBaseException_Exception e) {
			log.warn("Erreur a la recupération des SPECIALITE BAC",e);
		}

	}

	private String getOptionBac(String codOpt) {
		log.debug("Recuperation lib OPT BAC from code : "+codOpt);
		if(codOpt!=null) {
			if(listeOptBac ==null || listeOptBac.isEmpty()) {
				recuperOptBacApogee();
			}
			if(listeOptBac!=null && !listeOptBac.isEmpty()) {
				return listeOptBac.get(codOpt);
			}
		}
		return null;
	}


	private void recuperOptBacApogee() {
		try {
			log.debug("Recuperation Options BAC");
			List<OptionBacDTO2>  liste = scolariteService.recupererOptBacWS(null, null);
			if(liste!=null && !liste.isEmpty()) {
				log.debug(liste.size()+" Options BAC");
				listeOptBac = new HashMap<String, String> ();
				for(OptionBacDTO2 opt : liste) {
					listeOptBac.put(opt.getCodOptBac(), opt.getLibOptBac());
				}
			} else {
				log.warn("Aucune Option BAC récupérée dans Apogée : "+liste);
			}
		} catch (WebBaseException_Exception e) {
			log.warn("Erreur a la recupération des OPTIONS BAC",e);
		}

	}

	public void recupererAdresses() {

		if(GenericUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(GenericUI.getCurrent().getEtudiant().getCod_etu())){

			try{
				List<String> annees =  administratifService.recupererAnneesIa(GenericUI.getCurrent().getEtudiant().getCod_etu(), null);

				if(annees!=null && !annees.isEmpty()){
					//récupération de l'année la plus récente
					String annee = "0";
					for(String a : annees){
						if (Integer.parseInt(a)>Integer.parseInt(annee)){
							annee = a;
						}
					}

					//récupération des coordonnées :
					CoordonneesDTO2 cdto = etudiantService.recupererAdressesEtudiantV2(GenericUI.getCurrent().getEtudiant().getCod_etu(), annee, "N");

					//récupération des adresses, annuelle et fixe :
					annee = cdto.getAnnee();
					GenericUI.getCurrent().getEtudiant().setEmailPerso(cdto.getEmail());
					GenericUI.getCurrent().getEtudiant().setTelPortable(cdto.getNumTelPortable());


					AdresseDTO2 ada = cdto.getAdresseAnnuelle();
					AdresseDTO2 adf = cdto.getAdresseFixe();

					if (ada != null) {
						Adresse adresseAnnuelle=new Adresse();


						adresseAnnuelle.setAnnee(Utils.getAnneeUniversitaireEnCours(annee));
						//informations sur l'adresse annuelle :
						if (ada.getLibAde() != null) {
							adresseAnnuelle.setAdresseetranger(ada.getLibAde());
							adresseAnnuelle.setCodePostal("");
							adresseAnnuelle.setVille("");
						} else {
							adresseAnnuelle.setAdresseetranger(null);
							if (ada.getCommune() != null) {
								adresseAnnuelle.setCodePostal(ada.getCommune().getCodePostal());
								adresseAnnuelle.setVille(ada.getCommune().getNomCommune());
							} else {
								adresseAnnuelle.setCodePostal("");
								adresseAnnuelle.setVille("");
							}
						}

						//TypeHebergementCourtDTO th = ada.getTypeHebergement();
						TypeHebergementCourtDTO th = cdto.getTypeHebergement();
						if (th != null) {
							//adresseAnnuelle.setType(th.getLibTypeHebergement());
							adresseAnnuelle.setType(th.getCodTypeHebergement());
						} else {
							adresseAnnuelle.setType("");
						}
						adresseAnnuelle.setAdresse1(ada.getLibAd1());
						adresseAnnuelle.setAdresse2(ada.getLibAd2());
						adresseAnnuelle.setAdresse3(ada.getLibAd3());
						adresseAnnuelle.setNumerotel(ada.getNumTel());
						if (ada.getPays() != null) {
							adresseAnnuelle.setPays(ada.getPays().getLibPay());
							adresseAnnuelle.setCodPays(ada.getPays().getCodPay());
						} else {
							adresseAnnuelle.setPays("");
						}

						GenericUI.getCurrent().getEtudiant().setAdresseAnnuelle(adresseAnnuelle);
					}
					if (adf != null) {

						Adresse adresseFixe=new Adresse();

						//informations sur l'adresse fixe :
						adresseFixe.setAdresse1(adf.getLibAd1());
						adresseFixe.setAdresse2(adf.getLibAd2());
						adresseFixe.setAdresse3(adf.getLibAd3());
						adresseFixe.setNumerotel(adf.getNumTel());

						if (adf.getLibAde() != null) {
							adresseFixe.setAdresseetranger(adf.getLibAde());
							adresseFixe.setCodePostal("");
							adresseFixe.setVille("");
						} else {
							adresseFixe.setAdresseetranger(null);
							if (adf.getCommune() != null ) {
								adresseFixe.setCodePostal(adf.getCommune().getCodePostal());
								adresseFixe.setVille(adf.getCommune().getNomCommune());
							} else {
								adresseFixe.setCodePostal("");
								adresseFixe.setVille("");
							}
						}
						if (adf.getPays() != null) {
							adresseFixe.setPays(adf.getPays().getLibPay());
							adresseFixe.setCodPays(adf.getPays().getCodPay());
						} else {
							adresseFixe.setPays("");
						}

						GenericUI.getCurrent().getEtudiant().setAdresseFixe(adresseFixe);
					}
				}else{
					log.info("Probleme lors de la recherche des annees d'IA pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu());
				}
			} catch (ServerSOAPFaultException ssx) {
				//Erreur côté WebService (ex : data.nullretrieve)
				log.info("Probleme lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ssx);
			} catch (ClientTransportException cte) {
				//Erreur Bad Gateway
				log.info("Probleme lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),cte);
			}  catch (Exception ex) {
				if(ex != null && ex.getMessage() != null && ex.getMessage().contains("technical.data.nullretrieve")) {
					log.warn("Probleme "+ex.getMessage()+" lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu());
				}else {
					log.error("Probleme lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}
			}
		}
	}

	/**
	 * va chercher et renseigne les informations concernant les inscriptions de 
	 * l'étudiant via le WS de l'Amue.
	 * @return true si tout c'est bien passé, false sinon
	 */
	public void recupererInscriptions() {
		try {
			if(GenericUI.getCurrent().getEtudiant().getLinsciae()!=null){
				GenericUI.getCurrent().getEtudiant().getLinsciae().clear();
			}else{
				GenericUI.getCurrent().getEtudiant().setLinsciae(new LinkedList<Inscription>());
			}

			if(GenericUI.getCurrent().getEtudiant().getLinscdac()!=null){
				GenericUI.getCurrent().getEtudiant().getLinscdac().clear();
			}else{
				GenericUI.getCurrent().getEtudiant().setLinscdac(new LinkedList<Inscription>());
			}


			GenericUI.getCurrent().getEtudiant().setLibEtablissement(multipleApogeeService.getLibEtablissementDef());

			//cursus au sein de l'université:

			List<InsAdmEtpDTO3> insdtotab = administratifService.recupererIAEtapesV3(GenericUI.getCurrent().getEtudiant().getCod_etu(), "toutes", "ARE", "ARE");

			if(insdtotab!=null && !insdtotab.isEmpty()){
				for (InsAdmEtpDTO3 insdto : insdtotab) {
					Inscription insc = new Inscription();

					//on test si l'inscription n'est pas annulée:
					if (insdto.getEtatIae()!=null && insdto.getEtatIae().getCodeEtatIAE()!=null && insdto.getEtatIae().getCodeEtatIAE().equals("E")){

						//récupération de l'année
						int annee = Integer.valueOf(insdto.getAnneeIAE());
						int annee2 = annee + 1;
						insc.setCod_anu(annee + "/" + annee2);

						//récupération des informations sur l'étape
						insc.setCod_etp(insdto.getEtape().getCodeEtp());
						insc.setCod_vrs_vet(insdto.getEtape().getVersionEtp());
						insc.setLib_etp(insdto.getEtape().getLibWebVet());
						if(insdto.getRegimeIns() != null) {
							insc.setLib_rge(insdto.getRegimeIns().getLibRgi());
						}
						
						//récupération des informations sur le diplôme
						insc.setCod_dip(insdto.getDiplome().getCodeDiplome());
						insc.setVers_dip(insdto.getDiplome().getVersionDiplome());
						insc.setLib_dip(insdto.getDiplome().getLibWebVdi());

						//récupération des informations sur la composante
						insc.setCod_comp(insdto.getComposante().getCodComposante());
						//insc.setLib_comp(insdto.getComposante().getLibComposante());
						insc.setLib_comp(composanteService.getLibelleComposante(insc.getCod_comp()));

						//récupération de l'état en règle de l'inscription
						if(insdto.getInscriptionPayee().equals(Utils.LIBELLE_WS_INSCRIPTION_PAYEE)){
							insc.setEstEnRegle(true);
						}else{
							insc.setEstEnRegle(false);
						}

						// Si le dossier d'inscription est validé
						if(insdto.getEtatIaa() != null && insdto.getEtatIaa().getTemDosIAA() != null && insdto.getEtatIaa().getTemDosIAA().equals("O")) {
							insc.setEstDossierValide(true);
						}

						//récupération de l'état de l'inscription
						if (insdto.getEtatIae() != null && StringUtils.hasText(insdto.getEtatIae().getCodeEtatIAE())) {
							insc.setEtatIae(insdto.getEtatIae().getCodeEtatIAE());
							insc.setEstEnCours(insdto.getEtatIae().getCodeEtatIAE().equals(Utils.ETAT_IAE_EN_COURS));
						} else {
							insc.setEtatIae(null);
							insc.setEstEnCours(false);
						}

						//ajout de l'inscription à la liste
						GenericUI.getCurrent().getEtudiant().getLinsciae().add(0, insc);
					}
				}
			}

			//Autres cursus : 

			CursusExternesEtTransfertsDTO ctdto = administratifService.recupererCursusExterne(GenericUI.getCurrent().getEtudiant().getCod_etu());

			if (ctdto != null) {
				TableauCursusExterneDto listeCursusExt = ctdto.getListeCursusExternes();
				if(listeCursusExt!=null && listeCursusExt.getItem()!=null && !listeCursusExt.getItem().isEmpty()) {
					for (CursusExterneDTO cext : listeCursusExt.getItem()) {

						Inscription insc = new Inscription();

						int annee = Integer.valueOf(cext.getAnnee());
						int annee2 = annee + 1;
						insc.setCod_anu(annee + "/" + annee2);

						// 02/07/2021 On prend en compte les établissements null
						if (cext.getEtablissement() != null || cext.getTypeAutreDiplome() != null || cext.getTypeDiplomeExt()!=null) {
							if (cext.getEtablissement()!=null) {
								insc.setLib_etb(cext.getEtablissement().getLibEtb());
							} else {
								insc.setLib_etb(applicationContext.getMessage("cursusexterne.etablissement.inconnu", null, Locale.getDefault()));
							}
							// Si TypeDiplomeExt valué
							if (cext.getTypeDiplomeExt()!=null) {
								// On renseigne COD_DAC avec TypeDiplomeExt
								insc.setCod_dac(cext.getTypeDiplomeExt().getLibTypDiplomeExt());
							}
							// Si TypeAutreDiplome valué
							if (cext.getTypeAutreDiplome()!=null) {
								// On écrase COD_DAC avec TypeAutreDiplome
								insc.setCod_dac(cext.getTypeAutreDiplome().getLibTypeDiplome());
							}
							insc.setLib_cmt_dac(cext.getCommentaire());
							// par défaut, on indique le diplôme obtenu
							insc.setRes(applicationContext.getMessage("cursusexterne.diplome.obtenu", null, Locale.getDefault()));
							if (cext.getTemObtentionDip() != null && cext.getTemObtentionDip().equals("N") ) {
								if(cext.getTemObtentionNiveau() != null && cext.getTemObtentionNiveau().equals("O")) {
									// diplome non obtenu mais niveau obtenu
									insc.setRes(applicationContext.getMessage("cursusexterne.diplome.nonobtenu", null, Locale.getDefault()));
								}else {
									// diplome et niveau non obtenu
									insc.setRes(applicationContext.getMessage("cursusexterne.diplome.ajourne", null, Locale.getDefault()));
								}
							} 

							GenericUI.getCurrent().getEtudiant().getLinscdac().add(insc);
						}
					}
				}
			}


			//première inscription universitaire : 
			//InfoAdmEtuDTO2 iaetu = etudiantService.recupererInfosAdmEtuV2(GenericUI.getCurrent().getEtudiant().getCod_etu());
			InfoAdmEtuDTO4 iaetu = etudiantService.recupererInfosAdmEtuV4(GenericUI.getCurrent().getEtudiant().getCod_etu());
			if (iaetu != null) {
				GenericUI.getCurrent().getEtudiant().setAnneePremiereInscrip(iaetu.getAnneePremiereInscUniv());
				GenericUI.getCurrent().getEtudiant().setEtbPremiereInscrip(iaetu.getEtbPremiereInscUniv().getLibEtb());
			}

			GenericUI.getCurrent().setRecuperationWsInscriptionsOk(true);

			//Si l'étudiant est inscrit pour l'année en cours
			if(GenericUI.getCurrent().getEtudiant().isInscritPourAnneeEnCours()){
				//Tentative de récupération des informations relatives à l'affiliation à la sécurité sociale
				try{
					GenericUI.getCurrent().getEtudiant().setRecuperationInfosAffiliationSsoOk(ssoController.recupererInfoAffiliationSso(getAnneeUnivEnCours(GenericUI.getCurrent()),GenericUI.getCurrent().getEtudiant()));
				} catch(Exception e){
					log.info("Probleme lors de la recuperer des Info AffiliationSso pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),e);
				}

				//Tentative de récupération des informations relatives à la quittance des droits payés
				try{
					GenericUI.getCurrent().getEtudiant().setRecuperationInfosQuittanceOk(ssoController.recupererInfoQuittance(getAnneeUnivEnCours(GenericUI.getCurrent()),GenericUI.getCurrent().getEtudiant()));
				} catch(Exception e){
					log.info("Probleme lors de la recuperer des Info Quittance pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),e);
				}
			}


		} catch(Exception ex) {
			if(GenericUI.getCurrent().getEtudiant()!=null){
				log.error("Probleme lors de la recherche des inscriptions pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
			}else{
				log.error("Probleme lors de la recherche des inscriptions pour etudiant ",ex);
			}
			GenericUI.getCurrent().setRecuperationWsInscriptionsOk(false);
		}
	}





	/**
	 * va chercher et renseigne les informations concernant le calendrier des examens
	 */
	public void recupererCalendrierExamens() {
		if(GenericUI.getCurrent()!=null && GenericUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(GenericUI.getCurrent().getEtudiant().getCod_ind())){
			GenericUI.getCurrent().getEtudiant().setCalendrier(multipleApogeeService.getCalendrierExamens(GenericUI.getCurrent().getEtudiant().getCod_ind(),configController.isAffDetailExamen()));
			GenericUI.getCurrent().getEtudiant().setCalendrierRecupere(true);
		}
	}

	public String getAnneeUnivRes(GenericUI ui) {
		if(ui!=null){
			if(ui.getAnneeUnivOuverteRes()==null){
				ui.setAnneeUnivOuverteRes(multipleApogeeService.getDerniereAnneeOuverteResultats());
			}
			return ui.getAnneeUnivOuverteRes();
		}
		return multipleApogeeService.getDerniereAnneeOuverteResultats();
	}

	public String getAnneeUnivEnCours(GenericUI ui) {
		if(ui!=null){
			if(ui.getAnneeUnivEnCours()==null){
				ui.setAnneeUnivEnCours(multipleApogeeService.getAnneeEnCours());
			}
			return ui.getAnneeUnivEnCours();
		}
		return multipleApogeeService.getAnneeEnCours();
	}

	public String getAnneeUnivEnCoursToDisplay(GenericUI ui) {
		int annee = Integer.parseInt(getAnneeUnivEnCours(ui));
		return annee+"/"+(annee+1);

	}



	public String getFormationEnCours(String codetu){
		return inscriptionService.getFormationEnCours(codetu);
	}




	public boolean proposerAttestationAffiliationSSO(Inscription ins, Etudiant etu){

		// autoriser ou non la generation de l'attestation
		if (!configController.isAttestationAffiliationSSO()) {
			return false;
		}
		// autoriser ou non les personnels à imprimer les attestations.
		if ( !configController.isAttestSsoAutoriseEnseignant() && userController.isEnseignant() && !userController.isGestionnaire()) {
			return false;
		}
		// autoriser ou non les gestionnaires à imprimer les attestations.
		if ( !configController.isAttestSsoAutoriseGestionnaire() && userController.isGestionnaire()) {
			return false;
		}
		String codAnuIns=ins.getCod_anu().substring(0, 4);
		if (!codAnuIns.equals(getAnneeUnivEnCours(GenericUI.getCurrent()))) {
			return false;
		}
		//si l'IAE n'est pas en règle
		if (!ins.isEstEnRegle()) {
			return false;
		}
		//si l'IAE n'est pas à l'état 'E'
		if (!ins.isEstEnCours()) {
			return false;
		}
		//Si pas affilié à la sécu
		if(!etu.isAffilieSso()){
			return false;
		}
		//interdit l'édition si on n'a pas réussi à récupérer les informations
		if(!etu.isRecuperationInfosAffiliationSsoOk()){
			return false;
		}

		return true;
	}

	public boolean proposerQuittanceDroitsPayes(Inscription ins, Etudiant etu){

		// autoriser ou non la generation de la quittance
		if (!configController.isQuittanceDroitsPayes()) {
			return false;
		}
		// autoriser ou non les personnels à imprimer les quittance
		if ( !configController.isQuittanceDroitsPayesAutoriseEnseignant() && userController.isEnseignant() && !userController.isGestionnaire()) {
			return false;
		}
		// autoriser ou non les gestionnaires à imprimer les quittance
		if ( !configController.isQuittanceDroitsPayesAutoriseGestionnaire() && userController.isGestionnaire()) {
			return false;
		}
		String codAnuIns=ins.getCod_anu().substring(0, 4);
		if (!codAnuIns.equals(getAnneeUnivEnCours(GenericUI.getCurrent()))) {
			return false;
		}
		//interdit l'édition de la quittance pour les étudiants dont le dossier n'est pas validé
		if(!configController.isQuittanceDossierNonValide()){
			//Si le dossier d'inscription non valide
			//if(!multipleApogeeService.isDossierInscriptionValide(etu.getCod_ind(), codAnuIns)){
			if(!ins.isEstDossierValide()){
				return false;
			}
		}
		//interdit l'edition de quittance si l'inscription n'est pas payée
		if(!ins.isEstEnRegle()){
			return false;
		}
		//interdit l'edition de quittance si l'inscription n'est pas "en cours"
		if(!ins.isEstEnCours()){
			return false;
		}
		//interdit l'édition si on n'a pas réussi à récupérer les informations
		if(!etu.isRecuperationInfosQuittanceOk()){
			return false;
		}

		return true;
	}

	public boolean proposerCertificat(Inscription ins, Etudiant etu) {

		// autoriser ou non la generation de certificats de scolarite.
		if (!configController.isCertificatScolaritePDF()) {
			return false;
		}
		// autoriser ou non les personnels à imprimer les certificats.
		if ( !configController.isCertScolAutoriseEnseignant() && userController.isEnseignant() && !userController.isGestionnaire()) {
			return false;
		}

		// autoriser ou non les gestionnaires à imprimer les certificats.
		if ( !configController.isCertScolAutoriseGestionnaire() && userController.isGestionnaire()) {
			return false;
		}

		String codAnuIns=ins.getCod_anu().substring(0, 4);

		// si on autorise l'édition de certificat de scolarité uniquement pour l'année en cours.
		if (!configController.isCertificatScolariteTouteAnnee() && !codAnuIns.equals(getAnneeUnivEnCours(GenericUI.getCurrent()))) {
			return false;
		}
		List<String> listeCertScolTypDiplomeDesactive=configController.getListeCertScolTypDiplomeDesactive();
		if ( listeCertScolTypDiplomeDesactive!=null && !listeCertScolTypDiplomeDesactive.isEmpty()) {
			// interdit les certificats pour certains types de diplomes
			DiplomeApogee dip = diplomeService.findDiplome(ins.getCod_dip());
			if(dip!=null && StringUtils.hasText(dip.getCodTpdEtb())){
				if (listeCertScolTypDiplomeDesactive.contains(dip.getCodTpdEtb())) {
					return false;
				}
			}
		}
		//interdit l'edition de certificat pour les étudiants si l'inscription n'est pas payée
		if ( !ins.isEstEnRegle() && userController.isEtudiant()){
			return false;
		}

		//interdit l'édition de certificat pour les étudiants si il reste des pièces justificatives non validées
		if(userController.isEtudiant() && !configController.isCertificatScolaritePiecesNonValidees()){
			//Si il reste des PJ non valides
			if(multipleApogeeService.getNbPJnonValides(etu.getCod_ind(), codAnuIns)>0){
				return false;
			}
		}
		//interdit l'édition de certificat pour les étudiants dont le dossier n'est pas validé
		if(!configController.isCertificatScolariteDossierNonValide()){
			//Si le dossier d'inscription non valide
			//if(!multipleApogeeService.isDossierInscriptionValide(etu.getCod_ind(), codAnuIns)){
			if(!ins.isEstDossierValide()){
				return false;
			}
		}
		//interdit l'edition de certificat pour les étudiants si l'inscription en cours est une cohabitation
		List<String> listeCertScolProfilDesactive=configController.getListeCertScolProfilDesactive();
		if ( listeCertScolProfilDesactive!=null && !listeCertScolProfilDesactive.isEmpty()) {
			// interdit les certificats pour certains types de diplomes
			String profil = inscriptionService.getProfil(codAnuIns, etu.getCod_ind());

			if (listeCertScolProfilDesactive.contains(profil)) {
				return false;
			}
		}
		//interdit l'édition de certificat pour les étudiants si l'inscription correspond à un code CGE désactivé
		List<String> listeCertScolCGEDesactive=configController.getListeCertScolCGEDesactive();
		if (listeCertScolCGEDesactive!=null && !listeCertScolCGEDesactive.isEmpty()) {
			// interdit les certificats pour certains code CGE
			String cge = inscriptionService.getCgeFromCodIndIAE(codAnuIns, etu.getCod_ind(), ins.getCod_etp(), ins.getCod_vrs_vet());

			if (listeCertScolCGEDesactive.contains(cge)) {
				return false;
			}
		}
		//interdit l'édition de certificat pour les étudiants si l'inscription correspond à un code composante désactivé
		List<String> listeCertScolCmpDesactive=configController.getListeCertScolCmpDesactive();
		if ( listeCertScolCmpDesactive!=null && !listeCertScolCmpDesactive.isEmpty()) {
			// interdit les certificats pour certains code composante
			String cmp = inscriptionService.getCmpFromCodIndIAE(codAnuIns, etu.getCod_ind(), ins.getCod_etp(), ins.getCod_vrs_vet());

			if (listeCertScolCmpDesactive.contains(cmp)) {
				return false;
			}
		}


		//interdit l'édition de certificat pour les étudiants dont le statut est dans la liste des exclusions
		List<String> listeCertScolStatutDesactive=configController.getListeCertScolStatutDesactive();
		if ( listeCertScolStatutDesactive!=null && !listeCertScolStatutDesactive.isEmpty()) {

			// interdit les certificats pour certains types de statut
			String statut = inscriptionService.getStatut(codAnuIns, etu.getCod_ind());

			if (statut!=null && listeCertScolStatutDesactive.contains(statut)) {
				return false;
			}
		}

		//interdit l'édition de certificat pour les étudiants si le témoin edition carte n'est pas coche
		if(userController.isEtudiant() && configController.isCertificatScolariteCarteEditee()){
			String temoinCarteEdit = multipleApogeeService.getTemoinEditionCarte(etu.getCod_ind(), codAnuIns);
			if ((temoinCarteEdit==null)||(!temoinCarteEdit.contains(Utils.TEMOIN_EDITION_CARTE_EDITEE))){
				return false;
			}
		}


		return true;
	}


	public List<String> updateContact(String telephone, String mail,String codetu) {
		List<String> retour = new LinkedList<String>();
		boolean erreur = false;
		String message = "";
		if(StringUtils.hasText(telephone) && !Utils.telephoneValide(telephone)){
			message = applicationContext.getMessage("modificationContact.erreur.tel", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if(StringUtils.hasText(mail) && !Pattern.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]{2,}[.][a-zA-Z]{2,4}$", mail)){
			message = applicationContext.getMessage("modificationContact.erreur.mail", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		// Test si le mail matche le pattern des mails de l'université
		if(StringUtils.hasText(configController.getRegexMailUniv()) && StringUtils.hasText(mail) && Pattern.matches(configController.getRegexMailUniv(), mail)){
			message = applicationContext.getMessage("modificationContact.erreur.mailuniv", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}

		//Si aucune erreur sur les données insérées
		if(!erreur){
			boolean succes = false;
			//On insere dans Apogée
			try {
				//recup de l'ancienne et modif dessus:
				List<String> annees =  administratifService.recupererAnneesIa(codetu, null);
				//récupération de l'année la plus récente
				String annee = "0";
				if(annees!=null && !annees.isEmpty()) {
					for(String a : annees){
						if (Integer.parseInt(a)>Integer.parseInt(annee)){
							annee = a;
						}
					}
				}
				CoordonneesDTO2 cdto = etudiantService.recupererAdressesEtudiantV2(codetu, annee , "N");


				AdresseMajDTO adanmaj = new AdresseMajDTO();
				AdresseMajDTO adfixmaj = new AdresseMajDTO();

				adanmaj.setLibAd1(cdto.getAdresseAnnuelle().getLibAd1());
				adanmaj.setLibAd2(cdto.getAdresseAnnuelle().getLibAd2());
				adanmaj.setLibAd3(cdto.getAdresseAnnuelle().getLibAd3());
				adanmaj.setNumTel(cdto.getAdresseAnnuelle().getNumTel());
				adanmaj.setCodPays(cdto.getAdresseAnnuelle().getPays().getCodPay());
				if (cdto.getAdresseAnnuelle().getCommune()!=null) {
					CommuneMajDTO comanmaj = new CommuneMajDTO();
					comanmaj.setCodeInsee(cdto.getAdresseAnnuelle().getCommune().getCodeInsee());
					comanmaj.setCodePostal(cdto.getAdresseAnnuelle().getCommune().getCodePostal());
					adanmaj.setCommune(comanmaj);
				}
				if(StringUtils.hasText(cdto.getAdresseAnnuelle().getLibAde())){
					adanmaj.setLibAde(cdto.getAdresseAnnuelle().getLibAde());
				}



				adfixmaj.setLibAd1(cdto.getAdresseFixe().getLibAd1());
				adfixmaj.setLibAd2(cdto.getAdresseFixe().getLibAd2());
				adfixmaj.setLibAd3(cdto.getAdresseFixe().getLibAd3());
				adfixmaj.setNumTel(cdto.getAdresseFixe().getNumTel());
				adfixmaj.setCodPays(cdto.getAdresseFixe().getPays().getCodPay());
				if (cdto.getAdresseFixe().getCommune()!=null) {
					CommuneMajDTO comfixmaj = new CommuneMajDTO();
					comfixmaj.setCodeInsee(cdto.getAdresseFixe().getCommune().getCodeInsee());
					comfixmaj.setCodePostal(cdto.getAdresseFixe().getCommune().getCodePostal());
					adfixmaj.setCommune(comfixmaj);
				}
				if(StringUtils.hasText(cdto.getAdresseFixe().getLibAde())){
					adfixmaj.setLibAde(cdto.getAdresseFixe().getLibAde());
				}


				CoordonneesMajDTO cdtomaj = new CoordonneesMajDTO();
				cdtomaj.setAnnee(annee);
				cdtomaj.setTypeHebergement(cdto.getTypeHebergement().getCodTypeHebergement());
				cdtomaj.setEmail(mail);
				cdtomaj.setNumTelPortable(telephone);
				cdtomaj.setAdresseAnnuelle(adanmaj);
				cdtomaj.setAdresseFixe(adfixmaj);

				log.debug("==== MAJ ADRESSE ==="+cdto.getAnnee()+" "+cdto.getTypeHebergement().getCodTypeHebergement());
				etudiantService.mettreAJourAdressesEtudiant(cdtomaj, codetu);

				succes = true;
			} catch (Exception ex) {
				if(ex != null && ex.getMessage() != null && ex.getMessage().contains("technical.data.nullretrieve")) {
					log.warn("Probleme " + ex.getMessage() + " lors de la maj des contacts de l'etudiant dont codetu est : " + codetu);
				}else {
					log.error("Probleme avec le WS lors de la maj des contacts de l'etudiant dont codetu est : " + codetu,ex);
				}
			}

			if (!succes) {
				message = applicationContext.getMessage("modificationContact.erreur.ws", null, Locale.getDefault());
				retour.add(message);
			}else{
				retour.add("OK");
			}
		}
		return retour;
	}


}
