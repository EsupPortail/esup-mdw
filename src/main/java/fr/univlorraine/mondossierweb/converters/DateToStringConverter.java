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

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import fr.univlorraine.mondossierweb.utils.Utils;

import java.util.Date;

/**
 * Convertisseur String (dd/MM/yyyy) ↔ {@link Date} pour les Binder Vaadin Flow.
 */
@SuppressWarnings("serial")
public class DateToStringConverter implements Converter<String, Date> {

	@Override
	public Result<Date> convertToModel(String value, ValueContext context) {
		if (value == null || value.isEmpty()) {
			return Result.ok(null);
		}
		Date date = Utils.formatDateFromString(value);
		if (date == null) {
			return Result.error("Date invalide : " + value);
		}
		return Result.ok(date);
	}

	@Override
	public String convertToPresentation(Date value, ValueContext context) {
		if (value == null) {
			return null;
		}
		return Utils.formatDateToString(value);
	}

}
