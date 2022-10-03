package hello.advanced.app.v0;

import org.springframework.stereotype.Service;

@Service
public class OrderServiceV0 {
    private final OrderRepositoryV0 orderRepository;

    public OrderServiceV0(OrderRepositoryV0 orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void orderItem(String itemId) {
        orderRepository.save(itemId);
    }
}
