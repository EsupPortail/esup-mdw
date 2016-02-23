/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import lombok.Data;

/**
 * représente un étudiant inscrit lors de la rechercher dans la partie enseignant.
 * @author Charlie Dubois
 */
@Entity
@Data
public class Inscrit {
	/**
	 * le code individu.
	 */
	@Id
	@Column(name="COD_IND")
	private String cod_ind;
	/**
	 * le code etudiant.
	 */
	@Column(name="COD_ETU")
	private String cod_etu;
	/**
	 * le nom.
	 */
	@Column(name="NOM")
	private String nom;
	
	/**
	 * le nom usuel.
	 */
	@Column(name="NOM_USUEL")
	private String nomUsuel;
	/**
	 * le 1er prenom.
	 */
	@Column(name="LIB_PR1_IND")
	private String prenom;
	/**
	 * la date de naissance.
	 */
	@Column(name="date_nai_ind")
	private String date_nai_ind;
	/**
	 * l'iae.
	 */
	@Column(name="iae")
	private String iae;
	/**
	 * le login.
	 */
	private String login;
	/**
	 * la note de la session de juin.
	 */	
	@Column(name="notej")
	private String notej;
	/**
	 * le résultat de la session de juin.
	 */
	@Column(name="resj")
	private String resj;
	/**
	 * la note de la session de septembre.
	 */
	@Column(name="notes")
	private String notes;
	/**
	 * le résultat de septembre.
	 */
	@Column(name="ress")
	private String ress;
	/**
	 * le code étape où l'étudiant est incrit.
	 */
	@Column(name="cod_etp")
	private String cod_etp;
	/**
	 * la version de l'étape.
	 */
	@Column(name="cod_vrs_vet")
	private String cod_vrs_vet;
	/**
	 * le libellé de l'étape.
	 */
	@Column(name="lib_etp")
	private String lib_etp;
	/**
	 * code_etp + / + vers_vet
	 */
	private String id_etp;
	/**
	 * l'e-mail.
	 */
	private String email;
	/**
	 * l'url  de la photo.
	 */
	private String urlphoto;
	/**
	 * la liste des codes des gourpes auxquels appartient l'étudiant séparés par des ;
	 */
	private String codes_groupes;
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "inscrit=  cod_etu : "+cod_etu+" nom : "+getNom()+" prenom : "+prenom;
	}
	/**
	 * constructeur.
	 */
	/*public Inscrit() {
		super();
		cod_ind = "";
		cod_etu = "";
		nom = "";
		prenom = "";
		date_nai_ind = "";
		iae = "";
		notej = "";
		resj = "";
		notes = "";
		ress = "";
		cod_etp = "";
		cod_vrs_vet = "";
		lib_etp = "";
		email = "";
		urlphoto = "";
	}*/

	public String getNom(){
		//Si afichage utilisant le nom usuel
		if(PropertyUtils.getTypeAffichageNomEtatCivil().equals(PropertyUtils.AFFICHAGE_NOM_BASIQUE)
			&& nomUsuel != null && !nomUsuel.equals("")){
			return nomUsuel;
			
		}else if(PropertyUtils.getTypeAffichageNomEtatCivil().equals(PropertyUtils.AFFICHAGE_NOM_STANDARD)
				&& nomUsuel != null && !nomUsuel.equals("") && !nomUsuel.equals(nom)){
				//Si affichage avec nom patronymique ET usuel et si nom usuel non null et différent du nom patronymique
				return nom + " (" + nomUsuel + ")";
			
		}
		return nom;
	}
	
	
}
