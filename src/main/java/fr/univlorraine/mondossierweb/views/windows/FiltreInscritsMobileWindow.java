package fr.univlorraine.mondossierweb.views.windows;

import java.util.List;

import javax.annotation.Resource;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Fenêtre pour filtrer les inscrits sur mobile
 */
@Configurable(preConstruction=true)
public class FiltreInscritsMobileWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "filtreInscritsMobileWindow";

	public static final String TOUTES_LES_ETAPES_LABEL = "toutes";

	public static final String TOUS_LES_GROUPES_LABEL = "tous";

	
	private String typeFavori;
	
	private NativeSelect listeEtapes;

	private NativeSelect listeGroupes;
	
	@Getter
	private String vetSelectionnee;
	
	@Getter
	private String groupeSelectionne;
	
	@Getter
	private boolean demandeFiltrage;
	
	@Resource
	private transient ApplicationContext applicationContext;
	

	/**
	 * Crée une fenêtre
	 */
	public FiltreInscritsMobileWindow(){
		setWidth("90%");
        setHeight("260px");
        setModal(true);
		setResizable(false);

        setCaption("Filtre");
        setStyleName("v-popover-blank");
        
        typeFavori = MdwTouchkitUI.getCurrent().getTypeObjListInscrits();
        
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);
        
        //Si on affiche la liste des inscrits à un ELP
		//on doit affiche l'étape d'appartenance et éventuellement les groupes
		//Affichage d'une liste déroulante contenant la liste des années
		if(typeIsElp()){
	        
			List<VersionEtape> letapes = MdwTouchkitUI.getCurrent().getListeEtapesInscrits();
			if(letapes != null && letapes.size()>0){
				
				
		        Label etapeLabel = new Label("Etape d'appartenance");
		        layout.addComponent(etapeLabel);
		        layout.setComponentAlignment(etapeLabel, Alignment.BOTTOM_LEFT);
				listeEtapes = new NativeSelect();
				listeEtapes.setNullSelectionAllowed(false);
				listeEtapes.setRequired(false);
				listeEtapes.setWidth("100%");
				listeEtapes.addItem(TOUTES_LES_ETAPES_LABEL);
				listeEtapes.setItemCaption(TOUTES_LES_ETAPES_LABEL,TOUTES_LES_ETAPES_LABEL);
				for(VersionEtape etape : letapes){
					String idEtape  = etape.getId().getCod_etp()+"/"+etape.getId().getCod_vrs_vet();
					listeEtapes.addItem(idEtape);
					listeEtapes.setItemCaption(idEtape,"["+idEtape+"] "+etape.getLib_web_vet());
				}

				if(MdwTouchkitUI.getCurrent().getEtapeInscrits()!=null){
					listeEtapes.setValue( MdwTouchkitUI.getCurrent().getEtapeInscrits());
				}else{

					listeEtapes.setValue( TOUTES_LES_ETAPES_LABEL);
				}
				//Gestion de l'événement sur le changement d'étape
				listeEtapes.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						vetSelectionnee = (String) event.getProperty().getValue();
						if(vetSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
							vetSelectionnee = null;
						}
						MdwTouchkitUI.getCurrent().setEtapeInscrits(vetSelectionnee);
						//faire le changement
						groupeSelectionne = ((listeGroupes!=null && listeGroupes.getValue()!=null)?(String)listeGroupes.getValue():null);
						if(groupeSelectionne!=null && groupeSelectionne.equals(TOUS_LES_GROUPES_LABEL)){
							groupeSelectionne=null;
						}
						
						//update de l'affichage
						//initListe();

					}
				});
				layout.addComponent(listeEtapes);

			}

			
			
			List<ElpDeCollection> lgroupes = MdwTouchkitUI.getCurrent().getListeGroupesInscrits();
			if(lgroupes != null && lgroupes.size()>0){
			     
		        HorizontalLayout gLayout = new HorizontalLayout();
		        gLayout.setSizeFull();

		        Label groupeLabel = new Label("Groupe");
		        gLayout.addComponent(groupeLabel);
		        gLayout.setComponentAlignment(groupeLabel, Alignment.MIDDLE_LEFT);

		        layout.addComponent(gLayout);
		        
				listeGroupes = new NativeSelect();
				listeGroupes.setNullSelectionAllowed(false);
				listeGroupes.setRequired(false);
				listeGroupes.setWidth("100%");
				listeGroupes.addItem(TOUS_LES_GROUPES_LABEL);
				listeGroupes.setItemCaption(TOUS_LES_GROUPES_LABEL,TOUS_LES_GROUPES_LABEL);
				for(ElpDeCollection edc : lgroupes){
					for(CollectionDeGroupes cdg : edc.getListeCollection()){
						for(Groupe groupe : cdg.getListeGroupes()){
							listeGroupes.addItem(groupe.getCleGroupe());
							listeGroupes.setItemCaption(groupe.getCleGroupe(),groupe.getLibGroupe());

						}
					}
				}
				if(MdwTouchkitUI.getCurrent().getGroupeInscrits()!=null){
					listeGroupes.setValue( MdwTouchkitUI.getCurrent().getGroupeInscrits());
				}else{
					listeGroupes.setValue(TOUS_LES_GROUPES_LABEL);
				}


				//Gestion de l'événement sur le changement de groupe
				listeGroupes.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						groupeSelectionne = (String) event.getProperty().getValue();
						if(groupeSelectionne.equals(TOUS_LES_GROUPES_LABEL)){
							groupeSelectionne = null;
						}
						MdwTouchkitUI.getCurrent().setGroupeInscrits(groupeSelectionne);
						//faire le changement
						vetSelectionnee = ((listeEtapes!=null && listeEtapes.getValue()!=null)?(String)listeEtapes.getValue():null);
						if(vetSelectionnee!=null && vetSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
							vetSelectionnee=null;
						}
						
						//update de l'affichage
						//initListe();
					}
				});


				layout.addComponent(listeGroupes);

	

			}
		}


        this.setResizable(false);
        

        // Have a close button
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setSizeFull();
  
        Button closeButton = new Button("Filtrer");
        closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        closeButton.addStyleName("v-popover-button");
        //closeButton.setHeight("40px");
        demandeFiltrage=false;
        closeButton.addClickListener(e->{
        	//retourner vetSelectionnee et groupeSelectionne;
        	demandeFiltrage = true;
        	close();
        	
        });
        
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
        layout.addComponent(bLayout);


        setContent(layout);
       
	}




	private boolean typeIsElp(){
		return (typeFavori!=null && typeFavori.equals(Utils.ELP));
	}



	private void refreshListeCodind(BeanItemContainer<Inscrit> ic) {
		/*if(listecodind!=null){
			listecodind.clear();
		}else{
			listecodind = new LinkedList<String>();
		}
		for(Inscrit inscrit : ic.getItemIds()){
			listecodind.add(inscrit.getCod_ind());
		}*/
	}


}
