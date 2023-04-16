package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllByOrder_Submitter_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(Long submitterId);
    List<Offer> findAllByOrder_Submitter_IdAndOrder_IdAndCancelledIsFalseAndDeclinedIsFalseAndArchivedIsFalse(Long submitterId, Long orderId);
    List<Offer> findAllByOfferer_IdAndArchivedIsFalse(Long id);
    List<Offer> findAllByOrder_IdAndCancelledIsFalseAndDeclinedIsFalse(Long id);
    List<Offer> findAllByOfferer_IdAndOrder_IdAndCancelledIsFalse(Long offererId, Long orderId);
}
