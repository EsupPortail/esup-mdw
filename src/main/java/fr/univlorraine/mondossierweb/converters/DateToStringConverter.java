package fr.univlorraine.mondossierweb.converters;

import java.util.*;

import com.vaadin.data.util.converter.Converter;

import fr.univlorraine.mondossierweb.utils.Utils;

@SuppressWarnings("serial")
public class DateToStringConverter implements Converter<String, Date> {

	@Override
	public Date convertToModel(String value, Class<? extends Date> targetType,
			Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {


		 if (value == null) {
	            return null;
	        }
	       return Utils.formatDateFromString(value);
	        
	}

	@Override
	public String convertToPresentation(Date value,
			Class<? extends String> targetType, Locale locale)
			throws com.vaadin.data.util.converter.Converter.ConversionException {
		if (value == null) {
            return null;
        } else {
            return Utils.formatDateToString(value);
        }
	}

	@Override
	public Class<Date> getModelType() {
		return Date.class;
	}

	@Override
	public Class<String> getPresentationType() {
		return String.class;
	}

}
