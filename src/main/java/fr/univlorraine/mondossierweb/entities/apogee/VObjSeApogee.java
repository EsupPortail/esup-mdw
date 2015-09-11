package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;


/**
 * The persistent class for the ETAPE database table.
 * 
 */
@Entity
@Table(name="V_OBJ_SE")
@Data
public class VObjSeApogee implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@EmbeddedId
	private VObjSeApogeePK id;
	
	
	/*@Id
	@Column(name="COD_OBJ")
	private String codObj;
	
	@Column(name="COD_VRS_OBJ")
	private int codVrsObj;

	@Column(name="TYP_OBJ")
	private String typObj;*/

	@Column(name="LIB_OBJ")
	private String libObj;

	

}