package fr.univlorraine.mondossierweb.uicomponents;


import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

public class AutoComplete extends TextField{

	protected  PopupView choicesPopup;
	protected Table choices;

	protected Integer selectedItem=0;



	public void showChoices(List<String> text, Layout layout) {

		/*	if(choicesPopup!=null){
			layout.removeComponent(choicesPopup);
		}*/
		if(text.size()>0){
			if(choices==null){

				choices = new Table();
				choices.addContainerProperty("", String.class,  null);
				choices.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
				choices.setWidth(getWidth(), Unit.PIXELS);
				choices.setStyleName("googletable");

				choices.setImmediate(true);
				choices.addItemClickListener(new ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						Item i = event.getItem();
						//System.out.println("item selected : "+i);
						setValue(i.toString());

					}
				});


			}else{
				choices.removeAllItems();
			}
			//System.out.println("show Choices "+text);
			int i =1;
			for(String s : text){
				choices.addItem(new Object[] {s}, new Integer(i));
				i++;
			}
			selectedItem=0;
			choices.setHeight(40 * text.size(), Unit.PIXELS);
			choices.setWidth(getWidth(), Unit.PIXELS);
			// Show popup
			if(choicesPopup==null){
				choicesPopup = new PopupView(new PopupTextFieldContent());
				choicesPopup.setStyleName("googlepopupview");
				layout.addComponent(choicesPopup);
				choicesPopup.setWidth(getWidth(), Unit.PIXELS);
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
				//choices.setSizeFull();
				//choicesPopup.setSizeFull();
			}

			//layout.addComponent(choicesPopup);
			choicesPopup.setVisible(true);
			choicesPopup.setPopupVisible(true);
			choicesPopup.setHeight(40 * text.size(), Unit.PIXELS);


			//addBlurListener(e -> choicesPopup.setVisible(false));



		}else{
			if(choicesPopup!=null){
				choicesPopup.setVisible(false);
				choicesPopup.setPopupVisible(false);
			}
		}



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
