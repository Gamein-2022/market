package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllByOrder_Submitter_Id(Long submitterId);
    List<Offer> findAllByOrder_Submitter_IdAndOrder_Id(Long submitterId, Long orderId);
    List<Offer> findAllByOfferer_Id(Long id);
    List<Offer> findAllByOrder_Id(Long id);
    List<Offer> findAllByOfferer_IdAndOrder_IdAndDeclined(Long offererId, Long orderId, Boolean declined);
}
