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
import lombok.Singular;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Value
public class SyncContract implements ISyncModel
{
	String uuid;
	boolean deleted;
	long syncConfirmationId;

	Date dateFrom;
	Date dateTo;
	List<SyncContractLine> contractLines;
	String rfq_uuid; // optional

	@Builder(toBuilder = true)
	@JsonCreator
	public SyncContract(
			@JsonProperty("uuid") final String uuid,
			@JsonProperty("deleted") final boolean deleted,
			@JsonProperty("syncConfirmationId") final long syncConfirmationId,
			@JsonProperty("dateFrom") final Date dateFrom,
			@JsonProperty("dateTo") final Date dateTo,
			@JsonProperty("contractLines") @Singular final List<SyncContractLine> contractLines,
			@JsonProperty("rfq_uuid") final String rfq_uuid)
	{
		this.uuid = uuid;
		this.deleted = deleted;
		this.syncConfirmationId = syncConfirmationId;

		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.contractLines = contractLines;
		this.rfq_uuid = rfq_uuid;
	}

	@Override
	public String toString()
	{
		return "SyncContract ["
				+ "dateFrom=" + dateFrom + ", dateTo=" + dateTo
				+ ", rfq_uuid=" + rfq_uuid
				+ ", contractLines=" + contractLines + "]";
	}

	@Override
	public ISyncModel withNotDeleted()
	{
		return toBuilder().deleted(false).build();
	}
}
