package da.kool.app.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OrdersTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Orders getOrdersSample1() {
        return new Orders().id(1L).userId(1L).productId(1L).quantity(1);
    }

    public static Orders getOrdersSample2() {
        return new Orders().id(2L).userId(2L).productId(2L).quantity(2);
    }

    public static Orders getOrdersRandomSampleGenerator() {
        return new Orders()
            .id(longCount.incrementAndGet())
            .userId(longCount.incrementAndGet())
            .productId(longCount.incrementAndGet())
            .quantity(intCount.incrementAndGet());
    }
}
