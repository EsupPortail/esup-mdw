/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class VersionDiplomePK implements Serializable {
	

	private String cod_dip;


	private String cod_vrs_vdi;

}
