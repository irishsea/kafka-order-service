package sergeeva.dev.kafka_order_service.locking;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sergeeva.dev.kafka_order_service.api.product.ProductDto;
import sergeeva.dev.kafka_order_service.api.product.ProductDtoMapper;
import sergeeva.dev.kafka_order_service.api.product.ProductUpdateRequest;
import sergeeva.dev.kafka_order_service.domain.db.ProductEntity;
import sergeeva.dev.kafka_order_service.domain.service.DbProductService;

import java.time.Duration;


@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/products/lock")
public class ProductUpdateLockingController {

    private final RedisLockManager redisLockManager;
    private final DbProductService dbProductService;
    private final ProductDtoMapper mapper;

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request,
            @RequestParam(defaultValue = "500") long workMs

    ) {
        log.info("Updating product {} with locking id={}", id);
        String lockKey = "product:" + id;

        String lockId = redisLockManager.tryLock(lockKey, Duration.ofMinutes(1));
        if (lockId == null) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Is already locked, try again later: key" + lockKey);
        }

        try {
            sleep(workMs);

            ProductEntity product = dbProductService.update(id, request);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            ProductDto dto = mapper.toProductDto(product);
            log.info("Product has been updated: id={}", id);
            return ResponseEntity.ok(dto);
        } finally {
            redisLockManager.unlock(lockKey, lockId);
        }
    }

    private static void sleep(long workMs) {
        try {
            Thread.sleep(workMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
