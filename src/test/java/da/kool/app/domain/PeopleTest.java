package da.kool.app.domain;

import static da.kool.app.domain.PeopleTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import da.kool.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PeopleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(People.class);
        People people1 = getPeopleSample1();
        People people2 = new People();
        assertThat(people1).isNotEqualTo(people2);

        people2.setId(people1.getId());
        assertThat(people1).isEqualTo(people2);

        people2 = getPeopleSample2();
        assertThat(people1).isNotEqualTo(people2);
    }
}
