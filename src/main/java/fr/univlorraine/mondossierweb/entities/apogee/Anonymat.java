/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * représente un numéro d'anonymat
 * @author Charlie Dubois
 */
@Entity
@Data
public class Anonymat {
	
	/**
	 * id de l'anonymat.
	 */
	@Id
	@Column(name="ID")
	private int id;
	
	/**
	 * le numero d'anonymat.
	 */
	@Column(name="COD_ETU_ANO")
	private String cod_etu_ano;
	
	/**
	 * le libelle de la maquette d'anonymat.
	 */
	@Column(name="LIB_MAN")
	private String lib_man;
	

	
	
}
