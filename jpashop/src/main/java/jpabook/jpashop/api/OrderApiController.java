package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/* X to Many 컬렉션 조회 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;
	private final OrderQueryRepository orderQueryRepository;
	
	//엔티티 직접 노출한 경우. 양방향 관계는 꼭 @JsonIgnore
	@GetMapping("/api/v1/orders")
	public List<Order> ordersV1(){
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName();
			order.getDelivery().getAddress();
			
			List<OrderItem> orderItems = order.getOrderItems();
			orderItems.stream().forEach(o -> o.getItem().getName());
		}
		return all;
	}
	
	//엔티티를 DTO로 변환 : 쿼리가 너무 많이 나감
	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2(){
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return result;
	}
	
	//DTO + fetch join : data 중복 -> select distinct o from Order o
	//v2, v3 코드 동일한데 한방 쿼리로 성능최적화. 
	//단점 : 페이징 불가능. 중복된 data를 전부 애플리케이션으로 보냄.
	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithItem();
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return result;
	}
	
	//DTO + fetch join + paging : 쿼리 수 1+N+M -> 1+1+1
	//yml '[default_batch_fetch_size]': 100 or @OneToMany에 @BatchSize(size = 1000) 개별최적화
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
			@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "limit", defaultValue = "100") int limit) {
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); //xToOne fetch join
		List<OrderDto> result = orders.stream()
				.map(o -> new OrderDto(o))
				.collect(Collectors.toList());
		return result;
	}
	
	//DTO 직접 조회 : N+1 문제 발생
	@GetMapping("/api/v4/orders")
	public List<OrderQueryDto> ordersV4() {
		return orderQueryRepository.findOrderQueryDtos();
	}
			
	//DTO 직접 조회 : 성능최적화
	@GetMapping("/api/v5/orders")
	public List<OrderQueryDto> ordersV5() {
		return orderQueryRepository.findAllByDto_optimization();
	}
	
	//DTO 직접 조회 : 한방쿼리
	//단점 : 조인으로 인해 DB에서 중복 데이터 추가됨. 상황에 따라 v5 보다 더 느릴 수도. 페이징 불가능
	@GetMapping("/api/v6/orders")
	public List<OrderQueryDto> ordersV6() {
		//return orderQueryRepository.findAllByDto_flat(); 페이징이 불가능한 중복 데이터들 조회
		
		//중복 걸러내기
		List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
		
		//FlatDto -> OrderQueryDto / OrderItemQueryDto 발라내서 OrderQueryDto 로 바꾸기
		return flats.stream()
				.collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
						 mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())))
				.entrySet()
				.stream().map(e -> new OrderQueryDto(e.getKey().getOrderId(),
													e.getKey().getName(), 
													e.getKey().getOrderDate(), 
													e.getKey().getOrderStatus(),
													e.getKey().getAddress(), 
													e.getValue()))
				.collect(toList());
	}
	
	@Getter
	static class OrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems; //DTO를 반환할 때 엔티티가 있으면 안 됨. orderItems도 DTO로
		
		public OrderDto(Order order) {
			orderId = order.getId();
			name = order.getMember().getName();
			orderDate = order.getOrderDate();
			orderStatus = order.getStatus();
			address = order.getDelivery().getAddress();
			//1. orderItems = order.getOrderItems(); //엔티티이기 때문에 null로 조회됨.
			//2. order.getOrderItems().stream().forEach(o -> o.getItem().getName()); //이런 방식으로 조회는 할 수 있으나 엔티티 자체를 노출하면 X
			orderItems = order.getOrderItems().stream()
				.map(orderItem -> new OrderItemDto(orderItem)) //.map(OrderDto::new)
				.collect(Collectors.toList());
		}	
	}
	
	@Getter
	static class OrderItemDto {

		//필요한 엔티티만 조회하도록
		private String itemName;
		private int orderPrice;
		private int count;
		
		public OrderItemDto(OrderItem orderItem) {
			itemName = orderItem.getItem().getName();
			orderPrice = orderItem.getOrderPrice();
			count = orderItem.getCount();
		}
		
	}
}
