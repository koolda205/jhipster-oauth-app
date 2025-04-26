package da.kool.app.web.rest;

import static da.kool.app.domain.OrdersAsserts.*;
import static da.kool.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import da.kool.app.IntegrationTest;
import da.kool.app.domain.Orders;
import da.kool.app.repository.OrdersRepository;
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
 * Integration tests for the {@link OrdersResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OrdersResourceIT {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Long UPDATED_USER_ID = 2L;

    private static final Long DEFAULT_PRODUCT_ID = 1L;
    private static final Long UPDATED_PRODUCT_ID = 2L;

    private static final Double DEFAULT_TOTAL = 1D;
    private static final Double UPDATED_TOTAL = 2D;

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final String ENTITY_API_URL = "/api/orders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOrdersMockMvc;

    private Orders orders;

    private Orders insertedOrders;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Orders createEntity() {
        return new Orders().userId(DEFAULT_USER_ID).productId(DEFAULT_PRODUCT_ID).total(DEFAULT_TOTAL).quantity(DEFAULT_QUANTITY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Orders createUpdatedEntity() {
        return new Orders().userId(UPDATED_USER_ID).productId(UPDATED_PRODUCT_ID).total(UPDATED_TOTAL).quantity(UPDATED_QUANTITY);
    }

    @BeforeEach
    void initTest() {
        orders = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedOrders != null) {
            ordersRepository.delete(insertedOrders);
            insertedOrders = null;
        }
    }

    @Test
    @Transactional
    void createOrders() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Orders
        var returnedOrders = om.readValue(
            restOrdersMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Orders.class
        );

        // Validate the Orders in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertOrdersUpdatableFieldsEquals(returnedOrders, getPersistedOrders(returnedOrders));

        insertedOrders = returnedOrders;
    }

    @Test
    @Transactional
    void createOrdersWithExistingId() throws Exception {
        // Create the Orders with an existing ID
        orders.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOrdersMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isBadRequest());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        orders.setUserId(null);

        // Create the Orders, which fails.

        restOrdersMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkProductIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        orders.setProductId(null);

        // Create the Orders, which fails.

        restOrdersMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        orders.setTotal(null);

        // Create the Orders, which fails.

        restOrdersMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        orders.setQuantity(null);

        // Create the Orders, which fails.

        restOrdersMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOrders() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        // Get all the ordersList
        restOrdersMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(orders.getId().intValue())))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID.intValue())))
            .andExpect(jsonPath("$.[*].productId").value(hasItem(DEFAULT_PRODUCT_ID.intValue())))
            .andExpect(jsonPath("$.[*].total").value(hasItem(DEFAULT_TOTAL)))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)));
    }

    @Test
    @Transactional
    void getOrders() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        // Get the orders
        restOrdersMockMvc
            .perform(get(ENTITY_API_URL_ID, orders.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(orders.getId().intValue()))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID.intValue()))
            .andExpect(jsonPath("$.productId").value(DEFAULT_PRODUCT_ID.intValue()))
            .andExpect(jsonPath("$.total").value(DEFAULT_TOTAL))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY));
    }

    @Test
    @Transactional
    void getNonExistingOrders() throws Exception {
        // Get the orders
        restOrdersMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOrders() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orders
        Orders updatedOrders = ordersRepository.findById(orders.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOrders are not directly saved in db
        em.detach(updatedOrders);
        updatedOrders.userId(UPDATED_USER_ID).productId(UPDATED_PRODUCT_ID).total(UPDATED_TOTAL).quantity(UPDATED_QUANTITY);

        restOrdersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedOrders.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedOrders))
            )
            .andExpect(status().isOk());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOrdersToMatchAllProperties(updatedOrders);
    }

    @Test
    @Transactional
    void putNonExistingOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, orders.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orders))
            )
            .andExpect(status().isBadRequest());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(orders))
            )
            .andExpect(status().isBadRequest());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(orders)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOrdersWithPatch() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orders using partial update
        Orders partialUpdatedOrders = new Orders();
        partialUpdatedOrders.setId(orders.getId());

        partialUpdatedOrders.userId(UPDATED_USER_ID).productId(UPDATED_PRODUCT_ID);

        restOrdersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrders.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrders))
            )
            .andExpect(status().isOk());

        // Validate the Orders in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrdersUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedOrders, orders), getPersistedOrders(orders));
    }

    @Test
    @Transactional
    void fullUpdateOrdersWithPatch() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the orders using partial update
        Orders partialUpdatedOrders = new Orders();
        partialUpdatedOrders.setId(orders.getId());

        partialUpdatedOrders.userId(UPDATED_USER_ID).productId(UPDATED_PRODUCT_ID).total(UPDATED_TOTAL).quantity(UPDATED_QUANTITY);

        restOrdersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOrders.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOrders))
            )
            .andExpect(status().isOk());

        // Validate the Orders in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOrdersUpdatableFieldsEquals(partialUpdatedOrders, getPersistedOrders(partialUpdatedOrders));
    }

    @Test
    @Transactional
    void patchNonExistingOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, orders.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orders))
            )
            .andExpect(status().isBadRequest());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(orders))
            )
            .andExpect(status().isBadRequest());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOrders() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        orders.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOrdersMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(orders)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Orders in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOrders() throws Exception {
        // Initialize the database
        insertedOrders = ordersRepository.saveAndFlush(orders);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the orders
        restOrdersMockMvc
            .perform(delete(ENTITY_API_URL_ID, orders.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return ordersRepository.count();
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

    protected Orders getPersistedOrders(Orders orders) {
        return ordersRepository.findById(orders.getId()).orElseThrow();
    }

    protected void assertPersistedOrdersToMatchAllProperties(Orders expectedOrders) {
        assertOrdersAllPropertiesEquals(expectedOrders, getPersistedOrders(expectedOrders));
    }

    protected void assertPersistedOrdersToMatchUpdatableProperties(Orders expectedOrders) {
        assertOrdersAllUpdatablePropertiesEquals(expectedOrders, getPersistedOrders(expectedOrders));
    }
}
