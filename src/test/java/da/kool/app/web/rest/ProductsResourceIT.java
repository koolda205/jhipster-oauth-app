package da.kool.app.web.rest;

import static da.kool.app.domain.ProductsAsserts.*;
import static da.kool.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import da.kool.app.IntegrationTest;
import da.kool.app.domain.Products;
import da.kool.app.repository.ProductsRepository;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProductsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProductsResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final Double DEFAULT_PRICE = 1D;
    private static final Double UPDATED_PRICE = 2D;

    private static final String ENTITY_API_URL = "/api/products";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProductsMockMvc;

    private Products products;

    private Products insertedProducts;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Products createEntity() {
        return new Products().title(DEFAULT_TITLE).category(DEFAULT_CATEGORY).price(DEFAULT_PRICE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Products createUpdatedEntity() {
        return new Products().title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);
    }

    @BeforeEach
    void initTest() {
        products = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedProducts != null) {
            productsRepository.delete(insertedProducts);
            insertedProducts = null;
        }
    }

    @Test
    @Transactional
    void createProducts() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Products
        var returnedProducts = om.readValue(
            restProductsMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(products)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Products.class
        );

        // Validate the Products in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertProductsUpdatableFieldsEquals(returnedProducts, getPersistedProducts(returnedProducts));

        insertedProducts = returnedProducts;
    }

    @Test
    @Transactional
    void createProductsWithExistingId() throws Exception {
        // Create the Products with an existing ID
        products.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(products)))
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        products.setTitle(null);

        // Create the Products, which fails.

        restProductsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(products)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        products.setPrice(null);

        // Create the Products, which fails.

        restProductsMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(products)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProducts() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        // Get all the productsList
        restProductsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(products.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE)));
    }

    @Test
    @Transactional
    void getProducts() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        // Get the products
        restProductsMockMvc
            .perform(get(ENTITY_API_URL_ID, products.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(products.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE));
    }

    @Test
    @Transactional
    void getNonExistingProducts() throws Exception {
        // Get the products
        restProductsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProducts() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the products
        Products updatedProducts = productsRepository.findById(products.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProducts are not directly saved in db
        em.detach(updatedProducts);
        updatedProducts.title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedProducts.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProductsToMatchAllProperties(updatedProducts);
    }

    @Test
    @Transactional
    void putNonExistingProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, products.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(products)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProductsWithPatch() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the products using partial update
        Products partialUpdatedProducts = new Products();
        partialUpdatedProducts.setId(products.getId());

        partialUpdatedProducts.title(UPDATED_TITLE).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProducts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductsUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProducts, products), getPersistedProducts(products));
    }

    @Test
    @Transactional
    void fullUpdateProductsWithPatch() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the products using partial update
        Products partialUpdatedProducts = new Products();
        partialUpdatedProducts.setId(products.getId());

        partialUpdatedProducts.title(UPDATED_TITLE).category(UPDATED_CATEGORY).price(UPDATED_PRICE);

        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProducts.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProducts))
            )
            .andExpect(status().isOk());

        // Validate the Products in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductsUpdatableFieldsEquals(partialUpdatedProducts, getPersistedProducts(partialUpdatedProducts));
    }

    @Test
    @Transactional
    void patchNonExistingProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, products.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(products))
            )
            .andExpect(status().isBadRequest());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProducts() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        products.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProductsMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(products)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Products in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProducts() throws Exception {
        // Initialize the database
        insertedProducts = productsRepository.saveAndFlush(products);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the products
        restProductsMockMvc
            .perform(delete(ENTITY_API_URL_ID, products.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return productsRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Products getPersistedProducts(Products products) {
        return productsRepository.findById(products.getId()).orElseThrow();
    }

    protected void assertPersistedProductsToMatchAllProperties(Products expectedProducts) {
        assertProductsAllPropertiesEquals(expectedProducts, getPersistedProducts(expectedProducts));
    }

    protected void assertPersistedProductsToMatchUpdatableProperties(Products expectedProducts) {
        assertProductsAllUpdatablePropertiesEquals(expectedProducts, getPersistedProducts(expectedProducts));
    }
}
