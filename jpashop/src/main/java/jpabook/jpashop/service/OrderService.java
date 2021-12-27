package jpabook.jpashop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
	
	private final OrderRepository orderRepository;
	private final MemberRepository memberRepository;
	private final ItemRepository itemRepository;

	/* 주문 */
	@Transactional
	public Long order(Long memberId, Long itemId, int count) {
		
		//엔티티 조회
		Member member = memberRepository.findOne(memberId);
		Item item = itemRepository.findOne(itemId);
		
		//배송정보 생성
		Delivery delivery = new Delivery();
		delivery.setAddress(member.getAddress());
		
		//주문상품 생성
		OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
		/* OrderItem orderItem1 = new OrderItem(); 직접 생성 사용을 막기 위해서 
		 * OrderItem.java -> protected OrderItem() {} or
		 * @NoArgsConstructor(access = AccessLevel.PROTECTED) 선언*/
		
		//주문 생성
		Order order = Order.createOrder(member, delivery, orderItem);
		/* new Order(); 직접 생성 사용을 막기 위해 Order.java
		 * @NoArgsConstructor(access = AccessLevel.PROTECTED) 선언 */
		
		//주문 저장
		orderRepository.save(order);
		
		return order.getIdLong();
	}
	
	
	/* 취소 */
	@Transactional
	public void cancelOrder(Long orderId) {
		//주문 엔티티 조회
		Order order = orderRepository.findOne(orderId);
		//주문 취소
		order.cancel();
	}
	
	
	/* 검색 */
	public List<Order> findOrders(OrderSearch orderSearch){ 
		return orderRepository.findAllByString(orderSearch);
	}
	
	
}
