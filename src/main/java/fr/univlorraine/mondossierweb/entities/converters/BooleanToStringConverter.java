package fr.univlorraine.mondossierweb.entities.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

	private static final String YES = "O";
	private static final String NO = "N";

	@Override
	public String convertToDatabaseColumn(final Boolean entityValue) {
		if (entityValue == null) {
			return null;
		}
		if (entityValue) {
			return YES;
		} else {
			return NO;
		}
	}

	@Override
	public Boolean convertToEntityAttribute(final String databaseValue) {
		if (databaseValue == null) {
			return null;
		}
		if (databaseValue.equals("O")) {
			return true;
		} else {
			return false;
		}
	}
}


