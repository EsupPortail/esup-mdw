package fr.univlorraine.mondossierweb.uicomponents;


import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;

public class AutoComplete extends TextField{

	protected  PopupView choicesPopup;
	protected Table choices;

	protected Integer selectedItem=0;



	public void showChoices(List<ResultatDeRecherche> text, Layout layout, boolean touchkitMobileDisplay) {



		if(text.size()>0){
			

			if(choicesPopup!=null){
				choicesPopup.setPopupVisible(false);

			}
		
			
			if(choices==null){

				choices = new Table();
				choices.addContainerProperty("lib", String.class,  "");
				choices.addContainerProperty("type", String.class,  "");
				choices.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
				choices.setWidth(getWidth(), getWidthUnits());
				choices.setStyleName("googletable");
				if(!touchkitMobileDisplay){
				choices.setColumnWidth("lib", 596);
				choices.setColumnWidth("type", 100);
				}
			
				choices.setImmediate(true);
				choices.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						Item i = event.getItem();
						setValue(i.getItemProperty("lib").getValue().toString());

					}
				});


			}else{
				choices.removeAllItems();
			}

			int i =1;
			for(ResultatDeRecherche r : text){
				Item item = choices.addItem(i);
				item.getItemProperty("lib").setValue(r.getLib());
				item.getItemProperty("type").setValue(transcodeType(r.getType()));
				i++;
			}
			selectedItem=0;
			choices.setHeight(38 * text.size()+1, Unit.PIXELS);
			choices.setWidth(getWidth(), Unit.PIXELS);
			
			// Show popup
			if(choicesPopup==null){
				choicesPopup = new PopupView(new PopupTextFieldContent());
				choicesPopup.setStyleName("googlepopupview");
				layout.addComponent(choicesPopup);
				//layout.setComponentAlignment(choicesPopup, Alignment.TOP_CENTER);
				choicesPopup.setWidth(getWidth(), getWidthUnits());
				choices.setSelectable(true);
				choices.setImmediate(true);
				choicesPopup.addPopupVisibilityListener(new PopupVisibilityListener() {
	                @Override
	                public void popupVisibilityChange(PopupVisibilityEvent event) {
	                    if (!event.isPopupVisible()) {
	                    	choicesPopup.setVisible(false);
	                    }
	                }
	            });

			}

			choicesPopup.setVisible(true);
			choicesPopup.setPopupVisible(true);
			//choicesPopup.setHeight(40 * text.size(), Unit.PIXELS);
			//choicesPopup.addStyleName("googlepopupview");
			choicesPopup.setHeight(choices.getHeight(), Unit.PIXELS);

		

		}else{
			if(choicesPopup!=null){
				choicesPopup.setVisible(false);
				choicesPopup.setPopupVisible(false);
			}
		}



	}

	private Object transcodeType(String type) {
		if(type.equals("ETU")){
			return "ETUDIANT";
		}
		if(type.equals("ELP")){
			return "ELEMENT";
		}
		if(type.equals("CMP")){
			return "COMPOSANTE";
		}
		if(type.equals("VET")){
			return "ETAPE";
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
