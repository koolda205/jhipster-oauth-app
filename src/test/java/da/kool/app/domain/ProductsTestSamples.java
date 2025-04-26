package da.kool.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ProductsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Products getProductsSample1() {
        return new Products().id(1L).title("title1").category("category1");
    }

    public static Products getProductsSample2() {
        return new Products().id(2L).title("title2").category("category2");
    }

    public static Products getProductsRandomSampleGenerator() {
        return new Products().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).category(UUID.randomUUID().toString());
    }
}
