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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBaseCollectionGroupe;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Fenêtre du détail des groupes
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetailGroupesWindow extends Dialog {

	public static final String NAME = "groupesWindow";

	private List<ElpDeCollection> lgroupes;

	private String elpLibelle;

	private String annee;

	private String vetLibelle;

	/**
	 * Crée une fenêtre
	 */
	public void init(final List<ElpDeCollection> lg, final String elpLib, final String vetLib, final String anneeUniv) {
		lgroupes = lg;
		elpLibelle = elpLib;
		final int anneenplusun = Integer.parseInt(anneeUniv) + 1;
		annee = anneeUniv + "/" + anneenplusun;
		vetLibelle = vetLib;
		init();
	}

	private void init() {

		/* Style */
		setWidth("80%");
		setHeight("95%");
		setResizable(false);
		setCloseOnOutsideClick(true);

		/* Titre */
		setHeaderTitle(getTranslation(NAME + ".title"));

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(true);
		add(layout);

		//Sous titre avec l'année
		final HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setWidthFull();
		titleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		titleLayout.add(new Html("<span>" + getTranslation(NAME + ".label.anneeuniv") + " <b>" + annee + "</b></span>"));

		if (StringUtils.hasText(vetLibelle)) {
			titleLayout.add(new Html("<span>" + getTranslation(NAME + ".label.vet") + " : <b>" + vetLibelle + "</b></span>"));
		}

		layout.add(titleLayout);

		final Div panelDetailGroupes = new Div();
		panelDetailGroupes.setSizeFull();
		final Span titreGroupes = new Span(elpLibelle);
		titreGroupes.addClassName("font-bold");
		panelDetailGroupes.add(titreGroupes);

		if (lgroupes != null && !lgroupes.isEmpty()) {
			final TreeGrid<ObjetBaseCollectionGroupe> detailGroupesTable = new TreeGrid<>();
			detailGroupesTable.setSizeFull();
			detailGroupesTable.addClassName("scrollabletable");

			final TreeData<ObjetBaseCollectionGroupe> treeData = new TreeData<>();
			int id = 0;
			for (final ElpDeCollection edc : lgroupes) {
				id++;
				for (final CollectionDeGroupes cdg : edc.getListeCollection()) {
					id++;
					final ObjetBaseCollectionGroupe obj = new ObjetBaseCollectionGroupe();
					obj.setId("" + id);
					obj.setCod_coll(cdg.getCodCollection());
					obj.setCap_max("");
					obj.setCap_int("");
					obj.setNb_inscrits("");
					treeData.addItem(null, obj);

					for (final Groupe gpe : cdg.getListeGroupes()) {
						id++;
						final ObjetBaseCollectionGroupe objgpe = new ObjetBaseCollectionGroupe();
						objgpe.setId("" + id);
						objgpe.setCod_gpe(gpe.getCodGroupe());
						objgpe.setLib_gpe(gpe.getLibGroupe());
						objgpe.setCap_max("" + gpe.getCapMaxGpe());
						objgpe.setCap_int("" + gpe.getCapIntGpe());
						objgpe.setNb_inscrits("" + gpe.getNbInscrits());
						treeData.addItem(obj, objgpe);
					}
				}
			}

			detailGroupesTable.addHierarchyColumn(ObjetBaseCollectionGroupe::getCod_coll)
				.setHeader(getTranslation(NAME + ".table.codecollection"))
				.setAutoWidth(true).setResizable(true);
			detailGroupesTable.addColumn(ObjetBaseCollectionGroupe::getCod_gpe)
				.setHeader(getTranslation(NAME + ".table.codegroupe"))
				.setAutoWidth(true).setResizable(true);
			detailGroupesTable.addColumn(ObjetBaseCollectionGroupe::getLib_gpe)
				.setHeader(getTranslation(NAME + ".table.libgroupe"))
				.setAutoWidth(true).setResizable(true);
			detailGroupesTable.addColumn(ObjetBaseCollectionGroupe::getCap_max)
				.setHeader(getTranslation(NAME + ".table.capmax"))
				.setAutoWidth(true).setResizable(true);
			detailGroupesTable.addColumn(ObjetBaseCollectionGroupe::getCap_int)
				.setHeader(getTranslation(NAME + ".table.capint"))
				.setAutoWidth(true).setResizable(true);
			detailGroupesTable.addColumn(ObjetBaseCollectionGroupe::getNb_inscrits)
				.setHeader(getTranslation(NAME + ".table.nbinscrits"))
				.setAutoWidth(true).setResizable(true);

			detailGroupesTable.setDataProvider(new TreeDataProvider<>(treeData));
			detailGroupesTable.expand(treeData.getRootItems());

			panelDetailGroupes.add(detailGroupesTable);
		}

		layout.add(panelDetailGroupes);
		layout.setFlexGrow(1, panelDetailGroupes);

		/* Signification des colonnes */
		final Div panelCollectionInfo = new Div();
		panelCollectionInfo.addClassName("significationpanel");
		panelCollectionInfo.setWidthFull();

		final Icon iconeInfo = VaadinIcon.INFO_CIRCLE.create();
		iconeInfo.getStyle().set("width","1em");
		final Span titreInfo = new Span(iconeInfo);
		titreInfo.add(" " + getTranslation(NAME + ".info.title"));
		titreInfo.addClassName("font-bold");
		panelCollectionInfo.add(titreInfo);
		panelCollectionInfo.add(new Html("<div>" + getTranslation(NAME + ".collection.info") + "</div>"));
		layout.add(panelCollectionInfo);
	}
}
