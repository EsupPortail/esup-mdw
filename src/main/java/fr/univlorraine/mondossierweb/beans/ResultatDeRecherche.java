package fr.univlorraine.mondossierweb.beans;

import java.io.Serializable;
import java.util.Map;

import org.springframework.util.StringUtils;

import fr.univlorraine.mondossierweb.entities.apogee.VObjSeApogee;
import lombok.Data;

@Data
public class ResultatDeRecherche implements Serializable {
	
	
	public String code;
	
	public String lib;
	
	public String type;
	

	
	public ResultatDeRecherche(VObjSeApogee o){
		type=o.getId().getTypObj();
		code=o.getId().getCodObj();
		if(o.getId().getCodVrsObj()!=0)
			code = o.getId().getCodObj()+"/"+o.getId().getCodVrsObj();
		lib=o.getLibObj();
	}
	
	
	public ResultatDeRecherche(Map<String,Object> o){
		type=(String)o.get("TYP_OBJ");
		code=(String)o.get("COD_OBJ");
		if(((Integer)o.get("COD_VRS_OBJ"))!=0)
			code = o.get("COD_OBJ")+"/"+o.get("COD_VRS_OBJ");
		/*if(StringUtils.hasText((String)o.get("LIB_DESC_OBJ"))){
			lib=(String)o.get("LIB_DESC_OBJ"); //On prend  la description
		}else{
			//C'est un elp
			lib="["+code+"] "+(String)o.get("LIB_OBJ"); //On prend le libelle
		}*/
		lib="["+code+"] "+(String)o.get("LIB_OBJ"); //On prend le libelle
	}
	

	public ResultatDeRecherche() {
		super();
	}
	
	
	
	@Override
	public String toString() {
		return lib;
	}

	public ResultatDeRecherche(String code, String lib, String type) {
		super();
		this.type = type;
		this.code = code;
		this.lib = lib;
	}
}
