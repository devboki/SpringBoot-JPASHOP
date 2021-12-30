package jpabook.jpashop.repository.order.simplequery;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
	
	private final EntityManager em;
	
	//v3과 결과는 똑같으나 원하는 것만 select 가능
	public List<OrderSimpleQueryDto> findOrderDtos() {
		return em.createQuery("select new jpabook.jpashop.repository..order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) "
								+ "from Order o "
								+ "join o.member m "
								+ "join o.delivery d", OrderSimpleQueryDto.class
							).getResultList();
	}
}
