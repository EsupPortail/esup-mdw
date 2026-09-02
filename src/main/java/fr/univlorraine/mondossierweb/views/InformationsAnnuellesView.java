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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.InfosAnnuelles;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import jakarta.annotation.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Page des informations annuelles
 */
@SuppressWarnings("serial")
@Route(value = InformationsAnnuellesView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
public class InformationsAnnuellesView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "informationsAnnuellesView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue à chaque navigation.
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		removeAll();

		final Etudiant etudiant = sessionController.getEtudiant();

		//On vérifie le droit d'accéder à la vue
		if (etudiant != null
				&& (userController.isEtudiant()
					|| (userController.isEnseignant() && configController.isAffInfosAnnuellesEnseignant())
					|| (userController.isGestionnaire() && configController.isAffInfosAnnuellesGestionnaire()))) {
			/* Style */
			setPadding(true);
			setSpacing(true);

			//Layout avec les blocs d'informations annuelles (responsive : les blocs passent à la ligne)
			Div globalLayout = new Div();
			globalLayout.setWidthFull();
			globalLayout.getStyle()
				.set("display", "flex")
				.set("flex-wrap", "wrap")
				.set("gap", "var(--lumo-space-m)");

			for (InfosAnnuelles infos : etudiant.getInfosAnnuelles()) {
				FormLayout formInfosLayout = new FormLayout();
				formInfosLayout.setWidthFull();
				formInfosLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

				//Numéro Anonymat visible que si l'utilisateur est étudiant
				if (userController.isEtudiant()) {
					List<Anonymat> lano = infos.getNumerosAnonymat();
					if (lano != null) {
						//Si l'étudiant n'a qu'un seul numéro d'anonymat
						if (lano.size() == 1) {
							addFormValue(formInfosLayout, getTranslation(NAME + ".numanonymat.title"),
								lano.get(0).getCod_etu_ano(), getTranslation(NAME + ".numanonymat.description"));
						}
						//Si l'étudiant a plusieurs numéros d'anonymat
						if (lano.size() > 1) {
							int i = 0;
							for (Anonymat ano : lano) {
								String captionNumAnonymat = "";
								String description = null;
								if (i == 0) {
									//Pour le premier numéro affiché on affiche le libellé du champ et l'info bulle
									captionNumAnonymat = getTranslation(NAME + ".numanonymats.title");
									description = getTranslation(NAME + ".numanonymat.description");
								}
								addFormValue(formInfosLayout, captionNumAnonymat,
									ano.getCod_etu_ano() + " (" + ano.getLib_man() + ")", description);
								i++;
							}
						}
					}
				}

				if (userController.isEtudiant()
						|| (userController.isEnseignant() && configController.isAffBoursierEnseignant())
						|| (userController.isGestionnaire() && configController.isAffBoursierGestionnaire())) {
					addFormValue(formInfosLayout, getTranslation(NAME + ".boursier.title"),
						infos.isBoursier() ? getTranslation(NAME + ".boursier.oui") : getTranslation(NAME + ".boursier.non"), null);
				}
				if (userController.isEtudiant()
						|| (userController.isEnseignant() && configController.isAffSalarieEnseignant())
						|| (userController.isGestionnaire() && configController.isAffSalarieGestionnaire())) {
					addFormValue(formInfosLayout, getTranslation(NAME + ".salarie.title"),
						infos.isTemSalarie() ? getTranslation(NAME + ".salarie.oui") : getTranslation(NAME + ".salarie.non"), null);
				}
				if (userController.isEtudiant()
						|| (userController.isEnseignant() && configController.isAffAmenagementEnseignant())
						|| (userController.isGestionnaire() && configController.isAffAmenagementGestionnaire())) {
					addFormValue(formInfosLayout, getTranslation(NAME + ".amenagementetude.title"),
						infos.isTemAmenagementEtude() ? getTranslation(NAME + ".amenagementetude.oui") : getTranslation(NAME + ".amenagementetude.non"), null);
				}

				globalLayout.add(createPanel(getTranslation(NAME + ".infos.title") + " " + infos.getLibelle(), formInfosLayout));
			}
			add(globalLayout);
		}
	}

	/**
	 * Ajoute un couple libellé/valeur (valeur en gras) au formulaire,
	 * avec éventuellement une info-bulle.
	 */
	private void addFormValue(FormLayout formLayout, String caption, String value, String description) {
		Span valueSpan = new Span(StringUtils.hasText(value) ? value : "");
		valueSpan.addClassName(LumoUtility.FontWeight.BOLD);
		if (StringUtils.hasText(description)) {
			HorizontalLayout valueLayout = new HorizontalLayout();
			valueLayout.setSpacing(true);
			valueLayout.setPadding(false);
			valueLayout.add(valueSpan);
			Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
			infoIcon.setSize("var(--lumo-icon-size-s)");
			infoIcon.getElement().setAttribute("title", description);
			valueLayout.add(infoIcon);
			formLayout.addFormItem(valueLayout, caption != null ? caption : "");
		} else {
			formLayout.addFormItem(valueSpan, caption != null ? caption : "");
		}
	}

	/**
	 * Panel (ancien com.vaadin.ui.Panel) : bloc avec un titre et une bordure.
	 */
	private Div createPanel(String titre, Component content) {
		Div panel = new Div();
		panel.getStyle()
			.set("border", "1px solid var(--lumo-contrast-20pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("overflow", "hidden")
			.set("flex", "1 1 20rem");
		if (StringUtils.hasText(titre)) {
			Div header = new Div(new Span(titre));
			header.getStyle()
				.set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
				.set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
				.set("background-color", "var(--lumo-contrast-5pct)")
				.set("font-weight", "600");
			panel.add(header);
		}
		Div body = new Div(content);
		body.getStyle().set("padding", "var(--lumo-space-m)");
		panel.add(body);
		return panel;
	}

}
