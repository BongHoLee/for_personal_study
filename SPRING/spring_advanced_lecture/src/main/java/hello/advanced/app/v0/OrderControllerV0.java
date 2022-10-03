package hello.advanced.app.v0;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderControllerV0 {

    private final OrderServiceV0 orderService;

    public OrderControllerV0(OrderServiceV0 orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/v0/request")
    public String request(String itemId) {
        orderService.orderItem(itemId);
        return "ok";
    }
}
