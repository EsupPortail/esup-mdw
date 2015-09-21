/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.SqlResultSetMapping;

import lombok.Data;

@Entity
@SqlResultSetMapping(name="VersionDiplome",
entities={
		@EntityResult(entityClass=VersionDiplome.class, fields={
			@FieldResult(name="id.cod_dip", column="cod_dip"),
			@FieldResult(name="id.cod_vrs_vdi", column="cod_vrs_vdi"),
			@FieldResult(name="lib_web_vdi", column="lib_web_vdi"),
			@FieldResult(name="cod_tpd_etb", column="cod_tpd_etb")
		})
})
@Data
public class VersionDiplome {

	
	@EmbeddedId
	private VersionDiplomePK id;
	
	private String lib_web_vdi;
	
	private String cod_tpd_etb;


}
