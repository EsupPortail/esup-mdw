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
package fr.univlorraine.mondossierweb.entities.apogee;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.List;

/**
 * représente un étudiant inscrit lors de la rechercher dans la partie enseignant.
 * @author Charlie Dubois
 */
@Entity
@Data
public class Inscrit {
	
	@Id
	private int rownum;
	/**
	 * le code individu.
	 */
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
	 * Temoin IP VET
	 */
	@Column(name="ipe")
	private String ipe;
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
	
	private List<Vet> liste_vet;
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
	
	public void ajoutVet(String code, String version, String libelle) {
		Vet vet = new Vet(code, version, libelle);
		liste_vet.add(vet);
	}
	
	@Data
	public class Vet {
		private String cod_etp;
		private String cod_vrs_vet;
		private String lib_etp;
		
		public Vet() {
			super();
		}
		
		public Vet(String cod_etp, String cod_vrs_vet, String lib_etp) {
			super();
			this.cod_etp = cod_etp;
			this.cod_vrs_vet = cod_vrs_vet;
			this.lib_etp = lib_etp;
		}
		
		
	}
	
}
