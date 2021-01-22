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

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vaadin.ui.ComboBox;

import fr.univlorraine.apowsclient.offreFormation.CollectionDTO4;
import fr.univlorraine.apowsclient.offreFormation.ElementPedagogiDTO3;
import fr.univlorraine.apowsclient.offreFormation.GroupeDTO3;
import fr.univlorraine.apowsclient.offreFormation.OffreFormationMetierServiceInterface;
import fr.univlorraine.apowsclient.offreFormation.RecupererGroupeDTO3;
import fr.univlorraine.apowsclient.offreFormation.TableauCollection4;
import fr.univlorraine.apowsclient.offreFormation.TableauElementPedagogi3;
import fr.univlorraine.apowsclient.offreFormation.TableauGroupe3;
import fr.univlorraine.apowsclient.utils.ServiceProvider;
import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.converters.EmailConverterInterface;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit.Vet;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtapePK;
import fr.univlorraine.mondossierweb.services.apogee.ElementPedagogiqueService;
import fr.univlorraine.mondossierweb.services.apogee.MultipleApogeeService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;


/**
 * Gestion de l'affichage de la liste des inscrits
 */
@Component
public class ListeInscritsController {

	private Logger LOG = LoggerFactory.getLogger(ListeInscritsController.class);

	/**
	 * marges.
	 */
	private static final float MARGIN_TOP = 5.0f;
	private static final float MARGIN_RIGHT = 2.0f;
	private static final float MARGIN_BOTTOM = 4.0f;
	private static final float MARGIN_LEFT = 3.0f;
	/**
	 * le nombre d'inscrits par ligne dans la version pdf du trombinoscope.
	 */
	private static final int NB_INSCRITS_LIGNE_TROMBI_PDF = 7;
	/**
	 * 
	 */
	private static final int NB_LIGNE_INSEREE_TROMBI_PDF_A_LA_SUITE = 20;
	/**
	 * outputstream size.
	 */
	private static final int OUTPUTSTREAM_SIZE = 1024;
	/**
	 * marge.
	 */
	private static final float MARGE_PDF = 1.5f;
	/**
	 * l'écartement du pied de page (libellé de la promo et date d'édition) des pdf.
	 */
	private static final int ECARTEMENT_PIED_PAGE_PDF = 80;

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UiController uiController;
	@Resource
	private MultipleApogeeService multipleApogeeService;
	@Resource
	private ElementPedagogiqueService elementPedagogiqueService;
	@Resource(name="${emailConverter.implementation}")
	private transient EmailConverterInterface emailConverter;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	/**
	 * proxy pour faire appel aux infos sur l'étudiant WS .
	 */
	private final OffreFormationMetierServiceInterface offreDeFormationService = ServiceProvider.getOffreFormationService();



	/**
	 * Récupération des inscrits à une VET ou un ELP
	 * @param parameterMap
	 * @param annee
	 */
	public void recupererLaListeDesInscrits(Map<String, String> parameterMap, String annee, GenericUI ui){
		String code = parameterMap.get("code");
		String type = parameterMap.get("type");
		String anneeParam = parameterMap.get("annee");

		//Si on cherche des inscrits à une VET
		if (type.equals(Utils.VET)) {

			//init des attributs de la mainUI
			initMainUIAttributesValues(code, type,annee, ui);

			//init d'une liste d'inscrit vide
			List<Inscrit> listeInscrits = null;

			//On part d'une Etape pour établir une liste d'étudiant
			Etape e = new Etape();

			//On a une annee en parametre (on a choisi une année dans la liste déroulante)
			if(annee==null){
				//Renseignement de l'objet Etape
				e.setCode(code.split("/")[0]);
				e.setVersion(code.split("/")[1]);

				//Récupération derniere année universitaire dans Apogée
				int anneeMax = multipleApogeeService.getDerniereAnneeUniversitaire();

				//Récupération des années pour la vet
				List<String> annees = multipleApogeeService.getAnneesFromVetDesc(e, anneeMax);

				//Si on a récupéré aucune année, on ajoute l'année en cours par défaut.
				if(annees.size()==0){
					annees.add(etudiantController.getAnneeUnivEnCours(ui));
				}

				//On stocke laliste des année dans l'ui
				ui.setListeAnneeInscrits(annees);

				//Si on vient de la recherche arborescente et qu'on a une année présélectionnée
				if(anneeParam!=null){
					//On sélectionne cette année
					e.setAnnee(anneeParam);
				}else{
					//Si l'année universitaire est dans la liste des années retournées
					if(annees.contains(etudiantController.getAnneeUnivEnCours(ui))){
						//On prend l'année universitaire en cours
						e.setAnnee(etudiantController.getAnneeUnivEnCours(ui));
					}else{
						//Sinon on sélectionne la première année de la liste
						e.setAnnee(annees.get(0));
					}
				}
				//On garde l'année positionnée au niveau de l'ui
				ui.setAnneeInscrits(e.getAnnee());

				//On renseigne le libellé de l'étape
				e.setLibelle(multipleApogeeService.getLibelleEtape(e));

				//On garde l'étape au niveau de l'ui
				ui.setEtapeListeInscrits(e);

			}else{
				//Récupération de l'étape
				e = ui.getEtapeListeInscrits();

				//On modifie l'année de l'étape
				e.setAnnee(annee);

				//On met à jour l'étape au niveau de l'ui
				ui.setEtapeListeInscrits(e);

				//On met à jour l'année au niveau de l'ui
				ui.setAnneeInscrits(e.getAnnee());
			}

			//Récupération de la liste des inscrits
			listeInscrits = (List<Inscrit>) multipleApogeeService.getInscritsEtapeJuinSep(e);
			
			listeInscrits = aggregateVET(listeInscrits);

			//Maj des mails/photo et des groupes en fonction de la liste d'inscrits en paramètre
			finaliserListeInscrits(listeInscrits, null,annee, ui);

		} else {
			//On cherche les inscrits à un ELP.
			recupererLaListeDesInscritsELP(parameterMap, annee,ui);

		}


	}


	/**
	 * Récupération des inscrits à un ELP
	 * @param parameterMap
	 * @param annee
	 * @param etape
	 * @param groupe
	 */
	public void recupererLaListeDesInscritsELP(Map<String, String> parameterMap, String annee, GenericUI ui) {

		String code = parameterMap.get("code");
		String type = parameterMap.get("type");
		String anneeParam = parameterMap.get("annee");

		//init des attributs de la mainUI
		initMainUIAttributesValues(code, type,annee, ui);

		//init d'une liste d'inscrit vide
		List<Inscrit> listeInscrits = null;

		//On part d'un ELP pour établir une liste d'étudiant
		ElementPedagogique e = new ElementPedagogique();

		//On a une annee en parametre (on a choisi une année dans la liste déroulante)
		if(annee==null){
			//On renseigne le cod_elp
			e.setCode(code);

			//Récupération derniere année universitaire dans Apogée
			List<String> annees = multipleApogeeService.getDernieresAnneesUniversitaires();

			//On stocke laliste des année dans l'ui
			ui.setListeAnneeInscrits(annees);

			//Si on vient de la recherche arborescente et qu'on a une année présélectionnée
			if(anneeParam!=null){
				//On sélectionne cette année
				e.setAnnee(anneeParam);
			}else{
				//Si l'année universitaire est dans la liste des années retournées
				if(annees.contains(etudiantController.getAnneeUnivEnCours(ui))){
					//On prend l'année universitaire en cours
					e.setAnnee(etudiantController.getAnneeUnivEnCours(ui));
				}else{
					//Sinon on sélectionne la première année de la liste
					e.setAnnee(annees.get(0));
				}
			}

			//On garde l'année positionnée au niveau de l'ui
			ui.setAnneeInscrits(e.getAnnee());

			//On renseigne le libellé de l'ELP
			e.setLibelle(elementPedagogiqueService.getLibelleElp(code));

			//On stocke l'ELP au niveau de l'ui
			ui.setElpListeInscrits(e);

		}else{
			//Récupération de l'ELP
			e = ui.getElpListeInscrits();

			//Modification de l'année de l'ELP
			e.setAnnee(annee);

			//On met à jour l'ELP au niveau de l'ui
			ui.setElpListeInscrits(e);

			//On garde l'année positionnée au niveau de l'ui
			ui.setAnneeInscrits(e.getAnnee());
		}

		//Récupération de tous les inscrit à l'ELP quelque soit l'étape d'appartenance choisie dans la vue ListeInscritView
		listeInscrits = (List<Inscrit>) elementPedagogiqueService.getInscritsFromElp(code, e.getAnnee());

		listeInscrits = aggregateVET(listeInscrits);

		//On créé une liste de VET vide pour crééer la liste des étapes d'appartenance
		List<VersionEtape> letape = null;

		//test si on a des inscrits
		if(listeInscrits!=null && listeInscrits.size()>0){
			//on init la liste de VET à vide
			letape = new LinkedList<VersionEtape>();

			//Pour chaque inscrit
			for(Inscrit i : listeInscrits){
				if(i.getListe_vet() != null) {
					for(Vet v : i.getListe_vet()) {
						//Récupération de la VET
						VersionEtape vet = getVersionEtape(v.getCod_etp(), v.getCod_vrs_vet(), v.getLib_etp());
						
						LOG.info("--" + i.getCod_etu() + " => " + vet.getId().getCod_etp() + " / "+ vet.getId().getCod_vrs_vet()+ " : " +vet.getLib_web_vet());
						//Si la liste de VET de contient pas la VET de l'étudiant
						if(!letape.contains(vet)){
							//On ajoute la vet à la liste des étapes d'appartenance
							letape.add(vet);
						}
					}
				}/*else {
					//Test si l'étape est renseignée pour l'inscrit
					if(StringUtils.hasText(i.getCod_etp()) && StringUtils.hasText(i.getCod_vrs_vet()) && StringUtils.hasText(i.getLib_etp())){
						//Récupération de la VET
						VersionEtape vet = getVersionEtape(i.getCod_etp(), i.getCod_vrs_vet(), i.getLib_etp());
						
						LOG.info("-" + i.getCod_etu() + " => " + vet.getId().getCod_etp() + " / "+ vet.getId().getCod_vrs_vet()+ " : " +vet.getLib_web_vet());
						//Si la liste de VET de contient pas la VET de l'étudiant
						if(!letape.contains(vet)){
							//On ajoute la vet à la liste des étapes d'appartenance
							letape.add(vet);
						}
					}
				}*/

			}
		}
		//On stocket la liste des étapes d'appartenance au niveau de l'ui
		ui.setListeEtapesInscrits(letape);

		//Aucune étape n'est présélectionnée par défaut.
		ui.setEtapeInscrits(null);


		//Récupération des groupes de l'ELP
		List<ElpDeCollection> listeGroupes = recupererGroupes(e.getAnnee(), code);
		//Si on a récupérer des groupes
		if(listeGroupes!=null && listeGroupes.size()>0){
			//On stocke la liste des groupes au niveau de l'ui
			ui.setListeGroupesInscrits(listeGroupes);
		}

		//Maj des mails/photo et des groupes en fonction de la liste d'inscrits en paramètre
		finaliserListeInscrits(listeInscrits,listeGroupes,e.getAnnee(),ui);

	}

	private VersionEtape getVersionEtape(String code, String version, String libelle) {
		VersionEtape vet = new VersionEtape();
		VersionEtapePK vetpk = new VersionEtapePK();
		vetpk.setCod_etp(code);
		vetpk.setCod_vrs_vet(version);
		vet.setId(vetpk);
		vet.setLib_web_vet(libelle);
		return vet;
	}


	/**
	 * 
	 * @param listeInscrits
	 * @return liste sans doublon avec aggregation des vets
	 */
	private List<Inscrit> aggregateVET(List<Inscrit> listeInscrits) {
		List<Inscrit> liste=null;
		// Si la list d'origine contient des inscrits
		if(listeInscrits!=null) {
			liste = new LinkedList<Inscrit> ();
			// On parcourt la liste d'origine
			for(Inscrit inscrit : listeInscrits) {
				boolean insere = false;
				// On parcourt la liste en cours de création
				for(Inscrit i : liste) {
					// Si l'étudiant est déjà dans la liste
					if(i!=null && i.getCod_etu()!=null && i.getCod_etu().equals(inscrit.getCod_etu())){
						//Ajout de la vet de l'étudiant en cours de traitement
						i.ajoutVet(inscrit.getCod_etp(), inscrit.getCod_vrs_vet(), inscrit.getLib_etp());
						insere = true;
					}
				}
				// l'étudiant n'était pas dans la liste
				if(!insere) {
					// création de la liste 
					inscrit.setListe_vet(new LinkedList<Vet> ());
					//ajout de la vet déjà assignée à l'étudiant
					inscrit.ajoutVet(inscrit.getCod_etp(), inscrit.getCod_vrs_vet(), inscrit.getLib_etp());
					//Ajout de l'étudiant dans la liste
					liste.add(inscrit);
				}
			}
		}
		return liste;
	}


	/**
	 * initialise les attributs de MainUI utilisés dans a vue listeInscritsView
	 * @param code
	 * @param type
	 * @param annee
	 */
	private void initMainUIAttributesValues(String code, String type, String annee, GenericUI ui) {
		if(ui.getListeInscrits()!=null){
			ui.getListeInscrits().clear();
		}

		if(annee==null){
			ui.setAnneeInscrits(null);
			ui.setListeAnneeInscrits(null);
			ui.setEtapeListeInscrits(null);
		}

		ui.setEtapeInscrits(null);
		ui.setGroupeInscrits(null);
		ui.setListeEtapesInscrits(null);
		ui.setListeGroupesInscrits(null);

		ui.setCodeObjListInscrits(code);
		ui.setTypeObjListInscrits(type);

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
	private void finaliserListeInscrits(List<Inscrit> listeInscrits,List<ElpDeCollection> listeGroupes, String annee, GenericUI ui) {

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

						List<BigDecimal> lcodindinscrits = elementPedagogiqueService.getCodIndInscritsFromGroupe(groupe.getCleGroupe(), annee);

						int nbins=0;
						for (Inscrit i : listeInscrits) {
							if(lcodindinscrits!=null && listeContient(lcodindinscrits,i.getCod_ind())){	
								//ajout codgroupe dans attribut de l'inscrit";codgpe;"
								if(!StringUtils.hasText(i.getCodes_groupes())){
									i.setCodes_groupes(Utils.SEPARATEUR_CODE_GROUPE+groupe.getCleGroupe()+Utils.SEPARATEUR_CODE_GROUPE);
								}else{
									i.setCodes_groupes(i.getCodes_groupes()+Utils.SEPARATEUR_CODE_GROUPE+groupe.getCleGroupe()+Utils.SEPARATEUR_CODE_GROUPE);
								}

								nbins++;
							}
						}
						groupe.setNbInscrits(nbins);
					}
				}
			}
		}

		ui.setListeInscrits(listeInscrits);

	}


	private boolean listeContient(List<BigDecimal> lcodindinscrits, String cod_ind) {
		if(lcodindinscrits.size()>0 && cod_ind!=null){
			for(BigDecimal s: lcodindinscrits){
				if(s.toString().equals(cod_ind)){
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * renseigne les id etape.
	 * @param listeInscrits
	 */
	private void setIdEtpInscrits(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			i.setId_etp("");
			// parcourir les vets
			for(Vet vet : i.getListe_vet()) {
				if(vet.getCod_etp()!=null && vet.getCod_vrs_vet()!=null) {
					i.setId_etp(i.getId_etp() + Utils.SEPARATEUR_VETS + vet.getCod_etp()+"/"+vet.getCod_vrs_vet());
				}
			}
		}
	}

	/**
	 * renseigne les emails de chaque inscrit.
	 * @param listeInscrits
	 */
	private void setMailInscrits(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			if(i.getCod_etu()!=null)
				i.setEmail(emailConverter.getMail(i.getCod_etu()));
		}
	}

	/**
	 * renseigne l'url pour la photo de chaque inscrit.
	 * @param listeInscrits
	 */
	public void setUrlPhotos(List<Inscrit> listeInscrits) {
		for (Inscrit i : listeInscrits) {
			//i.setUrlphoto(photo.getUrlPhoto(i.getCod_ind(), i.getCod_etu()));
			i.setUrlphoto(GenericUI.getCurrent().getPhotoProvider().getUrlPhoto(i.getCod_ind(), i.getCod_etu(), userController.isEnseignant(),userController.getCurrentUserName()));
		}
	}






	public List<ElpDeCollection> recupererGroupes(String annee, String codElp) {
		//appel WS Offre de foramtion 'recupererGroupe'
		List<ElpDeCollection> listeElp = new LinkedList<ElpDeCollection>();

		/*if(offreDeFormationService==null){
			offreDeFormationService = (OffreFormationMetierServiceInterface) WSUtils.getService(WSUtils.OFFREFORMATION_SERVICE_NAME, PropertyUtils.getApoWsUsername(),PropertyUtils.getApoWsPassword());
		}*/

		try{
			RecupererGroupeDTO3 recupererGroupeDTO = offreDeFormationService.recupererGroupeV3(annee, null, null, null, codElp, null);

			if (recupererGroupeDTO != null){
				TableauElementPedagogi3 tep = recupererGroupeDTO.getListElementPedagogi();
				if(tep!=null && tep.getElementPedagogi()!=null && !tep.getElementPedagogi().isEmpty()) {
					//On parcourt les ELP
					for(ElementPedagogiDTO3 elp : tep.getElementPedagogi()){
						ElpDeCollection el = new ElpDeCollection(elp.getCodElp(), elp.getLibElp());

						List<CollectionDeGroupes> listeCollection = new LinkedList<CollectionDeGroupes>();

						//On parcourt les collections de l'ELP
						TableauCollection4 tcol = elp.getListCollection();
						if(tcol!=null && tcol.getCollection()!=null && !tcol.getCollection().isEmpty()) {
							for( CollectionDTO4 cd2: tcol.getCollection()){
								CollectionDeGroupes collection = new CollectionDeGroupes(cd2.getCodExtCol());

								List<Groupe> listegroupe = new LinkedList<Groupe>();

								//On parcourt les groupes de la collection
								TableauGroupe3 tgr = cd2.getListGroupe();
								if(tgr !=null && tgr.getGroupe()!=null && !tgr.getGroupe().isEmpty()) {
									for(GroupeDTO3 gd2 : tgr.getGroupe()){
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
								}
								//on insere la liste créé dans la collection
								collection.setListeGroupes(listegroupe);
								//On ajoute la collection a la liste
								listeCollection.add(collection);
							}
						}
						//On insere la liste créé dans l'ELP
						el.setListeCollection(listeCollection);
						//On ajoute l'ELP a la liste
						listeElp.add(el);
					}
				}

			}

		}catch(Exception e){
			LOG.debug("Aucun Groupe pour "+codElp+ " - "+annee);
		}
		return listeElp;
	}


	/**
	 * Retourne la liste d'inscrits en xls
	 * @param linscrits
	 * @param listecodind
	 * @return
	 */
	public ByteArrayInputStream getXlsStream(List<Inscrit> linscrits, List<String> listecodind,ComboBox listeGroupes, String libObj, String annee, String typeFavori) {

		LOG.debug("generation xls : "+libObj+ " "+annee+" "+linscrits.size()+ " "+listecodind.size());
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
			HSSFWorkbook wb = creerExcel(linscrits, listecodind, listeGroupes,(typeFavori!=null && typeFavori.equals(Utils.VET)));
			wb.write(baos);
			byte[] bytes = baos.toByteArray();
			return new ByteArrayInputStream(bytes);
		} catch (IOException e) {
			LOG.error("Erreur à la génération de la liste en xls : IOException ",e);
			return null;
		}


	}


	/**
	 * créer le fichier excel à partir de la liste des inscrits.
	 * @return le fichier excel de la liste des inscrits.
	 */
	@SuppressWarnings("deprecation")
	public HSSFWorkbook creerExcel(List<Inscrit> listeInscrits, List<String> listeCodInd,ComboBox listeGroupes, boolean isTraiteEtape) {
		//	creation du fichier excel
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("page1");


		boolean isSession1=true;
		boolean isSession2=true;

		//formatage de la taille des colonne
		sheet.setColumnWidth((short) 0, (short) (4000));
		sheet.setColumnWidth((short) 1, (short) (6000));
		sheet.setColumnWidth((short) 2, (short) (5120));
		sheet.setColumnWidth((short) 3, (short) (4000));
		sheet.setColumnWidth((short) 4, (short) (8000));
		if (isTraiteEtape) {
			sheet.setColumnWidth((short) 5 , (short) (1200));
			sheet.setColumnWidth((short) 6, (short) (2000));
			sheet.setColumnWidth((short) 7, (short) (3000));
			sheet.setColumnWidth((short) 8, (short) (2000));
			sheet.setColumnWidth((short) 9, (short) (3000));

		} else {
			sheet.setColumnWidth((short) 5, (short) (3000));
			sheet.setColumnWidth((short) 6, (short) (3000));
			sheet.setColumnWidth((short) 7, (short) (18000));
			sheet.setColumnWidth((short) 8, (short) (2000));
			sheet.setColumnWidth((short) 9, (short) (3000));
			sheet.setColumnWidth((short) 10, (short) (2000));
			sheet.setColumnWidth((short) 11, (short) (3000));
			sheet.setColumnWidth((short) 12, (short) (8000));
		}

		// Creation des lignes
		HSSFRow row = sheet.createRow((short) 0);

		//CREATION DES STYLES:
		//STYLE1:
		HSSFCellStyle headerStyle = wb.createCellStyle();
		headerStyle.setFillBackgroundColor(HSSFColor.BLUE.index);
		headerStyle.setFillForegroundColor(HSSFColor.BLUE.index);
		headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		HSSFFont font = wb.createFont();
		font.setColor(HSSFColor.WHITE.index);
		font.setBoldweight((short) 10);
		headerStyle.setFont(font);
		//bordure style1
		headerStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setBottomBorderColor(HSSFColor.BLACK.index);
		headerStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setLeftBorderColor(HSSFColor.BLACK.index);
		headerStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setRightBorderColor(HSSFColor.BLACK.index);
		headerStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
		headerStyle.setTopBorderColor(HSSFColor.BLACK.index);



		int rang_cellule = 0;
		HSSFCell cellLib1 = row.createCell((short) rang_cellule);
		cellLib1.setCellStyle(headerStyle);
		cellLib1.setCellValue(applicationContext.getMessage("xls.folder", null, Locale.getDefault()).toUpperCase() );
		rang_cellule++;

		HSSFCell cellLib2 = row.createCell((short) rang_cellule);
		cellLib2.setCellStyle(headerStyle);
		cellLib2.setCellValue(applicationContext.getMessage("xls.nom", null, Locale.getDefault()).toUpperCase() );
		rang_cellule++;

		HSSFCell cellLib3 = row.createCell((short) rang_cellule);
		cellLib3.setCellStyle(headerStyle);
		cellLib3.setCellValue(applicationContext.getMessage("xls.prenom", null, Locale.getDefault()).toUpperCase() );
		rang_cellule++;

		HSSFCell cellLib4 = row.createCell((short) rang_cellule);
		cellLib4.setCellStyle(headerStyle);
		cellLib4.setCellValue(applicationContext.getMessage("xls.naissance", null, Locale.getDefault()).toUpperCase() );
		rang_cellule++;

		HSSFCell cellLib5 = row.createCell((short) rang_cellule);
		cellLib5.setCellStyle(headerStyle);
		cellLib5.setCellValue(applicationContext.getMessage("xls.messagerie", null, Locale.getDefault()).toUpperCase() );
		rang_cellule++;

		if (isTraiteEtape) {
			HSSFCell cellLib6 = row.createCell((short) rang_cellule);
			cellLib6.setCellStyle(headerStyle);
			cellLib6.setCellValue(applicationContext.getMessage("xls.iae", null, Locale.getDefault()).toUpperCase()+"?" );
			rang_cellule++;
		}
		if (!isTraiteEtape) {
			HSSFCell cellLib7 = row.createCell((short) rang_cellule);
			cellLib7.setCellStyle(headerStyle);
			cellLib7.setCellValue(applicationContext.getMessage("xls.code", null, Locale.getDefault()).toUpperCase() );
			rang_cellule++;

			HSSFCell cellLib8 = row.createCell((short) rang_cellule);
			cellLib8.setCellStyle(headerStyle);
			cellLib8.setCellValue(applicationContext.getMessage("xls.version", null, Locale.getDefault()).toUpperCase() );
			rang_cellule++;

			HSSFCell cellLib9 = row.createCell((short) rang_cellule);
			cellLib9.setCellStyle(headerStyle);
			cellLib9.setCellValue(applicationContext.getMessage("xls.etape", null, Locale.getDefault()).toUpperCase() );
			rang_cellule++;
		}
		if (isSession1) {
			HSSFCell cellLib10 = row.createCell((short) rang_cellule);
			cellLib10.setCellStyle(headerStyle);
			cellLib10.setCellValue(applicationContext.getMessage("xls.note1", null, Locale.getDefault()).toUpperCase());
			rang_cellule++;

			HSSFCell cellLib11 = row.createCell((short) rang_cellule);
			cellLib11.setCellStyle(headerStyle);
			cellLib11.setCellValue(applicationContext.getMessage("xls.result1", null, Locale.getDefault()).toUpperCase());
			rang_cellule++;

		}

		if (isSession2) {
			HSSFCell cellLib12 = row.createCell((short) rang_cellule);
			cellLib12.setCellStyle(headerStyle);
			cellLib12.setCellValue(applicationContext.getMessage("xls.note2", null, Locale.getDefault()).toUpperCase() );
			rang_cellule++;

			HSSFCell cellLib13 = row.createCell((short) rang_cellule);
			cellLib13.setCellStyle(headerStyle);
			cellLib13.setCellValue(applicationContext.getMessage("xls.result2", null, Locale.getDefault()).toUpperCase());
			rang_cellule++;

		}

		//info sur les groupes
		if (!isTraiteEtape) {
			HSSFCell cellLib14 = row.createCell((short) rang_cellule);
			cellLib14.setCellStyle(headerStyle);
			cellLib14.setCellValue(applicationContext.getMessage("xls.groupes", null, Locale.getDefault()).toUpperCase() );
			rang_cellule++;
		}

		int nbrow = 1;
		for (Inscrit inscrit : listeInscrits) {
			if(listeCodInd.contains(inscrit.getCod_ind())){
				HSSFRow rowInscrit  = sheet.createRow((short) nbrow);
				
				CellStyle alignTopStyle = wb.createCellStyle();
				alignTopStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);

				int rang_cellule_inscrit = 0;
				HSSFCell cellLibInscrit1 = rowInscrit.createCell((short) rang_cellule_inscrit);
				cellLibInscrit1.setCellValue(inscrit.getCod_etu());
				cellLibInscrit1.setCellStyle(alignTopStyle);
				rang_cellule_inscrit++;

				HSSFCell cellLibInscrit2 = rowInscrit.createCell((short) rang_cellule_inscrit);
				cellLibInscrit2.setCellValue(inscrit.getNom());
				cellLibInscrit2.setCellStyle(alignTopStyle);
				rang_cellule_inscrit++;

				HSSFCell cellLibInscrit3 = rowInscrit.createCell((short) rang_cellule_inscrit);
				cellLibInscrit3.setCellValue(inscrit.getPrenom());
				cellLibInscrit3.setCellStyle(alignTopStyle);
				rang_cellule_inscrit++;

				HSSFCell cellLibInscrit31 = rowInscrit.createCell((short) rang_cellule_inscrit);
				cellLibInscrit31.setCellValue(inscrit.getDate_nai_ind());
				cellLibInscrit31.setCellStyle(alignTopStyle);
				rang_cellule_inscrit++;

				HSSFCell cellLibInscrit4 = rowInscrit.createCell((short) rang_cellule_inscrit);
				cellLibInscrit4.setCellValue(inscrit.getEmail());
				cellLibInscrit4.setCellStyle(alignTopStyle);
				rang_cellule_inscrit++;

				if (isTraiteEtape) {
					HSSFCell cellLibInscrit5 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit5.setCellValue(inscrit.getIae());
					cellLibInscrit5.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;
				}
				if (!isTraiteEtape) {
					String codes = "";
					String versions = "";
					String libelles = "";
					for(Vet v : inscrit.getListe_vet()) {
						codes += (codes.equals("") ? "" :"\n") + v.getCod_etp();
						versions += (versions.equals("") ? "" :"\n") + v.getCod_vrs_vet();
						libelles += (libelles.equals("") ? "" : "\n") + v.getLib_etp();
					}
					CellStyle breakLineStyle = wb.createCellStyle();
					breakLineStyle.setWrapText(true);
					breakLineStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					
					HSSFCell cellLibInscrit6 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit6.setCellValue(codes);
					cellLibInscrit6.setCellStyle(breakLineStyle);
					rang_cellule_inscrit++;

					HSSFCell cellLibInscrit7 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit7.setCellValue(versions);
					cellLibInscrit7.setCellStyle(breakLineStyle);
					rang_cellule_inscrit++;

					HSSFCell cellLibInscrit8 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit8.setCellValue(libelles);
					cellLibInscrit8.setCellStyle(breakLineStyle);
					rang_cellule_inscrit++;
				}
				if (isSession1) {
					HSSFCell cellLibInscrit9 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit9.setCellValue(inscrit.getNotej());
					cellLibInscrit9.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;

					HSSFCell cellLibInscrit10 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit10.setCellValue(inscrit.getResj());
					cellLibInscrit10.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;
				}

				if (isSession2) {
					HSSFCell cellLibInscrit11 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit11.setCellValue(inscrit.getNotes());
					cellLibInscrit11.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;

					HSSFCell cellLibInscrit12 = rowInscrit.createCell((short) rang_cellule_inscrit);
					cellLibInscrit12.setCellValue(inscrit.getRess());
					cellLibInscrit12.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;
				}

				//ajout info sur les groupes si il y a lieu
				if (!isTraiteEtape) {
					HSSFCell cellLibGroupes = rowInscrit.createCell((short) rang_cellule_inscrit);
					String grpXls="";
					List<String> lcodegroup = Utils.splitStringFromSemiColon(inscrit.getCodes_groupes());
					for(String codegroupe : lcodegroup){
						if(StringUtils.hasText(grpXls)){
							grpXls += ", ";
						}
						grpXls += listeGroupes.getItemCaption(codegroupe);
					}
					cellLibGroupes.setCellValue(grpXls);
					cellLibGroupes.setCellStyle(alignTopStyle);
					rang_cellule_inscrit++;
				}
				nbrow++;
			}
		}

		return wb;
	}


	/**
	 * Retourne le trombinoscope en pdf
	 * @param linscrits
	 * @param listecodind
	 * @return
	 */
	public InputStream getPdfStream(List<Inscrit> linscrits, List<String> listecodind, String libObj, String annee) {

		LOG.debug("generation pdf : "+libObj+ " "+annee+" "+linscrits.size()+ " "+listecodind.size());
		try {
			ByteArrayOutputStream baosPDF = new ByteArrayOutputStream(OUTPUTSTREAM_SIZE);
			PdfWriter docWriter = null;
			Document document = configureDocument(MARGE_PDF);
			docWriter = PdfWriter.getInstance(document, baosPDF);
			// Test si on doit activer l'encryption
			if(PropertyUtils.isEnablePdfSecurity()){
				docWriter.setEncryption(null, null, PdfWriter.AllowPrinting, PdfWriter.ENCRYPTION_AES_128);
			}
			docWriter.setStrictImageSequence(true);
			creerPdfTrombinoscope(document, linscrits, listecodind, libObj, annee);
			docWriter.close();
			baosPDF.close();
			//Creation de l'export
			byte[] bytes = baosPDF.toByteArray();
			return new ByteArrayInputStream(bytes);
		} catch (DocumentException e) {
			LOG.error("Erreur à la génération du trombinoscope : DocumentException ",e);
			return null;
		} catch (IOException e) {
			LOG.error("Erreur à la génération du trombinoscope : IOException ",e);
			return null;
		}


	}




	/**
	 * configure le document pdf.
	 * @param width
	 * @param height
	 * @param margin
	 * @return doc
	 */
	private Document configureDocument(final float margin) {

		Document document = new Document();

		document.setPageSize(PageSize.A4.rotate());
		float marginPage = (margin / 2.54f) * 72f;
		document.setMargins(marginPage, marginPage, marginPage, marginPage);

		return document;
	}


	/**
	 * 
	 * @param document pdf
	 */
	public void creerPdfTrombinoscope(final Document document, List<Inscrit> listeInscrits, List<String> listecodind, String libelle, String annee) {


		//configuration des fonts
		Font normal = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.NORMAL);
		Font normalbig = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10, Font.BOLD);
		Font legerita = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.ITALIC);
		Font leger = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.NORMAL);
		Font headerbig = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, Font.BOLD);
		Font header = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.BOLD);

		//pieds de pages:
		String part="";
		Date d = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String date = dateFormat.format(d);
		//alignement des libellés du pied de page:
		String partie1 = libelle+" "+annee;
		String partie2 =  applicationContext.getMessage("pdf.edition.date", null, Locale.getDefault())+" : " + date;
		if (partie1.length() < ECARTEMENT_PIED_PAGE_PDF) {
			int diff = ECARTEMENT_PIED_PAGE_PDF - partie1.length();
			for (int i = 0; i < diff; i++) {
				partie1 = partie1 + " ";

			}
		} 
		if (partie2.length() < ECARTEMENT_PIED_PAGE_PDF) {
			int diff = ECARTEMENT_PIED_PAGE_PDF - partie2.length();
			for (int i = 0; i < diff; i++) {
				partie2 = " " + partie2;
			}
		}

		//création du pied de page:
		Phrase phra = new Phrase(partie1 + "-" + part +" Page", legerita);
		Phrase phra2 = new Phrase("- "+partie2, legerita);
		HeaderFooter hf = new HeaderFooter(phra, phra2);
		hf.setAlignment(HeaderFooter.ALIGN_CENTER);
		document.setFooter(hf);	 


		//ouverte du document.
		document.open();

		try {

			//ajout image test
			if (configController.getLogoUniversitePdf() != null && !configController.getLogoUniversitePdf().equals("")){
				Image image1 = Image.getInstance(configController.getLogoUniversitePdf());
				float scaleRatio = 40 / image1.getHeight();
				float newWidth=scaleRatio * image1.getWidth();
				image1.scaleAbsolute(newWidth, 40);
				image1.setAbsolutePosition(800 - newWidth, 528);
				document.add(image1);
			}



			Paragraph p = new Paragraph( applicationContext.getMessage("pdf.trombinoscope.title", null, Locale.getDefault()).toUpperCase(), headerbig);
			p.setIndentationLeft(15);
			document.add(p);

			Paragraph p3 = new Paragraph( applicationContext.getMessage("pdf.promotion", null, Locale.getDefault())+" : " + libelle, normal);
			p3.setIndentationLeft(15);
			document.add(p3);

			Paragraph p2 = new Paragraph( applicationContext.getMessage("pdf.year", null, Locale.getDefault())+" : " + annee, normal);
			p2.setIndentationLeft(15);
			document.add(p2);

			Paragraph p4 = new Paragraph( applicationContext.getMessage("pdf.nbinscrits", null, Locale.getDefault())+" : " + listecodind.size(), normal);
			p4.setIndentationLeft(15);
			document.add(p4);


			Paragraph p03 = new Paragraph( applicationContext.getMessage("pdf.edition.date", null, Locale.getDefault())+" : " + date + "\n\n", normal);
			p03.setIndentationLeft(15);
			document.add(p03);


			PdfPTable table = new PdfPTable(NB_INSCRITS_LIGNE_TROMBI_PDF);
			table.setWidthPercentage(100f);

			int compteur = 0;
			Rectangle border = new Rectangle(0f, 0f);
			border.setBorderColorLeft(Color.WHITE);
			border.setBorderColorBottom(Color.WHITE);
			border.setBorderColorRight(Color.WHITE);
			border.setBorderColorTop(Color.WHITE);

			String tabNom[] = new String[NB_INSCRITS_LIGNE_TROMBI_PDF];
			String tabNum[] = new String[NB_INSCRITS_LIGNE_TROMBI_PDF];
			//insertion de listeInscrits dans listeInscritstrombi si le trombinoscope n'est pas decoupé
			/*	if (listeInscritsTrombi == null || listeInscritsTrombi.size() == 0) {
				ArrayList<Inscrit> listeInscritsbis = (ArrayList<Inscrit>) listeInscrits.clone();
				listeInscritsTrombi.add(listeInscritsbis);
			}*/
			//nombre d'etudiants insérer a la suite dans le pdf:
			int nbEtudiantInsere = 0;
			for (Inscrit inscrit : listeInscrits) {
				if(listecodind.contains(inscrit.getCod_ind())){
					nbEtudiantInsere++;
					//on en a inséré le plus possible d'un coup (pour eviter un timeout du server 
					//de photos sur les premieres photos 
					//au moment de l'insertion dans le pdf : document.add() ):
					//on insere la table dans le pdf et on recommence une nouvelle table
					if (nbEtudiantInsere > (NB_INSCRITS_LIGNE_TROMBI_PDF * NB_LIGNE_INSEREE_TROMBI_PDF_A_LA_SUITE)) {
						document.add(table);
						document.newPage();
						table = new PdfPTable(NB_INSCRITS_LIGNE_TROMBI_PDF);
						table.setWidthPercentage(100f);
						tabNom = new String[NB_INSCRITS_LIGNE_TROMBI_PDF];
						tabNum = new String[NB_INSCRITS_LIGNE_TROMBI_PDF];
						nbEtudiantInsere = 1;
						compteur = 0;
					}

					tabNom[compteur] = "" + inscrit.getPrenom() + " \n" + inscrit.getNom() + "\n";
					tabNum[compteur] = "" + inscrit.getCod_etu();

					compteur++;

					//String foto = photo.getUrlPhotoTrombinoscopePdf(inscrit.getCod_ind(), inscrit.getCod_etu());
					String foto = GenericUI.getCurrent().getPhotoProvider().getUrlPhotoTrombinoscopePdf(inscrit.getCod_ind(), inscrit.getCod_etu(), userController.isEnseignant(),userController.getCurrentUserName());
					Image photo = Image.getInstance(foto);
					photo.scaleAbsolute(85, 107);


					PdfPCell cell = new PdfPCell(photo);
					cell.cloneNonPositionParameters(border);
					table.addCell(cell);

					if (compteur == NB_INSCRITS_LIGNE_TROMBI_PDF) {
						for (int i = 0; i < NB_INSCRITS_LIGNE_TROMBI_PDF; i++) {
							Phrase ph = new Phrase(tabNom[i], normalbig);
							Phrase ph2 = new Phrase(tabNum[i], leger);
							Paragraph pinscrit = new Paragraph();
							pinscrit.add(ph);
							pinscrit.add(ph2);
							PdfPCell celltext = new PdfPCell(pinscrit);
							celltext.cloneNonPositionParameters(border);
							table.addCell(celltext);
						}
						compteur = 0;
					}


				}


			}
			if (compteur > 0) {
				for (int i = compteur; i < NB_INSCRITS_LIGNE_TROMBI_PDF; i++) {
					PdfPCell cell = new PdfPCell();
					cell.cloneNonPositionParameters(border);
					table.addCell(cell);
				}

				for (int i = 0; i < compteur; i++) {
					Phrase ph = new Phrase(tabNom[i], normalbig);
					Phrase ph2 = new Phrase(tabNum[i], leger);
					Paragraph pinscrit = new Paragraph();
					pinscrit.add(ph);
					pinscrit.add(ph2);
					PdfPCell celltext = new PdfPCell(pinscrit);
					celltext.cloneNonPositionParameters(border);
					table.addCell(celltext);
				}

				for (int i = compteur; i < NB_INSCRITS_LIGNE_TROMBI_PDF; i++) {
					PdfPCell cell = new PdfPCell();
					cell.cloneNonPositionParameters(border);
					table.addCell(cell);
				}

			}

			document.add(table);

		} catch (BadElementException e) {
			LOG.error("Erreur à la génération du certificat : BadElementException ",e);
		} catch (MalformedURLException e) {
			LOG.error("Erreur à la génération du certificat : MalformedURLException ",e);
		} catch (IOException e) {
			LOG.error("Erreur à la génération du certificat : IOException ",e);
		} catch (DocumentException e) {
			LOG.error("Erreur à la génération du certificat : DocumentException ",e);
		}

		// step 6: fermeture du document.
		document.close();




	}


	/**
	 * 
	 * @return true si on a un serveur de photo opérationnel
	 */
	public boolean isPhotoProviderOperationnel() {
		return GenericUI.getCurrent().getPhotoProvider().isOperationnel();
	}
}
