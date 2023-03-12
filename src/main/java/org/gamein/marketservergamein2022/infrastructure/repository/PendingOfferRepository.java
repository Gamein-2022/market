package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.PendingOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PendingOfferRepository extends JpaRepository<PendingOffer, Long> {
    List<PendingOffer> findAllByOffer_Submitter_Id(Long submitterId);
}
