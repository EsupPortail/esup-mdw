package fr.univlorraine.mondossierweb.controllers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.Getter;
import lombok.Setter;

import org.jfree.util.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;
import fr.univlorraine.mondossierweb.photo.IPhoto;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.Utils;
import gouv.education.apogee.commun.transverse.dto.scolarite.CollectionDTO3;
import gouv.education.apogee.commun.transverse.dto.scolarite.ElementPedagogiDTO2;
import gouv.education.apogee.commun.transverse.dto.scolarite.GroupeDTO2;
import gouv.education.apogee.commun.transverse.dto.scolarite.RecupererGroupeDTO2;
import gouv.education.apogee.commun.servicesmetiers.OffreFormationMetierServiceInterface;
import gouv.education.apogee.commun.client.ws.offreformationmetier.OffreFormationMetierServiceInterfaceProxy;

/**
 * Gestion de la recherche
 */
@Component
public class ListeInscritsController {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserDetailsService userDetailsService;
	@Resource
	private transient UiController uiController;
	@Resource
	private MultipleApogeeService multipleApogeeService;
	@Resource
	private ElementPedagogiqueService elementPedagogiqueService;
	@Resource(name="emailConverter")
	private transient EmailConverterInterface emailConverter;
	@Resource
	private transient EtudiantController etudiantController;
	/**
	 * proxy pour faire appel aux infos sur l'étudiant WS .
	 */
	private OffreFormationMetierServiceInterface monProxyOffreDeFormation;
	@Resource(name="photoProvider")
	private IPhoto photo;



	/**
	 * Récupération des inscrits à une VET ou un ELP
	 * @param parameterMap
	 * @param annee
	 */
	public void recupererLaListeDesInscrits(Map<String, String> parameterMap, String annee){
		String code = parameterMap.get("code");
		String type = parameterMap.get("type");

		if (type.equals(Utils.VET)) {


			initMainUIAttributesValues(code, type,annee);

			List<Inscrit> listeInscrits = null;

			//On part d'une Etape pour établir une liste d'étudiant
			Etape e = new Etape();
			if(annee==null){
				e.setCode(code.split("/")[0]);
				e.setVersion(code.split("/")[1]);
				List<String> annees = multipleApogeeService.getAnneesFromVetDesc(e,Integer.parseInt(etudiantController.getAnneeUnivEnCours()));
				MainUI.getCurrent().setListeAnneeInscrits(annees);
				//On prend l'année la plus récente (la premiere de la liste)
				e.setAnnee(annees.get(0));
				MainUI.getCurrent().setAnneeInscrits(e.getAnnee());
				e.setLibelle(multipleApogeeService.getLibelleEtape(e));
				MainUI.getCurrent().setEtapeListeInscrits(e);
			}else{
				e = MainUI.getCurrent().getEtapeListeInscrits();
				e.setAnnee(annee);
				MainUI.getCurrent().setEtapeListeInscrits(e);
				MainUI.getCurrent().setAnneeInscrits(e.getAnnee());
			}


			listeInscrits = (List<Inscrit>) multipleApogeeService.getInscritsEtapeJuinSep(e);

			finaliserListeInscrits(listeInscrits, null,annee);

		} else {
			recupererLaListeDesInscritsELP(parameterMap, annee);

		}


	}


	/**
	 * Récupération des inscrits à un ELP
	 * @param parameterMap
	 * @param annee
	 * @param etape
	 * @param groupe
	 */
	public void recupererLaListeDesInscritsELP(Map<String, String> parameterMap, String annee) {

		String code = parameterMap.get("code");
		String type = parameterMap.get("type");

		initMainUIAttributesValues(code, type,annee);

		List<Inscrit> listeInscrits = null;

		//On part d'une Etape pour établir une liste d'étudiant
		ElementPedagogique e = new ElementPedagogique();
		if(annee==null){
			e.setCode(code);
			List<String> annees = multipleApogeeService.getDixDernieresAnneesUniversitaires();
			MainUI.getCurrent().setListeAnneeInscrits(annees);
			//On prend l'année la plus récente (la premiere de la liste)
			annee = annees.get(0);
			e.setAnnee(annees.get(0));
			MainUI.getCurrent().setAnneeInscrits(e.getAnnee());
			e.setLibelle(elementPedagogiqueService.getLibelleElp(code));
			MainUI.getCurrent().setElpListeInscrits(e);
		}else{
			e = MainUI.getCurrent().getElpListeInscrits();
			e.setAnnee(annee);
			MainUI.getCurrent().setElpListeInscrits(e);
			MainUI.getCurrent().setAnneeInscrits(e.getAnnee());
		}

		//Récupération de tous les inscrit à l'ELP quelque soit l'étape d'appartenance choisie dans la vue ListeInscritView
		listeInscrits = (List<Inscrit>) elementPedagogiqueService.getInscritsFromElp(code, annee);


		List<VersionEtape> letape = null;
		//test si on a des inscrits
		if(listeInscrits!=null && listeInscrits.size()>0){
			letape = new LinkedList<VersionEtape>();
			//Pour chaque inscrit
			for(Inscrit i : listeInscrits){
				//Test si l'étape est renseignée pour l'inscrit
				if(StringUtils.hasText(i.getCod_etp()) && StringUtils.hasText(i.getCod_vrs_vet()) && StringUtils.hasText(i.getLib_etp())){
					VersionEtape vet = new VersionEtape();
					VersionEtapePK vetpk = new VersionEtapePK();
					vetpk.setCod_etp(i.getCod_etp());
					vetpk.setCod_vrs_vet(i.getCod_vrs_vet());
					vet.setId(vetpk);
					vet.setLib_web_vet(i.getLib_etp());
					if(!letape.contains(vet)){
						letape.add(vet);
					}
				}
			}
		}
		MainUI.getCurrent().setListeEtapesInscrits(letape);
		MainUI.getCurrent().setEtapeInscrits(null);


		//Récupération des groupes
		List<ElpDeCollection> listeGroupes = recupererGroupes(annee, code);
		if(listeGroupes!=null && listeGroupes.size()>0){
			MainUI.getCurrent().setListeGroupesInscrits(listeGroupes);
		}

		finaliserListeInscrits(listeInscrits,listeGroupes,annee);

	}

	/**
	 * initialise les attributs de MainUI utilisés dans a vue listeInscritsView
	 * @param code
	 * @param type
	 * @param annee
	 */
	private void initMainUIAttributesValues(String code, String type, String annee) {
		if(MainUI.getCurrent().getListeInscrits()!=null){
			MainUI.getCurrent().getListeInscrits().clear();
		}

		if(annee==null){
			MainUI.getCurrent().setAnneeInscrits(null);
			MainUI.getCurrent().setListeAnneeInscrits(null);
		}
		MainUI.getCurrent().setEtapeListeInscrits(null);
		MainUI.getCurrent().setEtapeInscrits(null);
		MainUI.getCurrent().setGroupeInscrits(null);
		MainUI.getCurrent().setListeEtapesInscrits(null);
		MainUI.getCurrent().setListeGroupesInscrits(null);

		MainUI.getCurrent().setCodeObjListInscrits(code);
		MainUI.getCurrent().setTypeObjListInscrits(type);

	}

	/**
	 * renseigne les logins de chaque inscrit.
	 *
	 */
	/*	private void setLoginInscrits( List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etu()!=null)
			i.setLogin(service.getLoginFromCodEtu(i.getCod_etu()));
		}
	}*/

	/**
	 * Finalise une liste d'inscrits pour affichage dans la vue listeInscritsView
	 * @param listeInscrits
	 */
	private void finaliserListeInscrits(List<Inscrit> listeInscrits,List<ElpDeCollection> listeGroupes, String annee) {

		if(listeInscrits!=null && listeInscrits.size()>0){
			//setLoginInscrits(listeInscrits);
			setIdEtpInscrits(listeInscrits);
			setMailInscrits(listeInscrits);
			setUrlPhotos(listeInscrits);
		}

		//on vérifie que les photo sont récupérées pour savoir si on peut afficher le lien vers le trombinoscope:
		/*if(listeInscrits != null && listeInscrits.size() > 0) {
			listeInscrits.get(0).setUrlphoto(photo.getUrlPhoto(listeInscrits.get(0).getCod_ind(), listeInscrits.get(0).getCod_etu()));
			if (listeInscrits.get(0).getUrlphoto() != null && !listeInscrits.get(0).getUrlphoto().equals("")) {
				photosValides = true;
			}
		}*/


		//On parcourt les groupes, on recup les inscrit puis 
		//pour chaque inscrit on ajoute les id des groupes auxquels il appartient dans un attribut ";codgpe;"
		if(listeGroupes!=null && listeGroupes.size()>0){
			for(ElpDeCollection edc : listeGroupes){
				for(CollectionDeGroupes cdg : edc.getListeCollection()){
					for(Groupe groupe : cdg.getListeGroupes()){

						List<String> lcodindinscrits = elementPedagogiqueService.getCodIndInscritsFromGroupe(groupe.getCleGroupe(), annee);

						for (Inscrit i : listeInscrits) {
							if(lcodindinscrits.contains(i.getCod_ind())){
								//ajout codgroupe dans attribut de l'inscrit";codgpe;"
								if(!StringUtils.hasText(i.getCodes_groupes())){
									i.setCodes_groupes(Utils.SEPARATEUR_CODE_GROUPE+groupe.getCodGroupe()+Utils.SEPARATEUR_CODE_GROUPE);
								}else{
									i.setCodes_groupes(i.getCodes_groupes()+Utils.SEPARATEUR_CODE_GROUPE+groupe.getCodGroupe()+Utils.SEPARATEUR_CODE_GROUPE);
								}
							}
						}
					}
				}
			}
		}

		MainUI.getCurrent().setListeInscrits(listeInscrits);

	}


	/**
	 * renseigne les id etape.
	 * @param listeInscrits
	 */
	private void setIdEtpInscrits(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etp()!=null && i.getCod_vrs_vet()!=null)
				i.setId_etp(i.getCod_etp()+"/"+i.getCod_vrs_vet());
		}
	}

	/**
	 * renseigne les emails de chaque inscrit.
	 * @param listeInscrits
	 */
	private void setMailInscrits(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etu()!=null)
				i.setEmail(emailConverter.getMail(null,i.getCod_etu()));
		}
	}

	/**
	 * renseigne l'url pour la photo de chaque inscrit.
	 * @param listeInscrits
	 */
	private void setUrlPhotos(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			i.setUrlphoto(photo.getUrlPhoto(i.getCod_ind(), i.getCod_etu()));

		}
	}




	public List<ElpDeCollection> recupererGroupes(String annee, String codElp) {
		//appel WS Offre de foramtion 'recupererGroupe'
		List<ElpDeCollection> listeElp = new LinkedList<ElpDeCollection>();

		if(monProxyOffreDeFormation==null){
			monProxyOffreDeFormation = new OffreFormationMetierServiceInterfaceProxy();
		}


		System.out.println("recuperer groupe :"+annee + " "+codElp);
		try{
			RecupererGroupeDTO2 recupererGroupeDTO = monProxyOffreDeFormation.recupererGroupe_v2(annee, null, null, null, codElp, null);

			if (recupererGroupeDTO != null){
				System.out.println("recuperer groupe :"+recupererGroupeDTO);
				//On parcourt les ELP
				for(ElementPedagogiDTO2 elp : recupererGroupeDTO.getListElementPedagogi()){
					ElpDeCollection el = new ElpDeCollection(elp.getCodElp(), elp.getLibElp());

					List<CollectionDeGroupes> listeCollection = new LinkedList<CollectionDeGroupes>();

					//On parcourt les collections de l'ELP
					for( CollectionDTO3 cd2: elp.getListCollection()){
						CollectionDeGroupes collection = new CollectionDeGroupes(cd2.getCodExtCol());

						List<Groupe> listegroupe = new LinkedList<Groupe>();

						//On parcourt les groupes de la collection
						for(GroupeDTO2 gd2 : cd2.getListGroupe()){
							//On récupère les infos sur le groupe
							Groupe groupe = new Groupe(gd2.getCodExtGpe());
							groupe.setLibGroupe(gd2.getLibGpe());
							//on récupère le codeGpe
							groupe.setCleGroupe(""+gd2.getCodGpe());


							if(gd2.getCapaciteGpe() != null){
								if(gd2.getCapaciteGpe().getCapMaxGpe() != null){
									groupe.setCapMaxGpe(gd2.getCapaciteGpe().getCapMaxGpe());
								}else{
									groupe.setCapMaxGpe(0);
								}
								if(gd2.getCapaciteGpe().getCapIntGpe()!=null){
									groupe.setCapIntGpe(gd2.getCapaciteGpe().getCapIntGpe());
								}else{
									groupe.setCapIntGpe(0);
								}
							}else {
								groupe.setCapMaxGpe(0);
								groupe.setCapIntGpe(0);
							}
							//On ajoute le groupe à la liste de la collection
							listegroupe.add(groupe);
						}
						//on insere la liste créé dans la collection
						collection.setListeGroupes(listegroupe);
						//On ajoute la collection a la liste
						listeCollection.add(collection);
					}
					//On insere la liste créé dans l'ELP
					el.setListeCollection(listeCollection);
					//On ajoute l'ELP a la liste
					listeElp.add(el);
				}

			}

		}catch(Exception e){
			Log.error("Aucun Groupe pour "+codElp+ " - "+annee);
		}
		return listeElp;
	}


}
