package fr.univlorraine.mondossierweb.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.beans.CacheResultatsElpEpr;
import fr.univlorraine.mondossierweb.beans.CacheResultatsVdiVet;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.DiplomeApogee;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.services.apogee.DiplomeApogeeService;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.InscriptionService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.client.ws.administratifmetier.AdministratifMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.client.ws.etudiantmetier.EtudiantMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.servicesmetiers.PedagogiqueMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.pedagogiquemetier.PedagogiqueMetierServiceInterfaceProxy;
import gouv.education.apogee.commun.servicesmetiers.AdministratifMetierServiceInterface;
import gouv.education.apogee.commun.servicesmetiers.EtudiantMetierServiceInterface;
import gouv.education.apogee.commun.transverse.dto.administratif.CursusExterneDTO;
import gouv.education.apogee.commun.transverse.dto.administratif.CursusExternesEtTransfertsDTO;
import gouv.education.apogee.commun.transverse.dto.administratif.InsAdmAnuDTO2;
import gouv.education.apogee.commun.transverse.dto.administratif.InsAdmEtpDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.AdresseDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.AdresseMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CommuneMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesDTO2;
import gouv.education.apogee.commun.transverse.dto.etudiant.CoordonneesMajDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.IdentifiantsEtudiantDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.IndBacDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.InfoAdmEtuDTO;
import gouv.education.apogee.commun.transverse.dto.etudiant.TypeHebergementCourtDTO;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ContratPedagogiqueResultatElpEprDTO4;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ContratPedagogiqueResultatVdiVetDTO;
import gouv.education.apogee.commun.transverse.dto.pedagogique.EpreuveElpDTO2;
import gouv.education.apogee.commun.transverse.dto.pedagogique.EtapeResVdiVetDTO;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ResultatElpDTO3;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ResultatEprDTO;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ResultatVdiDTO;
import gouv.education.apogee.commun.transverse.dto.pedagogique.ResultatVetDTO;
import gouv.education.apogee.commun.transverse.exception.WebBaseException;

/**
 * Gestion de l'étudiant dont on consulte le dossier
 */
@Component
public class EtudiantController {

	private Logger LOG = LoggerFactory.getLogger(EtudiantController.class);

	/**
	 * la signification du type de résultat 'COR'.
	 */
	private final String SIGNIFICATION_TYP_RESULT_COR ="Obtenu par Correspondance";

	private static final String COD_PAY_FRANCE = "100";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
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
	private transient UiController uiController;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;

	@Resource(name="emailConverter")
	private transient EmailConverterInterface emailConverter;



	@Resource(name="photoProvider")
	private IPhoto photo;

	/**
	 * proxy pour faire appel aux infos concernant un étudiant.
	 */
	private EtudiantMetierServiceInterface monProxyEtu;

	/**
	 * proxy pour faire appel aux infos administratives du WS .
	 */
	private AdministratifMetierServiceInterface monProxyAdministratif;

	/**
	 * proxy pour faire appel aux infos sur les résultats du WS .
	 */
	private PedagogiqueMetierServiceInterface monProxyPedagogique;

	@Resource
	private MultipleApogeeService multipleApogeeService;

	/*@Resource
	private SessionController sessionController;*/

	public void recupererEtatCivil() {

		if(GenericUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(GenericUI.getCurrent().getEtudiant().getCod_etu())){
			if(monProxyEtu==null)
				monProxyEtu = new EtudiantMetierServiceInterfaceProxy();
			if(monProxyAdministratif==null)
				monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();
			try {
				//informations générales :
				IdentifiantsEtudiantDTO idetu;

				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					idetu = monProxyEtu.recupererIdentifiantsEtudiant(GenericUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, null, "N");
				} else {
					idetu = monProxyEtu.recupererIdentifiantsEtudiant(GenericUI.getCurrent().getEtudiant().getCod_etu(), null, null, null, null, null, null, null, null, "O");
				}

				GenericUI.getCurrent().getEtudiant().setCod_ind(idetu.getCodInd().toString());

				//Gestion des codine null
				if(idetu.getNumeroINE() != null && idetu.getCleINE() != null ){
					GenericUI.getCurrent().getEtudiant().setCod_nne(idetu.getNumeroINE() + idetu.getCleINE());
				}else{
					GenericUI.getCurrent().getEtudiant().setCod_nne("");
				}

				//Pour ne renseigner la photo que si elle n'est pas renseignée.
				GenericUI.getCurrent().getEtudiant().setPhoto(photo.getUrlPhoto(GenericUI.getCurrent().getEtudiant().getCod_ind(),GenericUI.getCurrent().getEtudiant().getCod_etu()));


				if (!PropertyUtils.isRecupMailAnnuaireApogee()) {
					// on passe par iBATIS pour récupérer l'e-mail.
					GenericUI.getCurrent().getEtudiant().setEmail(emailConverter.getMail("",GenericUI.getCurrent().getEtudiant().getCod_etu()));
				} else {
					//on récupérer l'e-mail grâce au WS.
					GenericUI.getCurrent().getEtudiant().setEmail(idetu.getEmailAnnuaire());
				}

				InfoAdmEtuDTO iaetu = monProxyEtu.recupererInfosAdmEtu(GenericUI.getCurrent().getEtudiant().getCod_etu());

				//MODIF POUR UTILISER LE NOM USUEL SI RENSEIGNE 19/09/2012
				if (iaetu.getNomUsuel() != null && !iaetu.getNomUsuel().equals("")){
					GenericUI.getCurrent().getEtudiant().setNom(iaetu.getPrenom1()+ " "+iaetu.getNomUsuel());
				}else{
					GenericUI.getCurrent().getEtudiant().setNom( iaetu.getPrenom1()+ " "+iaetu.getNomPatronymique());
				}

				if (iaetu.getNumBoursier() != null ){
					GenericUI.getCurrent().getEtudiant().setNumBoursier(iaetu.getNumBoursier());
				}


				//informations sur la naissance :
				//la nationalité:
				if (iaetu.getNationaliteDTO() != null) {
					GenericUI.getCurrent().getEtudiant().setNationalite(iaetu.getNationaliteDTO().getLibNationalite());
				} else {
					GenericUI.getCurrent().getEtudiant().setNationalite("");
				}
				//la date de naissance:
				if (iaetu.getDateNaissance() != null) {
					Date d = iaetu.getDateNaissance();
					GenericUI.getCurrent().getEtudiant().setDatenaissance(Utils.formatDateToString(d));
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
				if (GenericUI.getCurrent().getEtudiant().getListeBac() != null && GenericUI.getCurrent().getEtudiant().getListeBac().size() > 0) {
					GenericUI.getCurrent().getEtudiant().getListeBac().clear();
				} else {
					GenericUI.getCurrent().getEtudiant().setListeBac(new ArrayList<BacEtatCivil>());
				}

				GenericUI.getCurrent().setAnneeUnivEnCours(multipleApogeeService.getAnneeEnCours());
				LOG.debug("anneeUnivEnCours : "+GenericUI.getCurrent().getAnneeUnivEnCours());
				try{
					InsAdmAnuDTO2[] iaad2 = monProxyAdministratif.recupererIAAnnuelles_v2(GenericUI.getCurrent().getEtudiant().getCod_etu(), GenericUI.getCurrent().getAnneeUnivEnCours(), "ARE");
					if(iaad2!=null){
						GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(true);
						LOG.debug("nb ia pour annee en cours : "+iaad2.length);
						InsAdmAnuDTO2 iaad = iaad2[0];
						//Si témoin aménagement d'étude valué à O
						if(iaad.getTemRgmAmgEtuIAA()!=null && iaad.getTemRgmAmgEtuIAA().equals("O")){
							GenericUI.getCurrent().getEtudiant().setTemAmenagementEtude(true);
						}
						//Si catégorie socio-professionnelle renseignée
						if(iaad.getCatSocProfEtu()!=null && iaad.getCatSocProfEtu().getCodeCategorie()!=null){
							String codeCatSocPro = iaad.getCatSocProfEtu().getCodeCategorie();
							//test si la catégorie n'est pas une catégorie de non salarié
							if(!codeCatSocPro.equals("81") && !codeCatSocPro.equals("82") &&
									!codeCatSocPro.equals("99") &&
									!codeCatSocPro.equals("A") ){
								GenericUI.getCurrent().getEtudiant().setTemSalarie(true);
							}

						}
					}else{
						GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					}
				} catch (WebBaseException ex) {
					GenericUI.getCurrent().getEtudiant().setInscritPourAnneeEnCours(false);
					LOG.info("Aucune IA remontée par le WS pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu()+" pour l'année "+GenericUI.getCurrent().getAnneeUnivEnCours());
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
							GenericUI.getCurrent().getEtudiant().getListeBac().add(bec);
						}
					}
				} else {
					LOG.info("Probleme avec le WS: AUCUN BAC RETOURNE, lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu());
					BacEtatCivil bec = new BacEtatCivil();
					bec.setLib_bac("/");
					GenericUI.getCurrent().getEtudiant().getListeBac().add(bec);
				}

				//On recupere les numeros d'anonymat
				GenericUI.getCurrent().getEtudiant().setNumerosAnonymat(multipleApogeeService.getNumeroAnonymat(GenericUI.getCurrent().getEtudiant().getCod_etu(), getAnneeUnivEnCours(GenericUI.getCurrent())));

				//On appel recupererAdresses pour récupérer le mail perso et le tel portable de l'étudiant
				recupererAdresses();

			} catch (WebBaseException ex) {
				//Si on est dans un cas d'erreur non expliqué
				if (ex.getNature().equals("technical.ws.remoteerror.global")){
					LOG.error("Probleme avec le WS lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}else{
					LOG.info("Probleme avec le WS lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}
			} catch (Exception ex) {
				LOG.error("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				throw(ex);
			}
		}

	}


	public void recupererAdresses() {

		if(GenericUI.getCurrent().getEtudiant()!=null && StringUtils.hasText(GenericUI.getCurrent().getEtudiant().getCod_etu())){
			if(monProxyAdministratif==null)
				monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();
			try{
				String[] annees =  monProxyAdministratif.recupererAnneesIa(GenericUI.getCurrent().getEtudiant().getCod_etu(), null);

				//récupération de l'année la plus récente
				String annee = "0";
				for(int i=0; i<annees.length;i++){
					if (Integer.parseInt(annees[i])>Integer.parseInt(annee)){
						annee = annees[i];
					}
				}

				//récupération des coordonnées :
				CoordonneesDTO2 cdto = monProxyEtu.recupererAdressesEtudiant_v2(GenericUI.getCurrent().getEtudiant().getCod_etu(), annee, "N");

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

			} catch (WebBaseException ex) {
				//Si on est dans un cas d'erreur non expliqué
				if (ex.getNature().equals("technical.ws.remoteerror.global")){
					LOG.error("Probleme avec le WS lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}else{
					LOG.info("Probleme avec le WS lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
				}
			} catch (Exception ex) {
				LOG.error("Probleme lors de la recherche de l'adresse pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
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

			InsAdmEtpDTO2[] insdtotab = monProxyAdministratif.recupererIAEtapes_v2(GenericUI.getCurrent().getEtudiant().getCod_etu(), "toutes", "ARE", "ARE");

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
					//insc.setLib_comp(insdto.getComposante().getLibComposante());
					insc.setLib_comp(composanteService.getLibelleComposante(insc.getCod_comp()));

					//récupération de l'état en règle de l'inscription
					if(insdto.getInscriptionPayee().equals(Utils.LIBELLE_WS_INSCRIPTION_PAYEE)){
						insc.setEstEnRegle(true);
					}else{
						insc.setEstEnRegle(false);
					}

					//ajout de l'inscription à la liste
					GenericUI.getCurrent().getEtudiant().getLinsciae().add(0, insc);
				}
			}


			//Autres cursus : 

			CursusExternesEtTransfertsDTO ctdto = monProxyAdministratif.recupererCursusExterne(GenericUI.getCurrent().getEtudiant().getCod_etu());

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

						GenericUI.getCurrent().getEtudiant().getLinscdac().add(0, insc);
					}
				}
			}


			//première inscription universitaire : 
			InfoAdmEtuDTO iaetu = monProxyEtu.recupererInfosAdmEtu(GenericUI.getCurrent().getEtudiant().getCod_etu());
			if (iaetu != null) {
				GenericUI.getCurrent().getEtudiant().setAnneePremiereInscrip(iaetu.getAnneePremiereInscUniv());
				GenericUI.getCurrent().getEtudiant().setEtbPremiereInscrip(iaetu.getEtbPremiereInscUniv().getLibEtb());
			}

			GenericUI.getCurrent().setRecuperationWsInscriptionsOk(true);
		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error("Probleme avec le WS lors de la recherche des inscriptions pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(), ex);
			}else{
				LOG.info("Probleme avec le WS lors de la recherche des inscriptions pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(), ex);
			}
			GenericUI.getCurrent().setRecuperationWsInscriptionsOk(false);
		} catch(Exception ex) {
			if(GenericUI.getCurrent().getEtudiant()!=null){
				LOG.error("Probleme lors de la recherche des inscriptions pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
			}else{
				LOG.error("Probleme lors de la recherche des inscriptions pour etudiant ",ex);
			}
			GenericUI.getCurrent().setRecuperationWsInscriptionsOk(false);
		}
	}





	/**
	 * va chercher et renseigne les informations concernant le calendrier des examens
	 */
	public void recupererCalendrierExamens() {
		GenericUI.getCurrent().getEtudiant().setCalendrier(multipleApogeeService.getCalendrierExamens(GenericUI.getCurrent().getEtudiant().getCod_ind()));
		GenericUI.getCurrent().getEtudiant().setCalendrierRecupere(true);
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




	/**
	 * va chercher et renseigne les notes de
	 * l'étudiant via le WS de l'Amue.
	 */
	public void recupererNotesEtResultats(Etudiant e) {
		if(monProxyPedagogique==null)
			monProxyPedagogique = new PedagogiqueMetierServiceInterfaceProxy();


		try {
			e.getDiplomes().clear();
			e.getEtapes().clear();

			String temoin = configController.getTemoinNotesEtudiant();
			if(temoin == null || temoin.equals("")){
				temoin="T";
			}

			String sourceResultat = PropertyUtils.getSourceResultats();
			if(sourceResultat == null || sourceResultat.equals("")){
				sourceResultat="Apogee";
			}
			// VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
			// Si different annee en cours => sourceResultat = Apogee
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				// On recupere les resultats dans cpdto avec sourceResultat=Apogee
				sourceResultat="Apogee";
				ContratPedagogiqueResultatVdiVetDTO[] cpdtoResult = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous");

				// Puis dans cpdtoExtract avec sourceResultat=Apogee-extraction pour l'année en cours
				temoin=null;
				sourceResultat="Apogee-extraction";
				String annee = getAnneeUnivEnCours(GenericUI.getCurrent());
				ContratPedagogiqueResultatVdiVetDTO[] cpdtoExtract;
				try {
					cpdtoExtract = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), annee, sourceResultat, temoin, "toutes", "tous");
				} catch (Exception ex) {
					cpdtoExtract = null;
				}

				// Et on fusionne cpdtoResult et cpdtoExtract
				ArrayList<ContratPedagogiqueResultatVdiVetDTO> cpdtoAl = new ArrayList<ContratPedagogiqueResultatVdiVetDTO>();
				for (int i = 0; i < cpdtoResult.length; i++ ) {
					if (cpdtoResult[i].getAnnee() != null) {
						if (cpdtoResult[i].getAnnee().compareTo(annee)!=0) {
							cpdtoAl.add(cpdtoResult[i]);
						}
					}
				}
				if (cpdtoExtract!=null) {
					for (int i = 0; i < cpdtoExtract.length; i++ ) {
						cpdtoAl.add(cpdtoExtract[i]);
					}
				}
				ContratPedagogiqueResultatVdiVetDTO[] cpdto = cpdtoAl.toArray(new ContratPedagogiqueResultatVdiVetDTO[ cpdtoAl.size() ]);
				setNotesEtResultats(e, cpdto);

			} else {

				ContratPedagogiqueResultatVdiVetDTO[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous");
				setNotesEtResultats(e, cpdto);
			}


		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}else{
				LOG.info(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}
		} catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
		}

	}


	/**
	 * va chercher et renseigne les notes de
	 * l'étudiant à destination d'un enseignant via le WS de l'Amue.
	 */
	public void recupererNotesEtResultatsEnseignant(Etudiant e) {

		if(monProxyPedagogique==null)
			monProxyPedagogique = new PedagogiqueMetierServiceInterfaceProxy();

		try {
			e.getDiplomes().clear();
			e.getEtapes().clear();

			String temoin = configController.getTemoinNotesEnseignant();
			if(temoin == null || temoin.equals("")){
				temoin="AET";
			}

			String sourceResultat = PropertyUtils.getSourceResultats();
			if(sourceResultat == null || sourceResultat.equals("")){
				sourceResultat="Apogee";
			}

			// VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
			// Si different annee en cours => sourceResultat = Apogee
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				// On recupere les resultats dans cpdto avec sourceResultat=Apogee
				sourceResultat="Apogee";
				ContratPedagogiqueResultatVdiVetDTO[] cpdtoResult = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous");
				// Puis dans cpdtoExtract avec sourceResultat=Apogee-extraction pour l'année en cours
				temoin=null;
				sourceResultat="Apogee-extraction";
				String annee = getAnneeUnivEnCours(GenericUI.getCurrent());
				ContratPedagogiqueResultatVdiVetDTO[] cpdtoExtract;
				try {
					cpdtoExtract = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), annee, sourceResultat, temoin, "toutes", "tous");
				} catch (Exception ex) {
					cpdtoExtract = null;
				}

				// Et on fusionne cpdtoResult et cpdtoExtract
				ArrayList<ContratPedagogiqueResultatVdiVetDTO> cpdtoAl = new ArrayList<ContratPedagogiqueResultatVdiVetDTO>();
				for (int i = 0; i < cpdtoResult.length; i++ ) {
					if (cpdtoResult[i].getAnnee() != null) {
						if (cpdtoResult[i].getAnnee().compareTo(annee)!=0) {
							cpdtoAl.add(cpdtoResult[i]);
						}
					}
				}
				if (cpdtoExtract!=null) {
					for (int i = 0; i < cpdtoExtract.length; i++ ) {
						cpdtoAl.add(cpdtoExtract[i]);
					}
				}
				ContratPedagogiqueResultatVdiVetDTO[] cpdto = cpdtoAl.toArray(new ContratPedagogiqueResultatVdiVetDTO[ cpdtoAl.size() ]);
				setNotesEtResultats(e, cpdto);

			} else {

				ContratPedagogiqueResultatVdiVetDTO[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatVdiVet(e.getCod_etu(), "toutes", sourceResultat, temoin, "toutes", "tous");
				setNotesEtResultats(e, cpdto);
			}

		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}else{
				LOG.info(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}
		} catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
		}

	}



	/**
	 * renseigne les attributs concernant les notes et résultats obtenus.
	 * @param e
	 * @param cpdto
	 */
	public void setNotesEtResultats(Etudiant e, ContratPedagogiqueResultatVdiVetDTO[] resultatVdiVet) {
		try {


			e.getDiplomes().clear();
			e.getEtapes().clear();
			//Si on a configure pour toujours afficher le rang, on affichera les rangs de l'étudiant.
			e.setAfficherRang(configController.isAffRangEtudiant());

			for (int i = 0; i < resultatVdiVet.length; i++ ) {
				//information sur le diplome:
				ContratPedagogiqueResultatVdiVetDTO rdto = resultatVdiVet[i];

				if(rdto.getDiplome() != null){
					Diplome d = new Diplome();

					d.setLib_web_vdi(rdto.getDiplome().getLibWebVdi());
					d.setCod_dip(rdto.getDiplome().getCodDip());
					d.setCod_vrs_vdi(rdto.getDiplome().getCodVrsVdi().toString());

					int annee2 = new Integer(rdto.getAnnee()) + 1;


					d.setAnnee(rdto.getAnnee() + "/" + annee2);
					//information sur les résultats obtenus au diplome:
					ResultatVdiDTO[] tabres = rdto.getResultatVdi();

					if (tabres != null && tabres.length > 0) {


						for (int j = 0; j < tabres.length; j++ ) {
							Resultat r = new Resultat();
							ResultatVdiDTO res = tabres[j];

							r.setSession(res.getSession().getLibSes());
							if(res.getNatureResultat() != null && res.getNatureResultat().getCodAdm() != null && res.getNatureResultat().getCodAdm().equals("0")){
								//on est en Admissibilité à l'étape.Pas en admission.
								//on le note pour que ce soit plus clair pour l'étudiant
								r.setNote(res.getNatureResultat().getLibAdm());


							}

							//recuperation de la mention
							if(res.getMention() != null){
								r.setCodMention(res.getMention().getCodMen());
								r.setLibMention(res.getMention().getLibMen());
							}

							String result="";
							if( res.getTypResultat()!=null){
								result= res.getTypResultat().getCodTre();
								r.setAdmission(result);
							}
							if (res.getNotVdi() != null) {
								r.setNote(res.getNotVdi().toString());
								//ajout pour note Jury
								if(res.getNotPntJurVdi() != null && !res.getNotPntJurVdi().equals(new BigDecimal(0))){
									r.setNote(r.getNote()+"(+"+res.getNotPntJurVdi()+")");
								}
							} else {
								if (result.equals("DEF")) {
									r.setNote("DEF");
								}
							}

							//Gestion du barème:
							if(res.getBarNotVdi() != null){
								r.setBareme(res.getBarNotVdi());
							}


							//ajout de la signification du résultat dans la map
							if ((result != null && !result.equals("")) && !e.getSignificationResultats().containsKey(r.getAdmission())) {
								e.getSignificationResultats().put(r.getAdmission(), res.getTypResultat().getLibTre());
							}

							//ajout du résultat au diplome:
							d.getResultats().add(r);
							if(res.getNbrRngEtuVdi() != null && !res.getNbrRngEtuVdi().equals("")){
								d.setRang(res.getNbrRngEtuVdi()+"/"+res.getNbrRngEtuVdiTot());
								//On indique si on affiche le rang du diplome.
								d.setAfficherRang(configController.isAffRangEtudiant());

							}
						}
						//ajout du diplome si on a au moins un résultat
						//e.getDiplomes().add(0, d);
					}
					e.getDiplomes().add(0, d);
				}
				//information sur les etapes:
				EtapeResVdiVetDTO[] etapes = rdto.getEtapes();
				if (etapes != null && etapes.length > 0) {

					for (int j = 0; j < etapes.length; j++ ) {
						EtapeResVdiVetDTO etape = etapes[j];

						//29/01/10
						//on rejete les etapes annulée. MAJ sur proposition de Rennes1
						if((etape.getCodEtaIae()== null) || (etape.getCodEtaIae()!= null && !etape.getCodEtaIae().equals("A"))){

							Etape et = new Etape();
							int anneeEtape = new Integer(etape.getCodAnu());
							et.setAnnee(anneeEtape + "/" + (anneeEtape + 1));
							et.setCode(etape.getEtape().getCodEtp());
							et.setVersion(etape.getEtape().getCodVrsVet().toString());
							et.setLibelle(etape.getEtape().getLibWebVet());

							//ajout 16/02/2012 pour WS exposés pour la version mobile en HttpInvoker
							if(rdto.getDiplome()!= null){
								et.setCod_dip(rdto.getDiplome().getCodDip());
								et.setVers_dip(rdto.getDiplome().getCodVrsVdi());
							}

							//résultats de l'étape:
							ResultatVetDTO[] tabresetape = etape.getResultatVet();
							if (tabresetape != null && tabresetape.length > 0) {
								for (int k = 0; k < tabresetape.length; k++ ) {
									ResultatVetDTO ret = tabresetape[k];
									Resultat r = new Resultat();
									if(!ret.getEtatDelib().getCodEtaAvc().equals("T")) {
										et.setDeliberationTerminee(false);
									} else {
										et.setDeliberationTerminee(true);
									}

									r.setSession(ret.getSession().getLibSes());
									if(ret.getNatureResultat() != null && ret.getNatureResultat().getCodAdm()!= null && ret.getNatureResultat().getCodAdm().equals("0")){
										//on est en Admissibilité à l'étape.Pas en admission.
										//on le note pour que ce soit plus clair pour l'étudiant
										r.setNote(ret.getNatureResultat().getLibAdm());

									}
									//recuperation de la mention
									if(ret.getMention() != null){
										r.setCodMention(ret.getMention().getCodMen());
										r.setLibMention(ret.getMention().getLibMen());
									}

									String result="";
									if(ret.getTypResultat() != null){
										result = ret.getTypResultat().getCodTre();
										r.setAdmission(result);
									}
									if (ret.getNotVet() != null) {
										r.setNote(ret.getNotVet().toString());
										//ajout note jury
										if(ret.getNotPntJurVet() != null && !ret.getNotPntJurVet().equals(new BigDecimal(0))){
											r.setNote(r.getNote()+"(+"+ret.getNotPntJurVet()+")");
										}

									} else {
										if (result.equals("DEF")) {
											r.setNote("DEF");
										}
									}

									//Gestion du barème:
									if(ret.getBarNotVet() != null){
										r.setBareme(ret.getBarNotVet());
									}

									//ajout de la signification du résultat dans la map
									if (result != null && !result.equals("") && !e.getSignificationResultats().containsKey(r.getAdmission())) {
										e.getSignificationResultats().put(r.getAdmission(), ret.getTypResultat().getLibTre());
									}


									//ajout du résultat par ordre de code session (Juillet 2014)
									//ajout du resultat en fin de liste
									//et.getResultats().add(r);
									try{
										int session = Integer.parseInt(ret.getSession().getCodSes());
										if(et.getResultats().size()>0 && et.getResultats().size()>=session){
											//ajout du résultat à la bonne place dans la liste
											et.getResultats().add((session-1),r);
										}else{
											//ajout du résultat en fin de liste
											et.getResultats().add(r);
										}
									}catch(Exception excep){
										et.getResultats().add(r);
									}

									//ajout du rang
									if(ret.getNbrRngEtuVet() != null && !ret.getNbrRngEtuVet().equals("")){
										et.setRang(ret.getNbrRngEtuVet()+"/"+ret.getNbrRngEtuVetTot());
										//On calcule si on affiche ou non le rang.
										boolean cetteEtapeDoitEtreAffiche=false;
										for(String codetape : configController.getListeCodesEtapeAffichageRang()){
											if(codetape.equals(et.getCode())){
												cetteEtapeDoitEtreAffiche=true;
											}
										}
										if(configController.isAffRangEtudiant() || cetteEtapeDoitEtreAffiche){
											//On affichera le rang de l'étape.
											et.setAfficherRang(true);
											//On remonte au niveau de l'étudiant qu'on affiche le rang
											e.setAfficherRang(true);
										}
									}

								}
							}

							//ajout de l'étape a la liste d'étapes de l'étudiant:
							//e.getEtapes().add(0, et);
							//en attendant la maj du WS :
							insererEtapeDansListeTriee(e, et);

						}
					}
				}

			}
		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error("Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}else{
				LOG.info("Probleme avec le WS lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}
		} catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats pour etudiant dont codetu est : " + e.getCod_etu(),ex);
		}

	}




	private void insererEtapeDansListeTriee(Etudiant e, Etape et){

		boolean insere = false;
		int rang = 0;
		int anneeEtape = new Integer(et.getAnnee().substring(0, 4));
		while(!insere && rang < e.getEtapes().size()){

			int anneeEtapeEnCours = new Integer(e.getEtapes().get(rang).getAnnee().substring(0, 4));
			if(anneeEtape > anneeEtapeEnCours){
				e.getEtapes().add(rang, et);
				insere = true;
			}
			rang++;
		} 
		if(!insere){
			e.getEtapes().add(et);
		}
	}


	/**
	 * Récupère les données retournées par le WS et les trie pour les afficher
	 * @param e etudiant
	 * @param et etape
	 * @param reedto objet retourne par le WS
	 * @param temoinEtatDelib
	 */
	public void setNotesElpEpr(Etudiant e, Etape et, ContratPedagogiqueResultatElpEprDTO4[] reedto,String temoinEtatDelib) {
		try {

			e.getElementsPedagogiques().clear();
			//liste intermédiaire pour trié les éléments pédagogiques:
			List<ElementPedagogique> liste1 = new ArrayList<ElementPedagogique>();


			if (reedto != null && reedto.length > 0) {
				//On parcourt les ELP:
				for (int i = 0; i < reedto.length; i++ ) {

					ElementPedagogique elp = new ElementPedagogique();

					elp.setCode(reedto[i].getElp().getCodElp());
					elp.setLevel(reedto[i].getRngElp());
					elp.setCodElpSup(reedto[i].getCodElpSup());
					elp.setLibelle(reedto[i].getElp().getLibElp());
					elp.setAnnee("");
					elp.setEpreuve(false);


					if (reedto[i].getElp().getNatureElp().getCodNel().equals("FICM")) {
						//utile pour ne pas afficher les FICM par la suite
						elp.setAnnee("FICM");
					}


					elp.setNote1("");
					elp.setBareme1(0);
					elp.setRes1("");
					elp.setNote2("");
					elp.setBareme2(0);
					elp.setRes2("");
					elp.setEcts("");
					elp.setTemFictif(reedto[i].getElp().getNatureElp().getTemFictif());
					elp.setTemSemestre("N");
					elp.setTemSemestre(reedto[i].getElp().getNatureElp().getTemSemestre());
					elp.setEtatDelib("");

					//vrai si l'ELP est il dans un etat de delib qui nous convient en session1:
					boolean elpEtatDelibS1OK=false;

					//vrai si l'ELP est il dans un etat de delib qui nous convient en session2:
					boolean elpEtatDelibS2OK=false;

					//On s'occupe des résultats :
					ResultatElpDTO3[] relpdto = reedto[i].getResultatsElp();
					if (relpdto != null && relpdto.length > 0) {
						//on parcourt les résultats pour l'ELP:
						for (int j = 0; j < relpdto.length; j++ ) {
							if(relpdto[j] != null && relpdto[j].getEtatDelib() != null && relpdto[j].getEtatDelib().getCodEtaAvc()!= null)
								elp.setEtatDelib(relpdto[j].getEtatDelib().getCodEtaAvc());

							//on affiche pas les résultats d'admissibilité
							if(relpdto[j].getNatureResultat()==null || relpdto[j].getNatureResultat().getCodAdm()== null || !relpdto[j].getNatureResultat().getCodAdm().equals("0")){
								//29/01/10
								//On récupère les notes si l'ELP est dans un état de delibération compris dans la liste des témoins paramétrés.
								if(relpdto[j].getEtatDelib()==null ||  temoinEtatDelib.contains(relpdto[j].getEtatDelib().getCodEtaAvc())){

									int codsession = 0;
									if(relpdto[j].getSession() != null){
										codsession = new Integer(relpdto[j].getSession().getCodSes());
									}else{
										//Pour info, on arrive ici car on peut etre en VAC: validation d'acquis
									}

									String result = null;

									//le résultat:
									if (relpdto[j].getTypResultat() != null ) {
										result = relpdto[j].getTypResultat().getCodTre();
									}

									//Test sur la session traitée
									if (codsession < 2) {
										//l'elp est dans un état de delibération compris dans la liste des témoins paramétrés.
										elpEtatDelibS1OK=true;

										//1er session  : juin
										if (relpdto[j].getNotElp() != null && !relpdto[j].getNotElp().equals("null")) {
											elp.setNote1(relpdto[j].getNotElp().toString());
											if(relpdto[j].getNotPntJurElp()!= null && !relpdto[j].getNotPntJurElp().equals(new BigDecimal(0))){
												elp.setNote1(elp.getNote1()+"(+"+relpdto[j].getNotPntJurElp()+")");
											}

										} 
										if ((elp.getNote1() == null || (elp.getNote1() != null && elp.getNote1().equals(""))) && result != null && result.equals("DEF")) {
											elp.setNote1("DEF");
										}

										//Gestion du barème:
										if(relpdto[j].getBarNotElp() != null){
											elp.setBareme1(relpdto[j].getBarNotElp());
										}

										//ajout du rang si pas déjà renseigné via la session de juin.
										if(relpdto[j].getNbrRngEtuElp() != null && !relpdto[j].getNbrRngEtuElp().equals("")
												&& (elp.getRang()==null || elp.getRang().equals(""))){
											elp.setRang(relpdto[j].getNbrRngEtuElp()+"/"+relpdto[j].getNbrRngEtuElpTot());
										}

										//on récupère l'année car si année!=null c'est un PRC  si pas déjà renseigné via la session de juin.
										if(relpdto[j].getCodAnu()!=null && !relpdto[j].getCodAnu().equals("")
												&& (elp.getAnnee()==null || elp.getAnnee().equals(""))){
											elp.setAnnee(relpdto[j].getCodAnu());
										}

										//on recupere les crédits ECTS si pas déjà renseigné via la session de juin.
										if(relpdto[j].getNbrCrdElp()!= null && relpdto[j].getNbrCrdElp().toString()!=null && !relpdto[j].getNbrCrdElp().toString().equals("")
												&& (elp.getEcts()==null || elp.getEcts().equals(""))){
											elp.setEcts(relpdto[j].getNbrCrdElp().toString());
										}

										elp.setRes1(result);
									} else {
										//2em session  : septembre
										//l'elp est dans un état de delibération compris dans la liste des témoins paramétrés.
										elpEtatDelibS2OK=true;

										if (relpdto[j].getNotElp() != null && !relpdto[j].getNotElp().equals("null")) {
											elp.setNote2(relpdto[j].getNotElp().toString());
											if(relpdto[j].getNotPntJurElp()!= null && !relpdto[j].getNotPntJurElp().equals(new BigDecimal(0))){
												elp.setNote2(elp.getNote2()+"(+"+relpdto[j].getNotPntJurElp()+")");
											}
										}
										if ((elp.getNote2() == null || (elp.getNote2() != null && elp.getNote2().equals(""))) && result != null && result.equals("DEF")) {
											elp.setNote2("DEF");
										}

										//Gestion du barème:
										if(relpdto[j].getBarNotElp()!= null){
											elp.setBareme2(relpdto[j].getBarNotElp());
										}

										//ajout du rang
										if(relpdto[j].getNbrRngEtuElp() != null && !relpdto[j].getNbrRngEtuElp().equals("")){
											elp.setRang(relpdto[j].getNbrRngEtuElp()+"/"+relpdto[j].getNbrRngEtuElpTot());
										}
										//on récupère l'année car si getCodAnu()!=null c'est un PRC
										if(relpdto[j].getCodAnu()!=null && !relpdto[j].getCodAnu().equals("")){
											elp.setAnnee(relpdto[j].getCodAnu());
										}
										//on recupere les crédits ECTS 
										if(relpdto[j].getNbrCrdElp()!= null && relpdto[j].getNbrCrdElp().toString()!=null && !relpdto[j].getNbrCrdElp().toString().equals("")){
											elp.setEcts(relpdto[j].getNbrCrdElp().toString());
										}
										elp.setRes2(result);
									}



									//CAS DE NON OBTENTION PAR CORRESPONDANCE.
									if(relpdto[j].getLcc() == null) {

										//ajout de la signification du résultat dans la map
										if (result != null && !result.equals("") && !e.getSignificationResultats().containsKey(result)) {
											e.getSignificationResultats().put(result, relpdto[j].getTypResultat().getLibTre());
										}

									}
								}
							}
							//On affiche la correspondance meme si l'état de délibération n'est pas compris dans la liste des témoins paramétrés.
							if(relpdto[j].getLcc() != null) {
								//les notes ont été obtenues par correspondance a session 1.
								elp.setNote1("COR");
								//ajout de la signification du résultat dans la map
								if ( !e.getSignificationResultats().containsKey("COR")) {
									e.getSignificationResultats().put("COR",SIGNIFICATION_TYP_RESULT_COR);
								}
							}

						}
					}


					//ajout de l'élément dans la liste par ordre alphabétique:
					//liste1.add(elp);
					if (liste1.size() == 0) {
						liste1.add(elp);
					} else {
						int rang = 0;
						boolean insere = false;
						while (rang < liste1.size() && !insere) {

							if (liste1.get(rang).getCode().compareTo(elp.getCode()) > 0) {
								liste1.add(rang, elp);
								insere = true;
							}

							if (!insere) {
								rang++;
							}
						}
						if (!insere) {
							liste1.add(elp);
						}
					}



					//les epreuves de l'élément (si il y en a )
					EpreuveElpDTO2[] epelpdto = reedto[i].getEpreuvesElp();

					if (epelpdto != null && epelpdto.length > 0) {

						for (int j = 0; j < epelpdto.length; j++ ) {
							EpreuveElpDTO2 epreuve = epelpdto[j];
							boolean EprNotee = false;  //vrai si l'épreuve est notée
							ElementPedagogique elp2 = new ElementPedagogique();
							elp2.setLibelle(epreuve.getEpreuve().getLibEpr());
							elp2.setCode(epreuve.getEpreuve().getCodEpr());
							elp2.setLevel(elp.getLevel() + 1);

							//Modif 20/02/2012 pour les WS HttpInvoker
							//elp2.setAnnee("epreuve");
							elp2.setAnnee("");
							elp2.setEpreuve(true);

							elp2.setCodElpSup(elp.getCode());
							elp2.setNote1("");
							elp2.setBareme1(0);
							elp2.setRes1("");
							elp2.setNote2("");
							elp2.setBareme2(0);
							elp2.setRes2("");
							ResultatEprDTO[] repdto = epreuve.getResultatEpr();
							//29/01/10
							//On récupère le témoin TemCtlValCadEpr de l'épreuve
							String TemCtlValCadEpr = epreuve.getEpreuve().getTemCtlValCadEpr();

							if (repdto != null && repdto.length > 0) {
								for (int k = 0; k < repdto.length; k++ ) {
									int codsession = new Integer(repdto[k].getSession().getCodSes());
									//09/01/13
									//On recupere la note si :
									//  On a reseigné une liste de type épreuve à afficher et le type de l'épreuve en fait partie
									//  OU SI :
									//      le témoin d'avc fait partie de la liste des témoins paramétrés 
									//      OU si le témoin d'avc de  l'elp pere fait partie de la liste des témoins paramétrés 
									//      OU si le témoin TemCtlValCadEpr est égal au parametre TemoinCtlValCadEpr de monDossierWeb.xml.
									boolean recuperationNote = false;

									if(configController.getTypesEpreuveAffichageNote() != null && configController.getTypesEpreuveAffichageNote().size()>0){
										//On a renseigné une liste de type épreuve à afficher
										for(String typeEpreuve : configController.getTypesEpreuveAffichageNote()){
											if(typeEpreuve.equals(epreuve.getEpreuve().getTypEpreuve().getCodTep())){
												recuperationNote = true;
											}
										}
									}
									if(!recuperationNote){
										//On n'a pas renseigné de liste de type épreuve à afficher ou celui ci n'était pas dans la liste
										if (codsession < 2) {
											if(temoinEtatDelib.contains(repdto[k].getEtatDelib().getCodEtaAvc()) || elpEtatDelibS1OK || TemCtlValCadEpr.equals(configController.getTemoinCtlValCadEpr()))
												recuperationNote = true;
										}else{
											if(temoinEtatDelib.contains(repdto[k].getEtatDelib().getCodEtaAvc()) || elpEtatDelibS2OK || TemCtlValCadEpr.equals(configController.getTemoinCtlValCadEpr()))
												recuperationNote = true;
										}
									}
									//test si on recupere la note ou pas
									if(recuperationNote){


										if (codsession < 2) {
											//1er session  : juin
											if (repdto[k].getNotEpr() != null) {
												elp2.setNote1(repdto[k].getNotEpr().replaceAll(",", "."));

												//Gestion du barème:
												if(repdto[k].getBarNotEpr() != null){
													elp2.setBareme1(repdto[k].getBarNotEpr());
												}
											}
											if (elp2.getNote1() != null && !elp2.getNote1().equals("")) {
												EprNotee = true;
											}


										} else {
											//2er session  : septembre
											if (repdto[k].getNotEpr() != null) {
												elp2.setNote2(repdto[k].getNotEpr().replaceAll(",", "."));

												//Gestion du barème:
												if(repdto[k].getBarNotEpr() != null){
													elp2.setBareme2(repdto[k].getBarNotEpr());
												}
											}
											if (elp2.getNote2() != null && !elp2.getNote2().equals("")) {
												EprNotee = true;
											}
										}
									}
								}
							}
							//ajout de l'épreuve dans la liste en tant qu'élément si elle a une note
							if (EprNotee) {
								liste1.add(elp2);
							}
						}
					}
				}
			}
			//ajout des éléments dans la liste de l'étudiant en commençant par la ou les racine
			int niveauRacine = 1;
			if (liste1.size() > 0) {
				int i = 0;
				while (i < liste1.size()) {
					ElementPedagogique el = liste1.get(i);
					if (el.getCodElpSup() == null || el.getCodElpSup().equals("")) {
						//on a une racine:
						if (!el.getAnnee().equals("FICM")) {
							e.getElementsPedagogiques().add(el);
						}

						insererElmtPedagoFilsDansListe(el, liste1, e, niveauRacine);
					}
					i++;
				}
			}


			//suppression des épreuve seules et quand elles ont les mêmes notes que l'element pere:
			if (e.getElementsPedagogiques().size() > 0) {
				int i = 1;
				boolean suppr = false;
				while (i < e.getElementsPedagogiques().size()) {
					suppr = false;
					ElementPedagogique elp = e.getElementsPedagogiques().get(i);
					if (elp.isEpreuve()) {
						ElementPedagogique elp0 = e.getElementsPedagogiques().get(i - 1);
						if (i < (e.getElementsPedagogiques().size() - 1)) {
							ElementPedagogique elp1 = e.getElementsPedagogiques().get(i + 1);
							if (!elp0.isEpreuve() && !elp1.isEpreuve()) {
								if (elp0.getNote1().equals(elp.getNote1()) && elp0.getNote2().equals(elp.getNote2())) {
									//on supprime l'element i
									e.getElementsPedagogiques().remove(i);
									suppr = true;
								}
							}
						} else {
							if (!elp0.isEpreuve() && elp0.getNote1().equals(elp.getNote1()) && elp0.getNote2().equals(elp.getNote2())) {
								//on supprime l'element i
								e.getElementsPedagogiques().remove(i);
								suppr = true;
							}
						}
					}
					if (!suppr) {
						i++;
					}
				}
			}



			//Gestion des temoins fictif si temoinFictif est renseigné dans monDossierWeb.xml
			if(configController.getTemoinFictif()!=null && !configController.getTemoinFictif().equals("")){
				if (e.getElementsPedagogiques().size() > 0) {
					List<Integer> listeRangAsupprimer=new LinkedList<Integer>();
					int rang = 0;
					//on note les rangs des éléments à supprimer
					for (ElementPedagogique el : e.getElementsPedagogiques()) {
						if(el.getTemFictif()!= null && !el.getTemFictif().equals("") && !el.getTemFictif().equals(configController.getTemoinFictif())){
							//on supprime l'élément de la liste
							listeRangAsupprimer.add(rang);
						}
						rang++;
					}
					//on supprime les éléments de la liste
					int NbElementSupprimes = 0;
					for(Integer rg:listeRangAsupprimer){
						e.getElementsPedagogiques().remove(rg - NbElementSupprimes);
						NbElementSupprimes++;
					}
				}
			}

			//Gestion de la descendance des semestres si temNotesEtuSem est renseigné et à true dans monDossierWeb.xml
			if(configController.isTemNotesEtuSem()){
				if (e.getElementsPedagogiques().size() > 0) {
					List<Integer> listeRangAsupprimer=new LinkedList<Integer>();
					int rang = 0;

					int curSemLevel = 0;
					boolean supDesc = false;

					//on note les rangs des éléments à supprimer
					for (ElementPedagogique el : e.getElementsPedagogiques()) {
						if(el.getTemSemestre()!= null && !el.getTemSemestre().equals("") && el.getTemSemestre().equals("O")) {
							curSemLevel = new Integer(el.getLevel());
							supDesc = el.getEtatDelib()!= null && !el.getEtatDelib().equals("") && !el.getEtatDelib().equals("T");
						} else if(el.getLevel() <= curSemLevel) {
							supDesc = false;
						}

						if(supDesc && el.getLevel() > curSemLevel){
							//on supprime l'élément de la liste
							listeRangAsupprimer.add(rang);
						}
						rang++;
					}
					//on supprime les éléments de la liste
					int NbElementSupprimes = 0;
					for(Integer rg:listeRangAsupprimer){
						e.getElementsPedagogiques().remove(rg - NbElementSupprimes);
						NbElementSupprimes++;
					}
				}
			}

			//ajout de l'étape sélectionnée en début de liste:
			ElementPedagogique ep = new ElementPedagogique();
			ep.setAnnee(et.getAnnee());
			ep.setCode(et.getCode());
			ep.setLevel(1);
			ep.setLibelle(et.getLibelle());
			e.setDeliberationTerminee(et.isDeliberationTerminee());
			if (et.getResultats().size() > 0) {
				if (et.getResultats().get(0).getNote() != null)
					ep.setNote1(et.getResultats().get(0).getNote().toString());
				if (et.getResultats().get(0).getAdmission() != null)
					ep.setRes1(et.getResultats().get(0).getAdmission());
			}
			if (et.getResultats().size() > 1) {
				if (et.getResultats().get(1).getNote() != null)
					ep.setNote2(et.getResultats().get(1).getNote().toString());
				if (et.getResultats().get(1).getAdmission() != null)
					ep.setRes2(et.getResultats().get(1).getAdmission());
			}
			e.getElementsPedagogiques().add(0, ep);

		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}else{
				LOG.info("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}
		}catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
		}
	}

	/**
	 * ajoute les éléments dans la liste d'éléments de l'étudiant en corrigeant les levels (rangs).
	 * @param elp
	 * @param liste1
	 * @param e
	 * @param niveauDuPere 
	 */
	protected void insererElmtPedagoFilsDansListe(ElementPedagogique elp, List<ElementPedagogique> liste1, Etudiant e, int niveauDuPere) {
		for (ElementPedagogique el : liste1) {
			if (el.getCodElpSup() != null && !el.getCodElpSup().equals("")) {
				if (el.getCodElpSup().equals(elp.getCode()) && !el.getCode().equals(elp.getCode())) {
					//on affiche pas les FICM :
					if (!el.getAnnee().equals("FICM")) {
						el.setLevel(niveauDuPere + 1);
						e.getElementsPedagogiques().add(el);
					}
					//On test si on est pas sur une epreuve pour eviter les boucle infini dans le cas ou codEpr=CodElpPere
					if(!el.getAnnee().equals("epreuve"))
						insererElmtPedagoFilsDansListe(el, liste1, e, niveauDuPere + 1);
				}
			}
		}
	}





	/**
	 * va chercher et renseigne les informations concernant les notes
	 * et résultats des éléments de l'etape choisie
	 * de l'étudiant placé en paramètre via le WS de l'Amue.
	 */
	public void recupererDetailNotesEtResultats(Etudiant e,Etape et){
		try {

			if(monProxyPedagogique==null)
				monProxyPedagogique = new PedagogiqueMetierServiceInterfaceProxy();

			e.getElementsPedagogiques().clear();

			String temoin = configController.getTemoinNotesEtudiant();
			if(temoin == null || temoin.equals("")){
				temoin="T";
			}
			String sourceResultat = PropertyUtils.getSourceResultats();
			if(sourceResultat == null || sourceResultat.equals("")){
				sourceResultat="Apogee";
			}

			// VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
			// Si different annee en cours => sourceResultat = Apogee
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				String annee = getAnneeUnivEnCours(GenericUI.getCurrent());
				if (et.getAnnee().substring(0, 4).compareTo(annee)==0) {
					sourceResultat="Apogee-extraction";
					temoin=null;
				} else {
					sourceResultat="Apogee";
				}
			}
			//07/09/10
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				//07/09/10
				//on prend le témoin pour Apogee-extraction
				ContratPedagogiqueResultatElpEprDTO4[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatElpEpr_v5(e.getCod_etu(), et.getAnnee().substring(0, 4), et.getCode(), et.getVersion(), sourceResultat, temoin, "toutes", "tous");
				//29/01/10
				//on est dans le cas d'une extraction apogée
				setNotesElpEpr(e, et, cpdto,"AET");
			}else{
				//29/01/10
				//On récupère pour tout les états de délibération et on fera le trie après
				ContratPedagogiqueResultatElpEprDTO4[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatElpEpr_v5(e.getCod_etu(), et.getAnnee().substring(0, 4), et.getCode(), et.getVersion(), sourceResultat, "AET", "toutes", "tous");
				setNotesElpEpr(e, et, cpdto,temoin);
			}



		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}else{
				LOG.info(ex.getLastErrorMsg()+" Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
			}
		} catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats a une étape pour etudiant dont codetu est : " + e.getCod_etu(),ex);
		}
	}

	/**
	 * va chercher et renseigne les notes de
	 * l'étudiant via le WS de l'Amue.
	 */
	public void recupererDetailNotesEtResultatsEnseignant(Etudiant e,Etape et){
		try {

			if(monProxyPedagogique==null)
				monProxyPedagogique = new PedagogiqueMetierServiceInterfaceProxy();

			e.getElementsPedagogiques().clear();

			String temoin = configController.getTemoinNotesEnseignant();
			if(temoin == null || temoin.equals("")){
				temoin="AET";
			}
			String sourceResultat = PropertyUtils.getSourceResultats();
			if(sourceResultat == null || sourceResultat.equals("")){
				sourceResultat="Apogee";
			}

			// VR 09/11/2009 : Verif annee de recherche si sourceResultat = apogee-extraction :
			// Si different annee en cours => sourceResultat = Apogee
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				String annee = getAnneeUnivEnCours(GenericUI.getCurrent());
				if (et.getAnnee().substring(0, 4).compareTo(annee)==0) {
					sourceResultat="Apogee-extraction";
					temoin=null;
				} else {
					sourceResultat="Apogee";
				}
			}

			// 07/12/11 récupération du fonctionnement identique à la récupéraition des notes pour les étudiants.
			if(sourceResultat.compareTo("Apogee-extraction")==0){
				ContratPedagogiqueResultatElpEprDTO4[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatElpEpr_v5(e.getCod_etu(), et.getAnnee().substring(0, 4), et.getCode(), et.getVersion(), sourceResultat, temoin, "toutes", "tous");
				setNotesElpEpr(e, et, cpdto,"AET");
			}else{
				ContratPedagogiqueResultatElpEprDTO4[] cpdto = monProxyPedagogique.recupererContratPedagogiqueResultatElpEpr_v5(e.getCod_etu(), et.getAnnee().substring(0, 4), et.getCode(), et.getVersion(), sourceResultat, "AET", "toutes", "tous");
				setNotesElpEpr(e, et, cpdto,temoin);
			}



		} catch (WebBaseException ex) {
			//Si on est dans un cas d'erreur non expliqué
			if (ex.getNature().equals("technical.ws.remoteerror.global")){
				LOG.error("Probleme avec le WS lors de la recherche des notes et résultats a une étape pour etudiant dont codind est : " + e.getCod_ind(),ex);
			}else{
				LOG.info(ex.getLastErrorMsg()+" pour etudiant dont codind est : " + e.getCod_ind() + " recupererDetailNotesEtResultatsEnseignant("+et.getAnnee()+ ","+et.getCode()+"/"+et.getVersion()+")");
			}
		}catch (Exception ex) {
			LOG.error("Probleme lors de la recherche des notes et résultats a une étape pour etudiant dont codind est : " + e.getCod_ind(),ex);
		}

	}

	public void renseigneNotesEtResultats(Etudiant e) {
		//On regarde si on a pas déjà les infos dans le cache:
		String rang = getRangNotesEtResultatsEnCache(true,e);

		if(rang == null){
			recupererNotesEtResultats(e);
			//AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
			ajouterCacheResultatVdiVet(true,e);
		}else{
			//on récupére les infos du cache grace au rang :
			recupererCacheResultatVdiVet(new Integer(rang),e);
		}
	}

	public void renseigneNotesEtResultatsVueEnseignant(Etudiant e) {
		//On regarde si on a pas déjà les infos dans le cache:
		String rang = getRangNotesEtResultatsEnCache(false,e);
		if(rang == null){
			recupererNotesEtResultatsEnseignant(e);
			//AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
			ajouterCacheResultatVdiVet(false,e);
		}else{
			//on récupére les infos du cache grace au rang :
			recupererCacheResultatVdiVet(new Integer(rang),e);
		}
	}

	public void renseigneDetailNotesEtResultats(Etape etape) {
		//On regarde si on a pas déjà les infos dans le cache:
		String rang = getRangDetailNotesEtResultatsEnCache(etape,true,GenericUI.getCurrent().getEtudiant());

		if(rang == null){
			recupererDetailNotesEtResultats(GenericUI.getCurrent().getEtudiant(),etape);
			//AJOUT DES INFOS recupérées dans le cache. true car on est en vue Etudiant
			ajouterCacheDetailNotesEtResultats(etape,true,GenericUI.getCurrent().getEtudiant());
		}else{
			//on récupére les infos du cache grace au rang :
			recupererCacheDetailNotesEtResultats(new Integer(rang),GenericUI.getCurrent().getEtudiant());
		}
	}

	public void renseigneDetailNotesEtResultatsEnseignant(Etape etape) {
		//On regarde si on a pas déjà les infos dans le cache:
		String rang = getRangDetailNotesEtResultatsEnCache(etape,false,GenericUI.getCurrent().getEtudiant());
		if(rang == null){
			recupererDetailNotesEtResultatsEnseignant(GenericUI.getCurrent().getEtudiant(),etape);
			//AJOUT DES INFOS recupérées dans le cache. false car on est en vue Enseignant
			ajouterCacheDetailNotesEtResultats(etape,false,GenericUI.getCurrent().getEtudiant());
		}else{
			//on récupére les infos du cache grace au rang :
			recupererCacheDetailNotesEtResultats(new Integer(rang),GenericUI.getCurrent().getEtudiant());
		}
	}



	/* 
	 * @param etape
	 * @param vueEtudiant
	 * @return  le rang dans la liste des Notes et Résultat (aux elp et epr) en cache pour la vueEtudiant
	 */
	private String getRangDetailNotesEtResultatsEnCache(Etape etape, boolean vueEtudiant, Etudiant e){
		int rang=0;
		boolean enCache=false;

		//on parcourt le résultatElpEpr pour voir si on a ce qu'on cherche:
		for(CacheResultatsElpEpr cree : e.getCacheResultats().getResultElpEpr()){
			if(!enCache){
				//si on a déjà les infos:
				if(cree.getEtape().getAnnee().equals(etape.getAnnee())
						&& cree.getEtape().getCode().equals(etape.getCode())
						&& cree.getEtape().getVersion().equals(etape.getVersion())
						&& cree.isVueEtudiant() == vueEtudiant){
					enCache=true;
				}else{
					//on a pas trouvé, on incrémente le rang pour se placer sur le rang suivant
					rang++;
				}
			}
		}

		//si on a pas les infos en cache:
		if(!enCache){
			return null;
		}

		return ""+rang;

	}

	/**
	 * 
	 * @param vueEtudiant
	 * @return le rang dans la liste des Notes et Résultat (aux diplomes et étapes) en cache pour la vueEtudiant
	 */
	private String getRangNotesEtResultatsEnCache(boolean vueEtudiant, Etudiant e){
		int rang=0;
		boolean enCache=false;

		//on parcourt le résultatVdiVet pour voir si on a ce qu'on cherche:
		if(e.getCacheResultats()!=null && e.getCacheResultats().getResultVdiVet()!=null){
			for(CacheResultatsVdiVet crvv : e.getCacheResultats().getResultVdiVet()){
				if(!enCache){
					//si on a déjà les infos:
					if(crvv.isVueEtudiant() == vueEtudiant){
						enCache=true;
					}else{
						//on a pas trouvé, on incrémente le rang pour se placer sur le rang suivant
						rang++;
					}
				}
			}
		}
		//si on a pas les infos en cache:
		if(!enCache){
			return null;
		}

		return ""+rang;

	}


	/**
	 * On complète les infos du cache pour les Résultats aux diplomes et étapes.
	 * @param vueEtudiant
	 */
	public void ajouterCacheResultatVdiVet(boolean vueEtudiant, Etudiant e){
		CacheResultatsVdiVet crvv = new CacheResultatsVdiVet();
		crvv.setVueEtudiant(vueEtudiant);
		crvv.setDiplomes(new LinkedList<Diplome>(e.getDiplomes()));
		crvv.setEtapes(new LinkedList<Etape>(e.getEtapes()));
		e.getCacheResultats().getResultVdiVet().add(crvv);
	}

	/**
	 * On complète les infos du cache pour les Résultats aux elp et epr.
	 * @param vueEtudiant
	 */
	public void ajouterCacheDetailNotesEtResultats(Etape etape, boolean vueEtudiant, Etudiant e){
		CacheResultatsElpEpr cree = new CacheResultatsElpEpr();
		cree.setVueEtudiant(vueEtudiant);
		cree.setEtape(etape);
		if(e.getElementsPedagogiques()!=null && e.getElementsPedagogiques().size()>0){
			cree.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getElementsPedagogiques()));
		}
		e.getCacheResultats().getResultElpEpr().add(cree);
	}


	/**
	 * récupère les résultat aux diplomes et etapes dans le cache (en s'indexant sur le rang)
	 * @param rang
	 */
	private void recupererCacheResultatVdiVet(int rang, Etudiant e){
		//1-on vide les listes existantes
		if(e.getDiplomes()!=null){
			e.getDiplomes().clear();
		}
		if(e.getEtapes()!=null){
			e.getEtapes().clear();
		}
		//2-on récupère les infos du cache.
		if(e.getCacheResultats().getResultVdiVet().get(rang).getDiplomes()!=null){
			e.setDiplomes(new LinkedList<Diplome>(e.getCacheResultats().getResultVdiVet().get(rang).getDiplomes()));
		}
		if(e.getCacheResultats().getResultVdiVet().get(rang).getEtapes()!=null){
			e.setEtapes(new LinkedList<Etape>(e.getCacheResultats().getResultVdiVet().get(rang).getEtapes()));
		}

	}

	/**
	 * récupère les résultat aux Elp et Epr dans le cache (en s'indexant sur le rang)
	 * @param rang
	 */
	private void recupererCacheDetailNotesEtResultats(int rang, Etudiant e){
		//1-on vide la liste existante
		if(e.getElementsPedagogiques()!=null){
			e.getElementsPedagogiques().clear();
		}

		//2-on récupère les infos du cache.
		if(e.getCacheResultats().getResultElpEpr().get(rang).getElementsPedagogiques()!=null){
			e.setElementsPedagogiques(new LinkedList<ElementPedagogique>(e.getCacheResultats().getResultElpEpr().get(rang).getElementsPedagogiques()));
		}

	}


	public void changerVueNotesEtResultats() {
		if(GenericUI.getCurrent().isVueEnseignantNotesEtResultats()){
			GenericUI.getCurrent().setVueEnseignantNotesEtResultats(false);
		}else{
			GenericUI.getCurrent().setVueEnseignantNotesEtResultats(true);
		}
	}


	public boolean proposerCertificat(Inscription ins, Etudiant etu) {

		// autoriser ou non la generation de certificats de scolarite.
		if (!configController.isCertificatScolaritePDF()) {
			return false;
		}
		// autoriser ou non les personnels à imprimer les certificats.
		if ( !configController.isCertScolAutorisePersonnel() && userController.isEnseignant()) {
			return false;
		}
		String codAnuIns=ins.getCod_anu().substring(0, 4);
		// autorise l'édition de certificat de scolarité uniquement pour l'année en cours.
		if (!configController.isCertificatScolariteTouteAnnee() && !codAnuIns.equals(getAnneeUnivEnCours(GenericUI.getCurrent()))) {
			return false;
		}
		List<String> listeCertScolTypDiplomeDesactive=configController.getListeCertScolTypDiplomeDesactive();
		if ( listeCertScolTypDiplomeDesactive!=null || !listeCertScolTypDiplomeDesactive.isEmpty()) {
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



		return true;
	}




	public boolean isAfficherRangElpEpr(){
		List<ElementPedagogique> lelp = GenericUI.getCurrent().getEtudiant().getElementsPedagogiques();
		if(lelp != null && lelp.size()>0){
			List<String> codesAutorises = configController.getListeCodesEtapeAffichageRang();
			String codeEtpEnCours = lelp.get(0).getCode();
			for(String code : codesAutorises){
				if(code.equals(codeEtpEnCours)){
					return true;
				}
			}
		}
		return false;
	}


	public List<String> updateContact(String telephone, String mail,String codetu) {
		List<String> retour = new LinkedList<String>();
		boolean erreur = false;
		String message = "";
		if(StringUtils.hasText(telephone) && !Pattern.matches("[0-9[. ]]*", telephone)){
			message = applicationContext.getMessage("modificationContact.erreur.tel", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}
		if(StringUtils.hasText(mail) && !Pattern.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]{2,}[.][a-zA-Z]{2,4}$", mail)){
			message = applicationContext.getMessage("modificationContact.erreur.mail", null, Locale.getDefault());
			retour.add(message);
			erreur = true;
		}

		//Si aucune erreur sur les données insérées
		if(!erreur){
			boolean succes = false;
			//On insere dans Apogée
			if(monProxyEtu==null)
				monProxyEtu = new EtudiantMetierServiceInterfaceProxy();
			if(monProxyAdministratif==null)
				monProxyAdministratif = new AdministratifMetierServiceInterfaceProxy();

			try {
				//recup de l'ancienne et modif dessus:
				String[] annees =  monProxyAdministratif.recupererAnneesIa(codetu, null);
				//récupération de l'année la plus récente
				String annee = "0";
				for(int i=0; i<annees.length;i++){
					if (Integer.parseInt(annees[i])>Integer.parseInt(annee)){
						annee = annees[i];
					}
				}
				CoordonneesDTO2 cdto = monProxyEtu.recupererAdressesEtudiant_v2(codetu, annee , "N");


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

				LOG.debug("==== MAJ ADRESSE ==="+cdto.getAnnee()+" "+cdto.getTypeHebergement().getCodTypeHebergement());
				monProxyEtu.mettreAJourAdressesEtudiant(cdtomaj, codetu);

				succes = true;
			} catch (WebBaseException ex) {
				LOG.error("Probleme avec le WS lors de la maj des adresses de l'etudiant dont codetu est : " + codetu,ex);
			} catch (Exception ex) {
				LOG.error("Probleme avec le WS lors de la maj des adresses de l'etudiant dont codetu est : " + codetu,ex);
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
