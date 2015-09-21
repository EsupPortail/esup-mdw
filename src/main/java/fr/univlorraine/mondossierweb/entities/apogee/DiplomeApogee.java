/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


/**
 * The persistent class for the DIPLOME database table.
 * 
 */
@Entity
@Table(name="DIPLOME")
@Data
public class DiplomeApogee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="COD_DIP")
	private String codDip;

	@Column(name="LIB_DIP")
	private String libDip;

	
	@Column(name="COD_TPD_ETB")
	private String codTpdEtb;


	

}