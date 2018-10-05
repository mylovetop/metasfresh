package de.metas.ui.web.picking.pickingslot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.warehouse.WarehouseId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

import de.metas.handlingunits.HuId;
import de.metas.handlingunits.model.I_M_ShipmentSchedule;
import de.metas.handlingunits.picking.IHUPickingSlotDAO;
import de.metas.handlingunits.picking.PickingCandidate;
import de.metas.handlingunits.picking.PickingCandidateRepository;
import de.metas.handlingunits.picking.PickingCandidateStatus;
import de.metas.handlingunits.picking.PickingCandidatesQuery;
import de.metas.handlingunits.reservation.HUReservationService;
import de.metas.handlingunits.sourcehu.SourceHUsService;
import de.metas.handlingunits.sourcehu.SourceHUsService.MatchingSourceHusQuery;
import de.metas.handlingunits.sourcehu.SourceHUsService.MatchingSourceHusQuery.MatchingSourceHusQueryBuilder;
import de.metas.inoutcandidate.api.IShipmentScheduleEffectiveBL;
import de.metas.inoutcandidate.api.IShipmentSchedulePA;
import de.metas.inoutcandidate.api.ShipmentScheduleId;
import de.metas.picking.api.PickingSlotId;
import de.metas.picking.model.I_M_PickingSlot;
import de.metas.product.ProductId;
import de.metas.ui.web.handlingunits.DefaultHUEditorViewFactory;
import de.metas.ui.web.handlingunits.HUEditorRow;
import de.metas.ui.web.handlingunits.HUEditorRowAttributesProvider;
import de.metas.ui.web.handlingunits.HUEditorRowFilter;
import de.metas.ui.web.handlingunits.HUEditorViewRepository;
import de.metas.ui.web.handlingunits.SqlHUEditorViewRepository;
import de.metas.ui.web.picking.PickingConstants;
import de.metas.util.GuavaCollectors;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
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

/**
 * This class is used by {@link PickingSlotViewRepository} and provides the HUs that are related to picking.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@Service
public class PickingHURowsRepository
{
	private final HUEditorViewRepository huEditorRepo;
	private final PickingCandidateRepository pickingCandidatesRepo;

	@Autowired
	public PickingHURowsRepository(
			@NonNull final DefaultHUEditorViewFactory huEditorViewFactory,
			@NonNull final PickingCandidateRepository pickingCandidatesRepo,
			@NonNull final HUReservationService huReservationService)
	{
		this(
				createDefaultHUEditorViewRepository(huEditorViewFactory, huReservationService),
				pickingCandidatesRepo);
	}

	private static SqlHUEditorViewRepository createDefaultHUEditorViewRepository(
			@NonNull final DefaultHUEditorViewFactory huEditorViewFactory,
			@NonNull final HUReservationService huReservationService)
	{
		return SqlHUEditorViewRepository.builder()
				.windowId(PickingConstants.WINDOWID_PickingSlotView)
				.attributesProvider(HUEditorRowAttributesProvider.builder().readonly(true).build())
				.sqlViewBinding(huEditorViewFactory.getSqlViewBinding())
				.huReservationService(huReservationService)
				.build();
	}

	/**
	 * Creates an instance using the given {@code huEditorRepo}. Intended for testing.
	 *
	 * @param huEditorRepo
	 */
	@VisibleForTesting
	PickingHURowsRepository(
			@NonNull final HUEditorViewRepository huEditorRepo,
			@NonNull final PickingCandidateRepository pickingCandidatesRepo)
	{
		this.huEditorRepo = huEditorRepo;
		this.pickingCandidatesRepo = pickingCandidatesRepo;
	}

	/**
	 * Retrieve the union of all HUs that match any one of the given shipment schedule IDs and that are flagged to be fine picking source HUs.
	 *
	 * @param shipmentScheduleIds
	 * @return
	 */
	public List<HUEditorRow> retrieveSourceHUs(@NonNull final PickingSlotRepoQuery query)
	{
		final MatchingSourceHusQuery matchingSourceHUsQuery = createMatchingSourceHusQuery(query);
		final Set<HuId> sourceHUIds = SourceHUsService.get().retrieveMatchingSourceHUIds(matchingSourceHUsQuery);
		return huEditorRepo.retrieveHUEditorRows(sourceHUIds, HUEditorRowFilter.ALL);
	}

	@VisibleForTesting
	static MatchingSourceHusQuery createMatchingSourceHusQuery(@NonNull final PickingSlotRepoQuery query)
	{
		final IShipmentSchedulePA shipmentSchedulesRepo = Services.get(IShipmentSchedulePA.class);
		
		final I_M_ShipmentSchedule currentShipmentSchedule = query.getCurrentShipmentScheduleId() != null
				? shipmentSchedulesRepo.getById(query.getCurrentShipmentScheduleId(), I_M_ShipmentSchedule.class)
				: null;
				
		final Set<ProductId> productIds;
		final Set<ShipmentScheduleId> allShipmentScheduleIds = query.getShipmentScheduleIds();
		if (allShipmentScheduleIds.isEmpty())
		{
			productIds = ImmutableSet.of();
		}
		else if (allShipmentScheduleIds.size() == 1)
		{
			productIds = ImmutableSet.of(ProductId.ofRepoId(currentShipmentSchedule.getM_Product_ID()));
		}
		else
		{
			productIds = shipmentSchedulesRepo.getProductIdsByShipmentScheduleIds(allShipmentScheduleIds);
		}

		final MatchingSourceHusQueryBuilder builder = MatchingSourceHusQuery.builder()
				.productIds(productIds);

		final IShipmentScheduleEffectiveBL shipmentScheduleEffectiveBL = Services.get(IShipmentScheduleEffectiveBL.class);
		final WarehouseId effectiveWarehouseId = currentShipmentSchedule != null ? shipmentScheduleEffectiveBL.getWarehouseId(currentShipmentSchedule) : null;
		builder.warehouseId(effectiveWarehouseId);
		return builder.build();
	}

	/**
	 *
	 * @param pickingCandidatesQuery determines which {@code M_ShipmentSchedule_ID}s this is about,<br>
	 *            and also (optionally) if the returned rows shall have picking candidates with a certain status.
	 *
	 * @return a multi-map where the keys are {@code M_PickingSlot_ID}s and the value is a list of HUEditorRows which also contain with the respective {@code M_Picking_Candidate}s' {@code processed} states.
	 */
	public ListMultimap<PickingSlotId, PickedHUEditorRow> retrievePickedHUsIndexedByPickingSlotId(@NonNull final PickingCandidatesQuery pickingCandidatesQuery)
	{
		final List<PickingCandidate> pickingCandidates = pickingCandidatesRepo.query(pickingCandidatesQuery);
		return retrievePickedHUsIndexedByPickingSlotId(pickingCandidates);
	}

	private ListMultimap<PickingSlotId, PickedHUEditorRow> retrievePickedHUsIndexedByPickingSlotId(@NonNull final List<PickingCandidate> pickingCandidates)
	{
		final Map<HuId, PickedHUEditorRow> huId2huRow = new HashMap<>();

		final Builder<PickingSlotId, PickedHUEditorRow> builder = ImmutableListMultimap.builder();

		for (final PickingCandidate pickingCandidate : pickingCandidates)
		{
			final HuId huId = pickingCandidate.getHuId();
			if (huId2huRow.containsKey(huId))
			{
				continue;
			}

			final HUEditorRow huEditorRow = huEditorRepo.retrieveForHUId(huId);
			final boolean pickingCandidateProcessed = isPickingCandidateProcessed(pickingCandidate);
			final PickedHUEditorRow row = new PickedHUEditorRow(huEditorRow, pickingCandidateProcessed);

			huId2huRow.put(huId, row);

			final PickingSlotId pickingSlotId = pickingCandidate.getPickingSlotId();
			builder.put(pickingSlotId, row);
		}

		return builder.build();
	}

	private static boolean isPickingCandidateProcessed(@NonNull final PickingCandidate pc)
	{
		final PickingCandidateStatus status = pc.getStatus();
		if (PickingCandidateStatus.Closed.equals(status))
		{
			return true;
		}
		else if (PickingCandidateStatus.Processed.equals(status))
		{
			return true;
		}
		else if (PickingCandidateStatus.Draft.equals(status))
		{
			return false;
		}
		else
		{
			throw new AdempiereException("Unexpected M_Picking_Candidate.Status=" + status).setParameter("pc", pc);
		}
	}

	public ListMultimap<PickingSlotId, PickedHUEditorRow> //
			retrieveAllPickedHUsIndexedByPickingSlotId(@NonNull final List<I_M_PickingSlot> pickingSlots)
	{
		final SetMultimap<PickingSlotId, HuId> //
		huIdsByPickingSlotId = Services.get(IHUPickingSlotDAO.class).retrieveAllHUIdsIndexedByPickingSlotId(pickingSlots);

		huEditorRepo.warmUp(ImmutableSet.copyOf(huIdsByPickingSlotId.values()));

		return huIdsByPickingSlotId
				.entries()
				.stream()
				.map(pickingSlotAndHU -> {
					final PickingSlotId pickingSlotId = pickingSlotAndHU.getKey();
					final HuId huId = pickingSlotAndHU.getValue();

					final HUEditorRow huEditorRow = huEditorRepo.retrieveForHUId(huId);
					final boolean pickingCandidateProcessed = true;
					final PickedHUEditorRow row = new PickedHUEditorRow(huEditorRow, pickingCandidateProcessed);

					return GuavaCollectors.entry(pickingSlotId, row);
				})
				.collect(GuavaCollectors.toImmutableListMultimap());
	}

	/**
	 * Immutable pojo that contains the HU editor as retrieved from {@link HUEditorViewRepository} plus the the {@code processed} value from the respective picking candidate.
	 *
	 * @author metas-dev <dev@metasfresh.com>
	 *
	 */
	// the fully qualified annotations are a workaround for a javac problem with maven
	@lombok.Value
	@lombok.AllArgsConstructor
	static class PickedHUEditorRow
	{
		HUEditorRow huEditorRow;
		boolean processed;
	}
}
