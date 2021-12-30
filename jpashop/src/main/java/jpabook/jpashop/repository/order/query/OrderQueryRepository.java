package jpabook.jpashop.repository.order.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	//v4. OrderDto를 참조하지 않는 이유 : repository와 api controller가 의존관계가 되기 때문
	public List<OrderQueryDto> findOrderQueryDtos() { 
		List<OrderQueryDto> result = findOrders(); //query 1번 -> 결과 2개(N)
		
		//orderItems 가져오기
		result.forEach(o -> {
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); //query 2번(N). N+1 문제 발생
			o.setOrderItems(orderItems);
		});
		
		return result;
	}
	
	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
				+ " from OrderItem oi"
				+ " join oi.item i"
				+ " where oi.order.id = :orderId", OrderItemQueryDto.class)
				.setParameter("orderId", orderId)
				.getResultList();
	}
	
	//v5. 성능최적화를 위한 메서드
	public List<OrderQueryDto> findAllByDto_optimization() {
		//query 1번 : member, delivery join
		List<OrderQueryDto> result = findOrders();
		
		List<Long> orderIds = result.stream()
						.map(o -> o.getOrderId())
						.collect(Collectors.toList());
		
		//query 1번 : orderitem0_.order_id in ( ? , ? )
		List<OrderItemQueryDto> orderItems = em.createQuery(
				"select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
				+ " from OrderItem oi"
				+ " join oi.item i"
				+ " where oi.order.id in :orderIds", OrderItemQueryDto.class)
				.setParameter("orderIds", orderIds)
				.getResultList();
		
		//memory에서 처리
		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
					.collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
		
		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
		
		return result;
	}

	//v6. 한방쿼리
	public List<OrderFlatDto> findAllByDto_flat() {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"
				+ " from Order o"
				+ " join o.member m"
				+ " join o.delivery d"
				+ " join o.orderItems oi"
				+ " join oi.item i", OrderFlatDto.class)
				.getResultList();
	}
	
	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
				"select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
				+ " from Order o"
				+ " join o.member m"
				+ " join o.delivery d", OrderQueryDto.class)
			.getResultList();
	}
}
