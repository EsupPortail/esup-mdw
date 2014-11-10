package fr.univlorraine.mondossierweb.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.client.ws.administratifmetier.AdministratifMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.client.ws.etudiantmetier.EtudiantMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.servicesmetiers.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.servicesmetiers.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.transverse.dto.administratif.CursusExterneDTO;
import gouv.education.apogee.commun.transverse.dto.administratif.CursusExternesEtTransfertsDTO;
import gouv.education.apogee.commun.transverse.dto.administratif.InsAdmAnuDTO2;
import gouv.education.apogee.commun.transverse.dto.administratif.InsAdmEtpDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.AdresseDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.IdentifiantsEtudiantDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.IndBacDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.InfoAdmEtuDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.TypeHebergementCourtDTO;
import gouv.education.apogee.commun.transverse.exception.WebBaseException;

/**
 * Gestion de l'étudiant dont on consulte le dossier
 */
@Component
public class EtudiantController {

	private Logger LOG = LoggerFactory.getLogger(EtudiantController.class);

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient UiController uiController;

	@Resource(name="emailConverter")
	private transient EmailConverterInterface emailConverter;
	;


	@Resource(name="photoProvider")
	private IPhoto photo;

	/**
	 * proxy pour faire appel aux infos concernant un étudiant.
	 */
	private EtudiantMetierServiceInterface monProxyEtu;

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	protected AdministratifMetierServiceInterface monProxyAdministratif;

	@Resource
	private MultipleApogeeService multipleApogeeService;

	/*@Resource
	private SessionController sessionController;*/

	public void recupererEtatCivil() {

		if(MainUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(MainUI.getCurrent().getEtudiant().getCod_etu())){
			if(monProxyEtu==null)
				monProxyEtu = new EtudiantMetierServiceInterfaceProxy();
			if(monProxyAdministratif==null)
				monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();
			try {
				//informations générales :
				IdentifiantsEtudiantDTO idetu;

				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					idetu = monProxyEtu.recupererIdentifiantsEtudiant(MainUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, null, "N");
				} else {
					idetu = monProxyEtu.recupererIdentifiantsEtudiant(MainUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, null, "O");
				}

				MainUI.getCurrent().getEtudiant().setCod_ind(idetu.getCodInd().toString());

				//Gestion des codine null
				if(idetu.getNumeroINE() != null && idetu.getCleINE() != null ){
					MainUI.getCurrent().getEtudiant().setCod_nne(idetu.getNumeroINE() + idetu.getCleINE());
				}else{
					MainUI.getCurrent().getEtudiant().setCod_nne("");
				}

				//Pour ne renseigner la photo que si elle n'est pas renseignée.
				MainUI.getCurrent().getEtudiant().setPhoto(photo.getUrlPhoto(MainUI.getCurrent().getEtudiant().getCod_ind(),MainUI.getCurrent().getEtudiant().getCod_etu()));


				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					// on passe par iBATIS pour récupérer l'e-mail.
					MainUI.getCurrent().getEtudiant().setEmail(emailConverter.getMail("",MainUI.getCurrent().getEtudiant().getCod_etu()));
				} else {
					//on récupérer l'e-mail grâce au WS.
					MainUI.getCurrent().getEtudiant().setEmail(idetu.getEmailAnnuaire());
				}

				InfoAdmEtuDTO iaetu = monProxyEtu.recupererInfosAdmEtu(MainUI.getCurrent().getEtudiant().getCod_etu());

				//MODIF POUR UTILISER LE NOM USUEL SI RENSEIGNE 19/09/2012
				if (iaetu.getNomUsuel() != null && !iaetu.getNomUsuel().equals("")){
					MainUI.getCurrent().getEtudiant().setNom(iaetu.getNomUsuel()+ " " + iaetu.getPrenom1());
				}else{
					MainUI.getCurrent().getEtudiant().setNom( iaetu.getNomPatronymique() + " " + iaetu.getPrenom1());
				}

				if (iaetu.getNumBoursier() != null ){
					MainUI.getCurrent().getEtudiant().setNumBoursier(iaetu.getNumBoursier());
				}


				//informations sur la naissance :
				//la nationalité:
				if (iaetu.getNationaliteDTO() != null) {
					MainUI.getCurrent().getEtudiant().setNationalite(iaetu.getNationaliteDTO().getLibNationalite());
				} else {
					MainUI.getCurrent().getEtudiant().setNationalite("");
				}
				//la date de naissance:
				if (iaetu.getDateNaissance() != null) {
					Date d = iaetu.getDateNaissance();
					MainUI.getCurrent().getEtudiant().setDatenaissance(Utils.formatDateToString(d));
				} else {
					MainUI.getCurrent().getEtudiant().setDatenaissance("");
				}
				//la ville de naissance:
				MainUI.getCurrent().getEtudiant().setLieunaissance(iaetu.getLibVilleNaissance());

				//récupération du département ou du pays de naissance:
				if (iaetu.getDepartementNaissance() != null ) {
					MainUI.getCurrent().getEtudiant().setDepartementnaissance(iaetu.getDepartementNaissance().getLibDept());
				} else {
					if (iaetu.getPaysNaissance() != null) {
						MainUI.getCurrent().getEtudiant().setDepartementnaissance(iaetu.getPaysNaissance().getLibPay());
					} else {
						MainUI.getCurrent().getEtudiant().setDepartementnaissance("");
					}
				}

				//informations sur l'inscription universitaire :
				MainUI.getCurrent().getEtudiant().setAnneeInscriptionUniversitaire(iaetu.getAnneePremiereInscEnsSup());

				if (iaetu.getEtbPremiereInscUniv() != null) {
					MainUI.getCurrent().getEtudiant().setEtablissement(iaetu.getEtbPremiereInscUniv().getLibEtb());
				} else {
					MainUI.getCurrent().getEtudiant().setEtablissement("");
				}


				//informations sur le(s) bac(s) :
				if (MainUI.getCurrent().getEtudiant().getListeBac() != null && MainUI.getCurrent().getEtudiant().getListeBac().size() > 0) {
					MainUI.getCurrent().getEtudiant().getListeBac().clear();
				} else {
					MainUI.getCurrent().getEtudiant().setListeBac(new ArrayList<BacEtatCivil>());
				}

				MainUI.getCurrent().setAnneeUnivEnCours(multipleApogeeService.getAnneeEnCours());
				LOG.info("anneeUnivEnCours : "+MainUI.getCurrent().getAnneeUnivEnCours());
				try{
					InsAdmAnuDTO2[] iaad2 = monProxyAdministratif.recupererIAAnnuelles_v2(MainUI.getCurrent().getEtudiant().getCod_etu(), MainUI.getCurrent().getAnneeUnivEnCours(), "ARE");
					if(iaad2!=null){
						MainUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(true);
						LOG.debug("nb ia pour annee en cours : "+iaad2.length);
						InsAdmAnuDTO2 iaad = iaad2[0];
						//Si témoin aménagement d'étude valué à O
						if(iaad.getTemRgmAmgEtuIAA()!=null && iaad.getTemRgmAmgEtuIAA().equals("O")){
							MainUI.getCurrent().getEtudiant().setTemAmenagementEtude(true);
						}
						//Si catégorie socio-professionnelle renseignée
						if(iaad.getCatSocProfEtu()!=null && iaad.getCatSocProfEtu().getCodeCategorie()!=null){
							String codeCatSocPro = iaad.getCatSocProfEtu().getCodeCategorie();
							//test si la catégorie n'est pas une catégorie de non salarié
							if(!codeCatSocPro.equals("81") && !codeCatSocPro.equals("82") &&
									!codeCatSocPro.equals("99") &&
									!codeCatSocPro.equals("A") ){
								MainUI.getCurrent().getEtudiant().setTemSalarie(true);
							}

						}
					}else{
						MainUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					}
				} catch (WebBaseException ex) {
					MainUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					LOG.info("Aucune IA remontée par le WS pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu()+" pour l'année "+MainUI.getCurrent().getAnneeUnivEnCours());
				}

				IndBacDTO[] bacvo = iaetu.getListeBacs();
				if (bacvo != null) {
					for (int i = 0; i < bacvo.length; i++) {
						IndBacDTO bac = bacvo[i];
						if (bac != null) {
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
							MainUI.getCurrent().getEtudiant().getListeBac().add(bec);
						}
					}
				} else {
					LOG.error("Probleme avec le WS: AUCUN BAC RETOURNE, lors de la recherche de l'état-civil pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu());
					BacEtatCivil bec = new BacEtatCivil();
					bec.setLib_bac("/");
					MainUI.getCurrent().getEtudiant().getListeBac().add(bec);
				}

				//On appel recupererAdresses pour récupérer le mail perso et le tel portable de l'étudiant
				recupererAdresses();

			} catch (WebBaseException ex) {
				LOG.error("Probleme avec le WS lors de la recherche de l'état-civil pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(),ex);
			} catch (Exception ex) {
				LOG.error("Probleme avec le WS lors de la recherche de l'état-civil pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(),ex);
			}
		}

	}


	public void recupererAdresses() {

		if(MainUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(MainUI.getCurrent().getEtudiant().getCod_etu())){
			if(monProxyAdministratif==null)
				monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();
			try{
				String[] annees =  monProxyAdministratif.recupererAnneesIa(MainUI.getCurrent().getEtudiant().getCod_etu(), null);

				//récupération de l'année la plus récente
				String annee = "0";
				for(int i=0; i<annees.length;i++){
					if (Integer.parseInt(annees[i])>Integer.parseInt(annee)){
						annee = annees[i];
					}
				}

				//récupération des coordonnées :
				CoordonneesDTO2 cdto = monProxyEtu.recupererAdressesEtudiant_v2(MainUI.getCurrent().getEtudiant().getCod_etu(), annee, "N");

				//récupération des adresses, annuelle et fixe :
				annee = cdto.getAnnee();
				MainUI.getCurrent().getEtudiant().setEmailPerso(cdto.getEmail());
				MainUI.getCurrent().getEtudiant().setTelPortable(cdto.getNumTelPortable());


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
						adresseAnnuelle.setType(th.getLibTypeHebergement());
					} else {
						adresseAnnuelle.setType("");
					}
					adresseAnnuelle.setAdresse1(ada.getLibAd1());
					adresseAnnuelle.setAdresse2(ada.getLibAd2());
					adresseAnnuelle.setAdresse3(ada.getLibAd3());
					adresseAnnuelle.setNumerotel(ada.getNumTel());
					if (ada.getPays() != null) {
						adresseAnnuelle.setPays(ada.getPays().getLibPay());
					} else {
						adresseAnnuelle.setPays("");
					}

					MainUI.getCurrent().getEtudiant().setAdresseAnnuelle(adresseAnnuelle);
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
					} else {
						adresseFixe.setPays("");
					}

					MainUI.getCurrent().getEtudiant().setAdresseFixe(adresseFixe);
				}

			} catch (WebBaseException ex) {
				LOG.error("Probleme avec le WS lors de la recherche de l'adresse pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(),ex);
			} catch (Exception ex) {
				LOG.error("Probleme avec le WS lors de la recherche de l'adresse pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(),ex);
			}

		}
	}

	/**
	 * va chercher et renseigne les informations concernant les inscriptions de 
	 * l'étudiant via le WS de l'Amue.
	 */
	public void recupererInscriptions() {
		try {
			if(MainUI.getCurrent().getEtudiant().getLinsciae()!=null){
				MainUI.getCurrent().getEtudiant().getLinsciae().clear();
			}else{
				MainUI.getCurrent().getEtudiant().setLinsciae(new LinkedList<Inscription>());
			}

			if(MainUI.getCurrent().getEtudiant().getLinscdac()!=null){
				MainUI.getCurrent().getEtudiant().getLinscdac().clear();
			}else{
				MainUI.getCurrent().getEtudiant().setLinscdac(new LinkedList<Inscription>());
			}




			MainUI.getCurrent().getEtudiant().setLibEtablissement(multipleApogeeService.getLibEtablissementDef());

			//cursus au sein de l'université:

			InsAdmEtpDTO2[] insdtotab = monProxyAdministratif.recupererIAEtapes_v2(MainUI.getCurrent().getEtudiant().getCod_etu(), "toutes", "ARE", "ARE");

			for (int i = 0; i < insdtotab.length; i++) {
				Inscription insc = new Inscription();
				InsAdmEtpDTO2 insdto = insdtotab[i];

				//on test si l'inscription n'est pas annulée:
				if (insdto.getEtatIae()!=null && insdto.getEtatIae().getCodeEtatIAE()!=null && insdto.getEtatIae().getCodeEtatIAE().equals("E")){

					//récupération de l'année
					int annee = new Integer(insdto.getAnneeIAE());
					int annee2 = annee + 1;
					insc.setCod_anu(annee + "/" + annee2);

					//récupération des informations sur l'étape
					insc.setCod_etp(insdto.getEtape().getCodeEtp());
					insc.setCod_vrs_vet(insdto.getEtape().getVersionEtp());
					insc.setLib_etp(insdto.getEtape().getLibWebVet());

					//récupération des informations sur le diplôme
					insc.setCod_dip(insdto.getDiplome().getCodeDiplome());
					insc.setVers_dip(insdto.getDiplome().getVersionDiplome());
					insc.setLib_dip(insdto.getDiplome().getLibWebVdi());

					//récupération des informations sur la composante
					insc.setCod_comp(insdto.getComposante().getCodComposante());
					insc.setLib_comp(insdto.getComposante().getLibComposante());

					//récupération de l'état en règle de l'inscription
					if(insdto.getInscriptionPayee().equals(Utils.LIBELLE_WS_INSCRIPTION_PAYEE)){
						insc.setEstEnRegle(true);
					}else{
						insc.setEstEnRegle(false);
					}

					//ajout de l'inscription à la liste
					MainUI.getCurrent().getEtudiant().getLinsciae().add(0, insc);
				}
			}


			//Autres cursus : 

			CursusExternesEtTransfertsDTO ctdto = monProxyAdministratif.recupererCursusExterne(MainUI.getCurrent().getEtudiant().getCod_etu());

			if (ctdto != null) {
				CursusExterneDTO[] listeCursusExt = ctdto.getListeCursusExternes();
				for (int i = 0; i < listeCursusExt.length; i++) {

					Inscription insc = new Inscription();

					CursusExterneDTO cext = listeCursusExt[i];

					int annee = new Integer(cext.getAnnee());
					int annee2 = annee + 1;
					insc.setCod_anu(annee + "/" + annee2);

					if (cext.getEtablissement() != null && cext.getTypeAutreDiplome() != null) {
						insc.setLib_etb(cext.getEtablissement().getLibEtb());
						// 24/04/2012 utilisation du libTypeDiplome a la place du CodeTypeDiplome
						insc.setCod_dac(cext.getTypeAutreDiplome().getLibTypeDiplome());
						insc.setLib_cmt_dac(cext.getCommentaire());
						if (cext.getTemObtentionDip() != null && cext.getTemObtentionDip().equals("N") ) {
							insc.setRes("AJOURNE");
						} else {
							insc.setRes("OBTENU");
						}

						MainUI.getCurrent().getEtudiant().getLinscdac().add(0, insc);
					}
				}
			}


			//première inscription universitaire : 
			InfoAdmEtuDTO iaetu = monProxyEtu.recupererInfosAdmEtu(MainUI.getCurrent().getEtudiant().getCod_etu());
			if (iaetu != null) {
				MainUI.getCurrent().getEtudiant().setAnneePremiereInscrip(iaetu.getAnneePremiereInscUniv());
				MainUI.getCurrent().getEtudiant().setEtbPremiereInscrip(iaetu.getEtbPremiereInscUniv().getLibEtb());
			}


		} catch (WebBaseException ex) {
			LOG.error("Probleme avec le WS lors de la recherche des inscriptions pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(), ex);
		} catch(Exception ex) {
			if(MainUI.getCurrent().getEtudiant()!=null){
				LOG.error("Probleme avec le WS lors de la recherche des inscriptions pour etudiant dont codetu est : " + MainUI.getCurrent().getEtudiant().getCod_etu(),ex);
			}else{
				LOG.error("Probleme avec le WS lors de la recherche des inscriptions pour etudiant ",ex);
			}
		}
	}


	/**
	 * va chercher et renseigne les informations concernant le calendrier des examens
	 */
	public void recupererCalendrierExamens() {
		MainUI.getCurrent().getEtudiant().setCalendrier(multipleApogeeService.getCalendrierExamens(MainUI.getCurrent().getEtudiant().getCod_ind()));
		MainUI.getCurrent().getEtudiant().setCalendrierRecupere(true);
	}


	public String getAnneeUnivEnCours() {
		if(MainUI.getCurrent()!=null){
			if(MainUI.getCurrent().getAnneeUnivEnCours()==null){
				MainUI.getCurrent().setAnneeUnivEnCours(multipleApogeeService.getAnneeEnCours());
			}
			return MainUI.getCurrent().getAnneeUnivEnCours();
		}
		return multipleApogeeService.getAnneeEnCours();
	}




}
