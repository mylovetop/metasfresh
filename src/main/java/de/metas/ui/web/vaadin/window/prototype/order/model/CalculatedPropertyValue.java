package de.metas.ui.web.vaadin.window.prototype.order.model;

import java.util.Set;

import de.metas.ui.web.vaadin.window.prototype.order.PropertyName;

/*
 * #%L
 * de.metas.ui.web.vaadin
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


public abstract class CalculatedPropertyValue extends ObjectPropertyValue
{
	protected CalculatedPropertyValue(final PropertyName name)
	{
		super(PropertyValueBuilder.newBuilder()
				.setPropertyName(name));
	}
	
	@Override
	public abstract Set<PropertyName> getDependsOnPropertyNames();
	
	@Override
	public final void onDependentPropertyValueChanged(final PropertyValueCollection values, final PropertyName changedPropertyName)
	{
		final Object calculatedValueNew = calculateValue(values);
		setValue(calculatedValueNew);
	}

	protected abstract Object calculateValue(final PropertyValueCollection values);
	
	@Override
	public final boolean isReadOnlyForUser()
	{
		return true;
	}
}