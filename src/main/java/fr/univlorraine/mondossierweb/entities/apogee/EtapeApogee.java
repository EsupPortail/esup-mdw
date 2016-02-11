/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


/**
 * The persistent class for the ETAPE database table.
 * 
 */
@Entity
@Table(name="ETAPE")
@Data
public class EtapeApogee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="COD_ETP")
	private String codEtp;

	/*@Column(name="COD_ACT")
	private String codAct;*/

	/*@Column(name="COD_CUR")
	private String codCur;

	@Column(name="COD_CYC")
	private String codCyc;
*/
	@Column(name="LIB_ETP")
	private String libEtp;

	@Column(name="LIC_ETP")
	private String licEtp;

	/*@Column(name="NBR_MAX_IAE_AUT")
	private BigDecimal nbrMaxIaeAut;

	@Column(name="NBR_MAX_INSC_DEUG")
	private BigDecimal nbrMaxInscDeug;

	@Column(name="TEM_COU_ACC_TRV_ETP")
	private String temCouAccTrvEtp;

	@Column(name="TEM_OUV_DRT_SSO_ETP")
	private String temOuvDrtSsoEtp;

	@Column(name="TEM_TYP_OPI")
	private String temTypOpi;*/

	//bi-directional many-to-one association to VersionEtape
	/*@OneToMany(mappedBy="etape")
	private List<VersionEtape> versionEtapes;*/

}