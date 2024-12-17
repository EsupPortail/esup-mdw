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
package fr.univlorraine.mondossierweb.views.windows;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.ResultatController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * Fenêtre du détail de l'inscription
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetailInscriptionWindow extends Window {

	public static final String NAME = "inscriptionWindow";



	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource(name="${resultat.implementation}")
	private transient ResultatController resultatController;
	@Resource
	private transient ConfigController configController;

	private Etape etape;

	/**
	 * Crée une fenêtre
	 */
	public void init(Etape et) {
		etape = et;
		init();
	}

	private void init() {

		//On vérifie le droit d'accéder à la vue
		if((userController.isEnseignant() || userController.isEtudiant()) && MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null){

			/* Style */
			setWidth(80, Unit.PERCENTAGE);
			setHeight(95, Unit.PERCENTAGE);
			setModal(true);
			setResizable(false);


			//Test si user enseignant
			if(userController.isEnseignant()){
				//On recupere le détail de l'IP pour un enseignant
				resultatController.renseigneDetailInscription(etape, userController.isGestionnaire());
			}else{
				//On récupère le détail de l'IP pour un étudiant
				resultatController.renseigneDetailInscriptionEtudiant(etape);
			}

			/* Layout */
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setMargin(true);
			layout.setSpacing(true);

			/* Titre */
			setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


			//Sous titre avec l'année
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setSizeFull();
			titleLayout.setHeight("20px");
			Label labelAnneeUniv = new Label(applicationContext.getMessage(NAME+".label.anneeuniv", null, getLocale())+" <b>"+ etape.getAnnee()+"</b>");
			labelAnneeUniv.setContentMode(ContentMode.HTML);
			titleLayout.addComponent(labelAnneeUniv);
			titleLayout.setComponentAlignment(labelAnneeUniv, Alignment.MIDDLE_CENTER);
			layout.addComponent(titleLayout);



			Panel panelDetailInscription= new Panel(etape.getLibelle());
			panelDetailInscription.setSizeFull();
			panelDetailInscription.addStyleName("small-font-element");

			List<ElementPedagogique> lelp = MainUI.getCurrent().getEtudiant().getElementsPedagogiques();
			if(lelp!=null && lelp.size()>0){
				Table detailInscriptionTable = new Table(null, new BeanItemContainer<>(ElementPedagogique.class, lelp));
				detailInscriptionTable.setSizeFull();
				detailInscriptionTable.setVisibleColumns(new String[0]);
				detailInscriptionTable.setColumnCollapsingAllowed(true);
				detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.code", null, getLocale()), new CodeElpColumnGenerator());
				detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.libelle", null, getLocale()), new LibelleElpColumnGenerator());
				if(configController.isAffECTSIPEtudiant()){
					detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.ects", null, getLocale()), new ECTSColumnGenerator());
					if(configController.isMasqueECTSIPEtudiant()){
						detailInscriptionTable.setColumnCollapsed(applicationContext.getMessage(NAME+".table.elp.ects", null, getLocale()), true);
					}
				}
				detailInscriptionTable.setColumnReorderingAllowed(false);
				detailInscriptionTable.setSelectable(false);
				detailInscriptionTable.setImmediate(true);
				detailInscriptionTable.addStyleName("scrollabletable");
				panelDetailInscription.setContent(detailInscriptionTable);
			}else{
				setHeight(30, Unit.PERCENTAGE);
				HorizontalLayout messageLayout=new HorizontalLayout();
				messageLayout.setSpacing(true);
				messageLayout.setMargin(true);
				Label labelAucuneIp=new Label(applicationContext.getMessage(NAME+".message.aucuneip", null, getLocale()));
				labelAucuneIp.setStyleName(ValoTheme.LABEL_BOLD);
				messageLayout.addComponent(labelAucuneIp);
				panelDetailInscription.setContent(messageLayout);
			}


			layout.addComponent(panelDetailInscription);



			layout.setExpandRatio(panelDetailInscription, 1);


			setContent(layout);


			/* Centre la fenêtre */
			center();

		}
	}


	/** Formats the position in a column containing Date objects. */
	class CodeElpColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getLibelle())){

				//indentation des libelles dans la liste:
				String code = el.getCode();
				if(el.getLevel()==1 && !el.isEpreuve()){
					code="<b>"+code+"</b>";
				}
				if(el.isEpreuve()){
					code="<i>"+code+"</i>";
				}
				libLabel.setValue(code);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class LibelleElpColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getLibelle())){

				//indentation des libelles dans la liste:
				int rg = Integer.valueOf(el.getLevel());
				String libelp = el.getLibelle();
				String lib = "";
				for (int j = 2; j <= rg; j++) {
					lib= lib + "&#160;&#160;&#160;&#160;&#160;";
				}
				if(el.getLevel()==1 && !el.isEpreuve()){
					libelp="<b>"+libelp+"</b>";
				}
				if(el.isEpreuve()){
					libelp="<i>"+libelp+"</i>";
				}
				libLabel.setValue(lib+libelp);

				if(StringUtils.hasText(el.getEcts())){
					libLabel.addStyleName("ects-elp-label");
				}
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class ECTSColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getEcts())){
				libLabel.setValue(el.getEcts());
				libLabel.addStyleName("ects-value-label");
			}

			return libLabel;
		}
	}




}
