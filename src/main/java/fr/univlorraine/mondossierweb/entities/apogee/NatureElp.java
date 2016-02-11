/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Représente une nature d'ELP.
 * 
 * @author Charlie Dubois
 */
@Entity
@Table(name="NATURE_ELP")
@Data
public class NatureElp {

	/**
	 * Code nature elp.
	 */
	@Id
	@Column(name = "COD_NEL")
	private String cod_nel;

	/**
	 * libelle long.
	 */
	@Column(name = "LIB_NEL")
	private String lib_nel;

	/**
	 * libelle court.
	 */
	@Column(name = "LIC_NEL")
	private String lic_nel;

	/**
	 * témoin en service.
	 */
	@Column(name = "TEM_EN_SVE_NEL")
	private String tem_en_sve_nel;
	
}
