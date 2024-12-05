package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItems;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // Entity 조회 방법
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            order.getDelivery().getAddress();
            List<OrderItems> orderItems = order.getOrderItems();
            for (OrderItems orderItem : orderItems) {
                orderItem.getItem().getName();
            }
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * oneToMany 같은 상태에서 페치 조인을 하게 되면 (필드에 컬렉션이 있는 경우)
     * 컬렉션 개수가 4개인 경우 (주문은 하나, 주문 상품은 네 개)
     * 주문 상품 개수에 따라 주문도 네 개를 불러오게 된다. (조인이 되므로 주문도 4개 행이 불러와진다. 데이터가 뻥튀기 된다.)
     * 그래서 JPQL에 distinct 를 넣어 준다.
     * 이러면 실제 DB에서도 distinct를 담은 쿼리가 날아가는데, 대신 문제점이 하나 있다. SQL에서의 디스틴트는 정말 모든 행이 똑같아야 중복 제거가 된다.
     * 따라서 반환되는 행들의 orderItem idx는 같으므로 데이터를 불러오는 과정에서 중복 제거가 되지 않는다.
     * 그러나 JPQL에서의 distinct는, 애플리케이션 레벨에서 JPA가 order의 Idx를 기준으로 중복 제거를 해 준다.
     * 이렇게 하면 원하는 결과값을 얻을 수 있다.
     * [치명적인 단점] 페이징이 불가능하다. DB에서의 결과값과 애플리케이션에서 들어가는 결과값이 다르므로 하이버네이트에서 페이징 처리를 메모리에서 한다.
     * 또한 컬렉션 페치 조인은 컬렉션이 하나일 때만 사용 가능하다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /**
     * JPA 옵션 이용
     * default_batch_fetch_size: 100
     * 컬렉션은 컬렉션 필드에 개별로 설정하려면 @BatchSize를 적용하면 된다.
     * 엔티티는 엔티티 클래스에 적용하면 된다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        // ToOne 관계는 페이징에 영향을 주지 않기 때문에 페치 조인으로 한꺼번에 가지고 온다.
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * 장점: 쿼리 한 번
     * 단점: 중복 데이터를 많이 불러서 v5보다 더 오래 걸릴 수 있다. (데이터가 많은 경우)
     * 애플리케이션 레벨에서 추가 작업이 크다.
     * 페이징이 불가능하다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Getter
    static class OrderItemDto {
        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량

        public OrderItemDto(OrderItems orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
