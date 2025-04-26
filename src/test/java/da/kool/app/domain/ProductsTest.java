package da.kool.app.domain;

import static da.kool.app.domain.ProductsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import da.kool.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProductsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Products.class);
        Products products1 = getProductsSample1();
        Products products2 = new Products();
        assertThat(products1).isNotEqualTo(products2);

        products2.setId(products1.getId());
        assertThat(products1).isEqualTo(products2);

        products2 = getProductsSample2();
        assertThat(products1).isNotEqualTo(products2);
    }
}
