package jpabook.jpashop.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 총 2주문 2개
 * userA
 * JPA1 BOOK
 * JPA2 BOOK
 * userB
 * SPRING1 BOOK
 * SPRING2 book
 */
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1(){
            Member member = new Member();
            member.setName("userA");
            member.setAddress(new Address("서울", "1", "1111"));
            em.persist(member);

            Book book1 = getBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            // shift + F6 변수명 스크롤 같이 수정
            Book book2 = getBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItems orderItem1 = OrderItems.createOrderItem(book1, 10000, 1);
            OrderItems orderItem2 = OrderItems.createOrderItem(book2, 10000, 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2(){
            Member member = new Member();
            member.setName("userB");
            member.setAddress(new Address("부산", "1", "2222"));
            em.persist(member);

            Book book1 = getBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);

            // shift + F6 변수명 스크롤 같이 수정
            // opt + command + m 하면 메소드 뽑아냄
            Book book2 = getBook("SPRING2 BOOK", 40000, 300);
            em.persist(book2);

            OrderItems orderItem1 = OrderItems.createOrderItem(book1, 20000, 3);
            OrderItems orderItem2 = OrderItems.createOrderItem(book2, 40000, 4);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }
    }

    private static Book getBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        return book;
    }
}