/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="COMPOSANTE")
@Data
public class Composante {

	@Id
	@Column(name="COD_CMP")
	private String codCmp;
	
	@Column(name="LIB_CMP")
	private String libCmp;

	@Column(name="LIC_CMP")
	private String licCmp;
	
	@Column(name="LIB_CMT_CMP")
	private String libCmtCmp;
	
	@Column(name="tem_en_sve_cmp")
	private String temEnSveCmp;


}
