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


