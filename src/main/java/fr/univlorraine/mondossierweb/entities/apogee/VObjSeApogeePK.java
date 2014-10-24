package fr.univlorraine.mondossierweb.entities.apogee;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * The primary key class for the VERSION_ETAPE database table.
 * 
 */
@Embeddable
@Data
public class VObjSeApogeePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;


	@Column(name="COD_OBJ")
	private String codObj;
	
	@Column(name="COD_VRS_OBJ")
	private int codVrsObj;

	@Column(name="TYP_OBJ")
	private String typObj;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VObjSeApogeePK other = (VObjSeApogeePK) obj;
		if (codObj == null) {
			if (other.codObj != null)
				return false;
		} else if (!codObj.equals(other.codObj))
			return false;
		if (codVrsObj != other.codVrsObj)
			return false;
		if (typObj == null) {
			if (other.typObj != null)
				return false;
		} else if (!typObj.equals(other.typObj))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codObj == null) ? 0 : codObj.hashCode());
		result = prime * result + codVrsObj;
		result = prime * result + ((typObj == null) ? 0 : typObj.hashCode());
		return result;
	}

	
}