/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
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
