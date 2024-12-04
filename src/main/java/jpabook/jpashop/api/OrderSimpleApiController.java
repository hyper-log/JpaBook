package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * XToOne
 * Order
 * Order -> Member (ManyToOne)
 * Order -> Delivery (OneToOne)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }


    @GetMapping("/api/v2/simple-orders")
    public List<OrderSimpleQueryDto> orderV2() {
        // Order 2개
        // N + 1 -> 1 + 회원 N + 배송 N (order 개수를 N에 넣으면 총 쿼리 개수를 조회할 수 있는데 끔찍함)
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // 결과가 두 번 돌기 시작함
        List<OrderSimpleQueryDto> result = orders.stream()
                .map(o -> new OrderSimpleQueryDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3/simple-orders")
    public List<OrderSimpleQueryDto> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<OrderSimpleQueryDto> result = orders.stream()
                .map(o -> new OrderSimpleQueryDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 원하는 데이터만 가지고 올 수 있는 장점이 있지만, 재사용성이 낮다.
     * 생각보다 성능 최적화가 미비하다.
     * 셀렉트 절에서 필드가 몇 개 추가된다고 해서 성능 최적화가 크진 않다 | 필드가 몇십 개가 넘어가면 고민, 혹은 트래픽이 넘치는 경우
     * API 스펙에 맞춘 코드가 레파지토리에 들어가는 것 자체가 단점이다.
     * 복잡한 통계 쿼리 등 API 스펙을 맞추어서 보내 주는 것이 성능상 뛰어난 경우가 있다.
     * 화면 전용 쿼리 레파지토리를 따로 파서 구분해 두는 게 낫다. (repository -> order -> simpleQuery)
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }
}
