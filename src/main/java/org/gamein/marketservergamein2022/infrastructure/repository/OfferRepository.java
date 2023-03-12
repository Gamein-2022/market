package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Offer;
import org.gamein.marketservergamein2022.core.sharedkernel.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findAllByCancelled(Boolean cancelled);
    List<Offer> findAllByAccepterOrSubmitter(Team accepter, Team submitter);
}