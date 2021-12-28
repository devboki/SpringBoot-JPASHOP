package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/* X to One 성능최적화 
 * Order -> Member
 * Order -> Delivery */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;
	
	//주문 조회 1) 문제1 무한루프! 이런 경우 양방향매핑이 되어 있는 엔티티 중 하나에 @JsonIgnore 선언해야함
	//			문제2 Type definition error : Hibernate5Module 추가 -> null 조회 됨 
	// 							-> Feature.FORCE_LAZY_LOADING, true (lazy 강제 초기화) 추가
	@GetMapping("/api/v1/simple-orders")
	public List<Order> ordersV1(){
		List<Order> all = orderRepository.findAllByString(new OrderSearch());
		for (Order order : all) {
			order.getMember().getName(); 		//lazy 강제 초기화
			order.getDelivery().getAddress();	//lazy 강제 초기화
		}
		return all;
	}
	
	//주문 조회 2) N+1 문제 발생됨. 쿼리가 총 1+N+N번 실행. order 결과가 2개면 최악의 경우 1+2+2번 실행
	@GetMapping("/api/v2/simple-orders")
	public List<SimpleOrderDto> ordersV2(){ //예제니까 list. Result data type으로 묶어야 함
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<SimpleOrderDto> result = orders.stream()
											.map(o -> new SimpleOrderDto(o))
											.collect(Collectors.toList());
		return result;
	}
	
	//주문 조회 3) fetch join 으로 쿼리 1번 실행 .findAllWithMemberDelivery() 재사용성 높음
	@GetMapping("/api/v3/simple-orders")
	public List<SimpleOrderDto> ordersV3(){
		List<Order> orders = orderRepository.findAllWithMemberDelivery();
		List<SimpleOrderDto> result = orders.stream()
											.map(o -> new SimpleOrderDto(o))
											.collect(Collectors.toList());
		return result;
	}

	//주문 조회 4) select new ... 사용해서 JPQL의 결과를 DTO로 즉시 변환
	//			v3 결과와 join 부분은 같으나 select 절에서 원하는 것만 선택 가능. 리포지토리 재사용성이 떨어짐.
	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDto> ordersV4(){
		return orderRepository.findOrderDtos();
	}
	
	@Data
	static class SimpleOrderDto {
		
		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		
		public SimpleOrderDto(Order order) {
			 orderId = order.getId();
			 name = order.getMember().getName(); //lazy 초기화
			 orderDate = order.getOrderDate();
			 orderStatus = order.getStatus();
			 address = order.getDelivery().getAddress(); //lazy 초기화
		}
	
	}
}
