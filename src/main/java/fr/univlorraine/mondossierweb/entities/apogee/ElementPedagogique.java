/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import lombok.Data;

@Entity
@SqlResultSetMapping(name="ElementPedagogique",
entities={
		@EntityResult(entityClass=ElementPedagogique.class, fields={
			@FieldResult(name="cod_elp", column="cod_elp"),
			@FieldResult(name="lib_elp", column="lib_elp")
		})
})
@Table(name="ELEMENT_PEDAGOGI")
@Data
public class ElementPedagogique implements Serializable {
	
	@Id
	private String cod_elp;

	private String lib_elp;
	
	

	
}
