package de.metas.ui.web.window.shared.datatype;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.metas.ui.web.json.JsonHelper;
import de.metas.ui.web.window.PropertyName;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

public class PropertyValuesDTO_JSONDeserializer extends JsonDeserializer<PropertyValuesDTO>
{

	@Override
	public PropertyValuesDTO deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		final ObjectCodec oc = p.getCodec();
		final ObjectMapper mapper = (ObjectMapper)oc;
		final JsonNode root = oc.readTree(p);

		final PropertyValuesDTO.Builder valuesBuilder = PropertyValuesDTO.builder();
		for (final JsonNode entryNode : root)
		{
			final JsonNode propertyNameNode = entryNode.get(PropertyValuesDTO_JSONSerializer.PROPERTY_PropertyName);
			final PropertyName propertyName = mapper.treeToValue(propertyNameNode, PropertyName.class);

			final String valueTypeName = extractValueTypeName(entryNode);
			final Class<?> valueType = JsonHelper.getValueTypeForName(valueTypeName);
			final Object value;
			if (valueType == null)
			{
				value = null;
			}
			else
			{
				final JsonNode valueNode = entryNode.get(PropertyValuesDTO_JSONSerializer.PROPERTY_Value);
				value = mapper.treeToValue(valueNode, valueType);
			}

			valuesBuilder.put(propertyName, value);
		}

		return valuesBuilder.build();
	}

	private final String extractValueTypeName(final JsonNode root)
	{
		final JsonNode typeNode = root.get(PropertyValuesDTO_JSONSerializer.PROPERTY_ValueType);
		if (typeNode == null)
		{
			return null;
		}
		final String typeName = typeNode.textValue();
		return typeName;
	}

}
