package jpabook.jpashop.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

	private final EntityManager em;
	
	public void save(Order order) {
		em.persist(order);
	}
	
	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}
	
	
	/* Criteria */
	public List<Order> findAllByCriteria(OrderSearch orderSearch) {
		 
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> o = cq.from(Order.class);
		Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
		List<Predicate> criteria = new ArrayList<>();
		
		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			Predicate status = cb.equal(o.get("status"),
			orderSearch.getOrderStatus());
			criteria.add(status);
		}
		
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			Predicate name =
			cb.like(m.<String>get("name"), "%" +
			orderSearch.getMemberName() + "%");
			criteria.add(name);
		}
		
		cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
		
		return query.getResultList();
	}

	
	/* JPQL */
	public List<Order> findAllByString(OrderSearch orderSearch) {
		
		String jpql = "select o From Order o join o.member m";
		 
		boolean isFirstCondition = true;
		
		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			
		 jpql += " o.status = :status";
		}
		 
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			
			jpql += " m.name like :name";
		 }
		 
		TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
			
		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
			}
		
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
			}
		
		return query.getResultList();
		}

	//fetch join
	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery("select o from Order o"
								+ " join fetch o.member m"
								+ " join fetch o.delivery d", Order.class
							).getResultList();
	}
	
	//xToOne join + paging
	//yml '[default_batch_fetch_size]': 100
	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		return em.createQuery("select o from Order o"
				+ " join fetch o.member m"
				+ " join fetch o.delivery d", Order.class
			).setFirstResult(offset)
			 .setMaxResults(limit)
			 .getResultList();
	}
	
	//DTO : fetch join. 
	//distinct로 중복제거 가능하지만 페이징 불가능. 필요한 order 기준으로 페이징 X, data 뻥튀기 된 item 기준으로 페이징이 되므로 hibernate WARN 발생
	public List<Order> findAllWithItem() {
		return em.createQuery(
						"select distinct o from Order o"
						+ " join fetch o.member m"
						+ " join fetch o.delivery d"
						+ " join fetch o.orderItems oi"
						+ " join fetch oi.item i", Order.class)
			  //.setFirstResult(1)
			  //.setMaxResults(100)
				.getResultList();
	}
}
