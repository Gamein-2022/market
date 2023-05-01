package org.gamein.marketservergamein2022.infrastructure.repository;

import org.gamein.marketservergamein2022.core.sharedkernel.entity.Order;
import org.gamein.marketservergamein2022.core.sharedkernel.enums.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


@Repository
public class OrderRepoCustomImpl implements OrderRepoCustom {
    @Autowired
    private EntityManager em;
    @Override
    public List<Order> allOrders(OrderType type, Long productId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> orderRoot = cq.from(Order.class);
        cq.where(
                cb.equal(orderRoot.get("cancelled"), false)
        );
        cq.where(
                cb.equal(orderRoot.get("archived"), false)
        );
        cq.where(
                cb.isNull(orderRoot.get("acceptDate"))
        );
        if (productId != null) {
            Predicate productPredicate = cb.equal(orderRoot.get("product").get("id"), productId);
            cq.where(productPredicate);
        }
        if (type != null) {
            Predicate typePredicate = cb.equal(orderRoot.get("type"), type);
            cq.where(typePredicate);

            if (type == OrderType.SELL) {
                cq.orderBy(cb.asc(orderRoot.get("unitPrice")));
            } else {
                cq.orderBy(cb.desc(orderRoot.get("unitPrice")));
            }
        }

        TypedQuery<Order> query = em.createQuery(cq);
        return query.getResultList();
    }
}
