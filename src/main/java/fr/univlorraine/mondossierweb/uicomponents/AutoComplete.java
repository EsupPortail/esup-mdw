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
package fr.univlorraine.mondossierweb.uicomponents;


import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.utils.Utils;

public class AutoComplete extends TextField{

	protected  PopupView choicesPopup;
	protected Table choices;

	protected Integer selectedItem=0;



	public void showChoices(List<ResultatDeRecherche> text, Layout layout, Button btnRecherche, boolean touchkitMobileDisplay) {
		//Si du texte est saisi
		if(text.size()>0){
			//Si la popup est déjà instanciée, on la masque
			if(choicesPopup!=null){
				choicesPopup.setPopupVisible(false);

			}
			//Si c'est la première fois que l'on affiche la popup
			if(choices==null){
				//On créé la table contenant les propositions
				choices = new Table();
				//Ajout du libellé dans la table
				choices.addContainerProperty("lib", String.class,  "");
				//Si on est en affichage bureau, on ajoute le type
				if(!touchkitMobileDisplay){
					choices.addContainerProperty("type", String.class,  "");
				}
				//On cache les headers des colonnes
				choices.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
				//la table fait la même largeur que le composant textfield de saisie
				choices.setWidth(getWidth(), getWidthUnits());
				//Ajout du style css googletable
				choices.setStyleName("googletable");
				//Si on est en affichage bureau, on détermine à la main la taille des colonnes
				if(!touchkitMobileDisplay){
					choices.setColumnWidth("lib", 596);
					choices.setColumnWidth("type", 100);
				}

				choices.setImmediate(true);

				//Gestion du clic sur une ligne de la table -> on met la valeut de la ligne dans le textField de saisie
				choices.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						Item i = event.getItem();
						setValue(i.getItemProperty("lib").getValue().toString());
						btnRecherche.click();
					}
				});
			}else{
				//On vide simplement la table de son contenu précédent
				choices.removeAllItems();
			}

			//On parcourt les résultats pour les ajouter à la table
			int i =1;
			for(ResultatDeRecherche r : text){
				Item item = choices.addItem(i);
				item.getItemProperty("lib").setValue(r.getLib());
				//Si on est en affichage bureau, on ajoute le type
				if(!touchkitMobileDisplay){
					item.getItemProperty("type").setValue(transcodeType(r.getType()));
				}
				i++;
			}
			selectedItem=0;
			//On fixe la hauteur de la table en fonction du nombre de résultats affichés
			choices.setHeight(38 * text.size()+1, Unit.PIXELS);		
			//la table fait la même largeur que le composant textfield de saisie
			choices.setWidth(getWidth(), getWidthUnits());

			// Si on n'a encore jamais affiché la popup
			if(choicesPopup==null){
				//on créé la popup
				choicesPopup = new PopupView(new PopupTextFieldContent());
				//On lui ajoute le style css googlepopupview
				choicesPopup.setStyleName("googlepopupview");
				//On ajoute la popup au layout
				layout.addComponent(choicesPopup);
				//la popup fait la même largeur que le composant textfield de saisie
				choicesPopup.setWidth(getWidth(), getWidthUnits());
				choices.setSelectable(true);
				choices.setImmediate(true);
				choicesPopup.addPopupVisibilityListener(new PopupVisibilityListener() {
					@Override
					public void popupVisibilityChange(PopupVisibilityEvent event) {
						if (!event.isPopupVisible()) {
							//On masque la popup quand on perd le focus sur le champ texte
							choicesPopup.setVisible(false);
						}
					}
				});

			}

			//On affiche la popup
			choicesPopup.setVisible(true);
			choicesPopup.setPopupVisible(true);
			//La popup fait la même hauteur que la table qu'elle contient
			choicesPopup.setHeight(choices.getHeight(), choices.getHeightUnits());

		}else{
			//Aucun texte n'ai saisi
			if(choicesPopup!=null){
				//On masque la popup si elle est déjà instanciée
				choicesPopup.setVisible(false);
				choicesPopup.setPopupVisible(false);
			}
		}



	}

	private Object transcodeType(String type) {
		if(type.equals(Utils.ETU)){
			return Utils.TYPE_ETU.toUpperCase();
		}
		if(type.equals(Utils.ELP)){
			String elpType = Utils.TYPE_ELP.toUpperCase();
			return elpType.split(" ")[0];
		}
		if(type.equals(Utils.CMP)){
			return Utils.TYPE_CMP.toUpperCase();
		}
		if(type.equals(Utils.VET)){
			return Utils.TYPE_VET.toUpperCase();
		}
		return type;
	}

	// Create a dynamically updating content for the popup
	public class PopupTextFieldContent implements PopupView.Content {
		@Override
		public final Component getPopupComponent() {
			return choices;
		}
		@Override
		public final String getMinimizedValueAsHTML() {
			return "";
		}
	}

	public PopupView getChoicesPopup() {
		return choicesPopup;
	}

	public void setChoicesPopup(PopupView choicesPopup) {
		this.choicesPopup = choicesPopup;
	}

	public Table getChoices() {
		return choices;
	}

	public void setChoices(Table choices) {
		this.choices = choices;
	}

	public Integer getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Integer seletedItem) {
		this.selectedItem = seletedItem;
	}

	public Integer getNextItem() {
		if(this.selectedItem < choices.getItemIds().size()){
			this.selectedItem = this.selectedItem + 1;
		}
		return this.selectedItem;
	};

	public Integer getPreviousItem() {
		if(this.selectedItem > 0){
			this.selectedItem = this.selectedItem - 1;
		}
		return this.selectedItem;
	};




}
