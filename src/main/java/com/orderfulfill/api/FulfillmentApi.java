package com.orderfulfill.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

// Request and response models
record FulfillmentRequest(
        @NotEmpty(message = "items must contain at least one item")
        List<@Valid OrderItemRequest> items) {}

record OrderItemRequest(
        @NotBlank(message = "product must not be blank") String product,
        @Min(value = 1, message = "quantity must be at least 1") int quantity) {}

record FulfillmentResponse(
        boolean fulfilled,
        String message,
        List<WarehouseAllocation> allocations) {}

record WarehouseAllocation(String warehouseId, List<ItemAllocation> items) {}
record ItemAllocation(String product, int quantity) {}

// Ayush: service calls the adapter; adapter is the boundary to Hardik's engine.
interface FulfillmentEngineAdapter {
    FulfillmentResponse fulfill(FulfillmentRequest request);
}

@Component
class ExistingEngineAdapter implements FulfillmentEngineAdapter {
    
    // Simulating the database of warehouses (matching Hardik's Main.java setup)
    private final List<Order.Warehouse> allWarehouses;

    public ExistingEngineAdapter() {
        allWarehouses = new java.util.ArrayList<>();
        
        Order.Warehouse w1 = new Order.Warehouse("Noida");
        w1.inventory.put(new Order.Product("Laptop"), 5);
        
        Order.Warehouse w2 = new Order.Warehouse("Delhi");
        w2.inventory.put(new Order.Product("Mouse"), 10);
        
        Order.Warehouse w3 = new Order.Warehouse("Jaipur");
        
        allWarehouses.add(w1);
        allWarehouses.add(w2);
        allWarehouses.add(w3);
    }

    @Override
    public FulfillmentResponse fulfill(FulfillmentRequest request) {
        // 1. Map Ayush's API request to Hardik's Order object
        String newOrderId = "ORD-" + System.currentTimeMillis();
        Order.Order coreOrder = new Order.Order(newOrderId);
        
        for (OrderItemRequest reqItem : request.items()) {
            Order.Product product = new Order.Product(reqItem.product());
            coreOrder.items.add(new Order.OrderItem(product, reqItem.quantity()));
        }

        // 2. Setup Urvi's Constraints and Strategy
        List<Order.Constraint> constraints = new java.util.ArrayList<>();
        constraints.add(new Order.InventoryConstraint());
        Order.FulfillmentStrategy strategy = new Order.BalancedStrategy();

        // 3. Call the Core Engine
        Order.FulfillmentPlan plan = Order.FulfillmentEngine.selectBestPlan(
                allWarehouses, coreOrder, constraints, strategy);

        // 4. Handle failure if no combination can fulfill the order
        if (plan == null) {
            return new FulfillmentResponse(false, "Insufficient stock or no valid plan", java.util.List.of());
        }

        // 5. Execute Reservations & Map to Ayush's API Response
        List<WarehouseAllocation> allocations = new java.util.ArrayList<>();
        
        for (Order.Warehouse w : plan.assignedWarehouses) {
            List<ItemAllocation> warehouseItems = new java.util.ArrayList<>();
            
            for (Order.OrderItem item : coreOrder.items) {
                int available = w.inventory.getOrDefault(item.product, 0);
                int quantityToReserve = Math.min(item.requiredQuantity, available);
                
                if (quantityToReserve > 0) {
                    boolean success = w.reserveInventory(item.product, quantityToReserve);
                    if (success) {
                        warehouseItems.add(new ItemAllocation(item.product.productId, quantityToReserve));
                        item.requiredQuantity -= quantityToReserve; 
                    }
                }
            }
            
            if (!warehouseItems.isEmpty()) {
                allocations.add(new WarehouseAllocation(w.toString(), warehouseItems));
            }
        }

        return new FulfillmentResponse(true, "Order successfully fulfilled", allocations);
    }
}

@Service
class FulfillmentService {
    private final FulfillmentEngineAdapter adapter;

    FulfillmentService(FulfillmentEngineAdapter adapter) {
        this.adapter = adapter;
    }

    FulfillmentResponse fulfill(FulfillmentRequest request) {
        return adapter.fulfill(request);
    }
}

// Jatin's controller endpoint; keep in sync with Jatin's branch/project.
@RestController
@RequestMapping("/api")
class FulfillmentController {
    private final FulfillmentService service;

    FulfillmentController(FulfillmentService service) {
        this.service = service;
    }

    @PostMapping("/fulfill")
    ResponseEntity<FulfillmentResponse> fulfill(
            @Valid @RequestBody FulfillmentRequest request) {
        return ResponseEntity.ok(service.fulfill(request));
    }
}

// Consistent JSON errors for invalid requests and incomplete integration.
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400, "error", "Invalid request", "details", details));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    ResponseEntity<Map<String, Object>> notIntegrated(UnsupportedOperationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "status", 503, "error", "Engine not integrated", "details", ex.getMessage()));
    }
}