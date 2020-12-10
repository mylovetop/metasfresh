/*
 * #%L
 * de-metas-common-procurement
 * %%
 * Copyright (C) 2020 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.common.procurement.sync.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SyncBPartnersRequest extends ProcurementEvent
{
	public static final SyncBPartnersRequest of(@NonNull final SyncBPartner syncBPartner)
	{
		return SyncBPartnersRequest.builder().bpartner(syncBPartner).build();
	}

	public static final SyncBPartnersRequest of(@NonNull final List<SyncBPartner> syncBPartners)
	{
		return SyncBPartnersRequest.builder().bpartners(syncBPartners).build();
	}

	@Singular
	List<SyncBPartner> bpartners;

	@JsonCreator
	private SyncBPartnersRequest(@JsonProperty("bpartners") final List<SyncBPartner> bpartners)
	{
		this.bpartners = bpartners;
	}

	@Override
	public String toString()
	{
		return "SyncBPartnersRequest [bpartners=" + bpartners + "]";
	}
}
