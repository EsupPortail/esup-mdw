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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.ResultatController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Fenêtre du détail de l'inscription
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetailInscriptionWindow extends Dialog {

	public static final String NAME = "inscriptionWindow";

	/** Indentation maximale (en nombre de niveaux) des libellés dans les cartes mobile. */
	private static final int ELP_CARD_MAX_INDENT = 5;

	@Resource
	private transient UserController userController;
	@Resource(name = "${resultat.implementation}")
	private transient ResultatController resultatController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;

	private Etape etape;

	/**
	 * Crée une fenêtre
	 */
	public void init(Etape et) {
		etape = et;

		//On vérifie le droit d'accéder à la vue
		if ((userController.isEnseignant() || userController.isEtudiant()) && sessionController.getEtudiant() != null) {

			/* Style */
			setWidth("80%");
			setHeight("95%");
			setResizable(false);
			setCloseOnOutsideClick(true);
			addClassName("detail-inscription-window");

			//Test si user enseignant
			if (userController.isEnseignant()) {
				//On recupere le détail de l'IP pour un enseignant
				resultatController.renseigneDetailInscription(etape, userController.isGestionnaire());
			} else {
				//On récupère le détail de l'IP pour un étudiant
				resultatController.renseigneDetailInscriptionEtudiant(etape);
			}

			/* Titre */
			setHeaderTitle(getTranslation(NAME + ".title"));

			/* Layout */
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setPadding(false);
			layout.setSpacing(true);

			//Sous titre avec l'année
			Span labelAnneeUniv = new Span();
			labelAnneeUniv.add(new Span(getTranslation(NAME + ".label.anneeuniv") + " "));
			Span annee = new Span(etape.getAnnee());
			annee.addClassName(LumoUtility.FontWeight.BOLD);
			labelAnneeUniv.add(annee);
			layout.add(labelAnneeUniv);

			Component detailContent;
			List<ElementPedagogique> lelp = sessionController.getEtudiant().getElementsPedagogiques();
			if (lelp != null && !lelp.isEmpty()) {
				ResponsiveCardList<ElementPedagogique> detailInscriptionList = new ResponsiveCardList<>();
				detailInscriptionList.setWidthFull();
				detailInscriptionList.setAllRowsVisible(true);
				detailInscriptionList.addComponentColumn(this::buildCodeElpColumn,
						getTranslation(NAME + ".table.elp.code"))
					.setFlexGrow(1);
				detailInscriptionList.addComponentColumn(this::buildLibelleElpColumn,
						getTranslation(NAME + ".table.elp.libelle"))
					.setFlexGrow(4);
				if (configController.isAffECTSIPEtudiant()) {
					detailInscriptionList.addComponentColumn(this::buildEctsColumn,
							getTranslation(NAME + ".table.elp.ects"))
						.setFlexGrow(1)
						.setVisible(!configController.isMasqueECTSIPEtudiant());
				}
				/* Cartes affichées sur petit écran */
				detailInscriptionList.setCardRenderer(this::createElpCard);
				detailInscriptionList.setItems(lelp);
				detailContent = detailInscriptionList;
			} else {
				setHeight("30%");
				Span labelAucuneIp = new Span(getTranslation(NAME + ".message.aucuneip"));
				labelAucuneIp.addClassName(LumoUtility.FontWeight.BOLD);
				detailContent = labelAucuneIp;
			}
			Div panelDetailInscription = createPanel(etape.getLibelle(), detailContent);
			panelDetailInscription.setSizeFull();
			layout.add(panelDetailInscription);
			layout.setFlexGrow(1, panelDetailInscription);

			/* Bouton de fermeture, aligné en bas */
			final Button btnFermer = new Button(getTranslation(NAME + ".btnFermer"), e -> close());
			btnFermer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			final HorizontalLayout boutonsLayout = new HorizontalLayout(btnFermer);
			boutonsLayout.setWidthFull();
			boutonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			layout.add(boutonsLayout);

			add(layout);
		}
	}

	/**
	 * Colonne code de l'élément pédagogique (ancienne CodeElpColumnGenerator).
	 */
	private Component buildCodeElpColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			libLabel.setText(el.getCode());
			applyElpStyle(libLabel, el);
		}
		return libLabel;
	}

	/**
	 * Colonne libellé de l'élément pédagogique, indenté selon le niveau
	 * (ancienne LibelleElpColumnGenerator).
	 */
	private Component buildLibelleElpColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			libLabel.setText(el.getLibelle());
			applyElpStyle(libLabel, el);
			//indentation des libelles dans la liste
			if (el.getLevel() > 1) {
				libLabel.getStyle().set("padding-inline-start", ((el.getLevel() - 1) * 1.5) + "em");
			}
			if (StringUtils.hasText(el.getEcts())) {
				libLabel.addClassName("ects-elp-label");
			}
		}
		return libLabel;
	}

	/**
	 * Colonne ECTS de l'élément pédagogique (ancienne ECTSColumnGenerator).
	 */
	private Component buildEctsColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getEcts())) {
			libLabel.setText(el.getEcts());
			libLabel.addClassName("ects-value-label");
		}
		return libLabel;
	}

	/**
	 * Construit la carte d'un élément pédagogique, affichée sur petit écran :
	 * badge du code (+ ECTS éventuel) et libellé indenté selon le niveau.
	 * L'indentation est plafonnée pour préserver la largeur de lecture.
	 */
	private Div createElpCard(final ElementPedagogique el) {
		final Div card = new Div();
		card.addClassName("card");

		/* Indentation selon le niveau (plafonnée), appliquée à l'en-tête et au libellé */
		final int indent = Math.min(Math.max(el.getLevel() - 1, 0), ELP_CARD_MAX_INDENT);
		final String indentStyle = indent > 0 ? indent + "em" : null;

		/* Ligne 1 : badge du code + ECTS éventuel */
		final HorizontalLayout ligneEntete = new HorizontalLayout();
		ligneEntete.setSpacing(true);
		ligneEntete.setAlignItems(FlexComponent.Alignment.CENTER);
		if (StringUtils.hasText(el.getLibelle()) && StringUtils.hasText(el.getCode())) {
			final Span badgeCode = new Span(el.getCode());
			badgeCode.addClassName("card-badge");
			ligneEntete.add(badgeCode);
		}
		if (StringUtils.hasText(el.getEcts())) {
			final Span badgeEcts = new Span(el.getEcts());
			badgeEcts.addClassName("card-badge");
			ligneEntete.add(badgeEcts);
		}
		if (indentStyle != null) {
			ligneEntete.getStyle().set("padding-inline-start", indentStyle);
		}
		if (ligneEntete.getComponentCount() > 0) {
			card.add(ligneEntete);
		}

		/* Ligne 2 : libellé indenté selon le niveau (indentation plafonnée) */
		final Span libelle = new Span(el.getLibelle() != null ? el.getLibelle() : "");
		applyElpStyle(libelle, el);
		if (indentStyle != null) {
			libelle.getStyle().set("padding-inline-start", indentStyle);
		}
		card.add(libelle);

		return card;
	}

	/**
	 * Style du contenu d'une cellule : gras pour les éléments de niveau 1, italique pour les épreuves.
	 */
	private void applyElpStyle(Span span, ElementPedagogique el) {
		if (el.getLevel() == 1 && !el.isEpreuve()) {
			span.addClassName(LumoUtility.FontWeight.BOLD);
		}
		if (el.isEpreuve()) {
			span.getStyle().set("font-style", "italic");
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
			.set("display", "flex")
			.set("flex-direction", "column");
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
		body.getStyle()
			.set("padding", "var(--lumo-space-s)")
			.set("flex", "1 1 auto")
			.set("overflow", "auto");
		panel.add(body);
		return panel;
	}
}
