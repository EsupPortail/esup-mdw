/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import lombok.Data;

@Entity
@SqlResultSetMapping(name="VersionEtape",
entities={
		@EntityResult(entityClass=VersionEtape.class, fields={
			@FieldResult(name="id.cod_etp", column="cod_etp"),
			@FieldResult(name="id.cod_vrs_vet", column="cod_vrs_vet"),
			@FieldResult(name="lib_web_vet", column="lib_web_vet")
		})
})
@Table(name="VERSION_ETAPE")
@Data
public class VersionEtape {

	
	@EmbeddedId
	private VersionEtapePK id;
	
	private String lib_web_vet;


}
