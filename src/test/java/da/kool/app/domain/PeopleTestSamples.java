package da.kool.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PeopleTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static People getPeopleSample1() {
        return new People().id(1L).address("address1").email("email1").password("password1").name("name1");
    }

    public static People getPeopleSample2() {
        return new People().id(2L).address("address2").email("email2").password("password2").name("name2");
    }

    public static People getPeopleRandomSampleGenerator() {
        return new People()
            .id(longCount.incrementAndGet())
            .address(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .password(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString());
    }
}
