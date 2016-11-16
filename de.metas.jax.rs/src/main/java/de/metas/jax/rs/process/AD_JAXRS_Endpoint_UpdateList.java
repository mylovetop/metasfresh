package de.metas.jax.rs.process;

import org.adempiere.util.Services;

import de.metas.jax.rs.IJaxRsBL;
import de.metas.process.Param;
import de.metas.process.Process;
import de.metas.process.SvrProcess;

/*
 * #%L
 * de.metas.jax.rs
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
/**
 * Calls {@link IJaxRsBL#updateEndPointsList(java.util.Properties, boolean)}.
 *
 * @author metas-dev <dev@metasfresh.com>
 * @task http://dewiki908/mediawiki/index.php/09848_enable_metasfresh_to_provide_jax-rs_services_%28101763395402%29
 *
 */
@Process(requiresCurrentRecordWhenCalledFromGear = false)
public class AD_JAXRS_Endpoint_UpdateList extends SvrProcess
{

	@Param(parameterName = "p_alsoSyncClasses", mandatory = true)
	private boolean p_alsoSyncClasses = false;

	final IJaxRsBL jaxRsBL = Services.get(IJaxRsBL.class);

	@Override
	protected String doIt() throws Exception
	{
		jaxRsBL.updateEndPointsList(getCtx(), p_alsoSyncClasses);
		return "@Success@";
	}

}
