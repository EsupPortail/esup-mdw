/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.utils.miscellaneous;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import com.vaadin.ui.Button;

@Data
public class ReferencedButton {

	@Getter
	@Setter
	private Button button;
	
	@Getter
	@Setter
	private String idObj;
	
	
}
