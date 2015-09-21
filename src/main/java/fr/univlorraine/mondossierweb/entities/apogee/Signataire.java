/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * Représente un signataire.
 * 
 * @author Charlie Dubois
 */
@Entity
@Data
public class Signataire {

	/**
	 * Code du signataire.
	 */
	@Id
	@Column(name = "COD_SIG")
	private String cod_sig;

	/**
	 * Nom du signataire.
	 */
	@Column(name = "NOM_SIG")
	private String nom_sig;

	/**
	 * Qualité du signataire.
	 */
	@Column(name = "QUA_SIG")
	private String qua_sig;

	/**
	 * Image de la signature digitalisée
	 */
	@Column(name = "IMG_SIG_STD")
	private byte[] img_sig_std;

	
}
