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
package fr.univlorraine.mondossierweb.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.InfosAnnuelles;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = InformationsAnnuellesView.NAME)
@PreAuthorize("@userController.hasRoleInProperty('consultation_dossier')")
public class InformationsAnnuellesView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "informationsAnnuellesView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null &&
			(userController.isEtudiant() || 
				(userController.isEnseignant() && configController.isAffInfosAnnuellesEnseignant()) || 
				(userController.isGestionnaire() && configController.isAffInfosAnnuellesGestionnaire()))){
			/* Style */
			setMargin(true);
			setSpacing(true);

			/* Titre */
			Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			addComponent(title);


			HorizontalLayout globalLayout = new HorizontalLayout();
			globalLayout.setSizeFull();
			globalLayout.setSpacing(true);

			for(InfosAnnuelles infos : MainUI.getCurrent().getEtudiant().getInfosAnnuelles()) {
				Panel panelInfos= new Panel(applicationContext.getMessage(NAME+".infos.title", null, getLocale())+" "+infos.getLibelle());

				FormLayout formInfosLayout = new FormLayout();
				formInfosLayout.setSpacing(true);
				formInfosLayout.setMargin(true);

				//Numéro Anonymat visible que si l'utilisateur est étudiant
				List<Anonymat> lano = null;
				if(userController.isEtudiant()){
					lano = infos.getNumerosAnonymat();
					if(lano!=null) {
						//Si l'étudiant n'a qu'un seul numéro d'anonymat
						if(lano.size()==1){
							String captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymat.title", null, getLocale());
							TextField fieldNumAnonymat = new TextField(captionNumAnonymat, infos.getNumerosAnonymat().get(0).getCod_etu_ano());
							formatTextField(fieldNumAnonymat);
							fieldNumAnonymat.setIcon(FontAwesome.INFO_CIRCLE);
							fieldNumAnonymat.setDescription(applicationContext.getMessage(NAME+".numanonymat.description", null, getLocale()));
							formInfosLayout.addComponent(fieldNumAnonymat);
						}
						//Si l'étudiant a plusieurs numéros d'anonymat
						if(lano.size()>1){
							int i=0;
							for(Anonymat ano : lano){
								String captionNumAnonymat = "";
								if(i==0){
									//Pour le premier numéro affiché on affiche le libellé du champ
									captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymats.title", null, getLocale());
								}
								TextField fieldNumAnonymat = new TextField(captionNumAnonymat, ano.getCod_etu_ano()+ " ("+ano.getLib_man()+")");
								formatTextField(fieldNumAnonymat);
								if(i==0){
									//Pour le premier numéro affiché on affiche l'info bulle
									fieldNumAnonymat.setIcon(FontAwesome.INFO_CIRCLE);
									fieldNumAnonymat.setDescription(applicationContext.getMessage(NAME+".numanonymat.description", null, getLocale()));
								}
								formInfosLayout.addComponent(fieldNumAnonymat);
								i++;
							}
						}
					}
				}

				if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffBoursierEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffBoursierGestionnaire())) {
					String captionBousier = applicationContext.getMessage(NAME+".boursier.title", null, getLocale());
					TextField fieldNumBoursier = new TextField(captionBousier, infos.isBoursier()  ? applicationContext.getMessage(NAME+".boursier.oui", null, getLocale()) : applicationContext.getMessage(NAME+".boursier.non", null, getLocale()));
					formatTextField(fieldNumBoursier);
					formInfosLayout.addComponent(fieldNumBoursier);
				}
				if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffSalarieEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffSalarieGestionnaire())) {
					String captionSalarie = applicationContext.getMessage(NAME+".salarie.title", null, getLocale());
					TextField fieldSalarie = new TextField(captionSalarie, infos.isTemSalarie() == true ? applicationContext.getMessage(NAME+".salarie.oui", null, getLocale()) : applicationContext.getMessage(NAME+".salarie.non", null, getLocale()));
					formatTextField(fieldSalarie);
					formInfosLayout.addComponent(fieldSalarie);
				}
				if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffAmenagementEnseignant()) ||
					(userController.isGestionnaire() && configController.isAffAmenagementGestionnaire())) {
					String captionAmenagementEtude = applicationContext.getMessage(NAME+".amenagementetude.title", null, getLocale());
					TextField fieldAmenagementEtude = new TextField(captionAmenagementEtude, infos.isTemAmenagementEtude()==true ? applicationContext.getMessage(NAME+".amenagementetude.oui", null, getLocale()) : applicationContext.getMessage(NAME+".amenagementetude.non", null, getLocale()));
					formatTextField(fieldAmenagementEtude);
					formInfosLayout.addComponent(fieldAmenagementEtude);
				}


				panelInfos.setContent(formInfosLayout);
				globalLayout.addComponent(panelInfos);
			}
			addComponent(globalLayout);
		}

	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}

}
