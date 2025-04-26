package da.kool.app.web.rest;

import static da.kool.app.domain.PeopleAsserts.*;
import static da.kool.app.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import da.kool.app.IntegrationTest;
import da.kool.app.domain.People;
import da.kool.app.repository.PeopleRepository;
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
 * Integration tests for the {@link PeopleResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PeopleResourceIT {

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/people";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPeopleMockMvc;

    private People people;

    private People insertedPeople;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static People createEntity() {
        return new People().address(DEFAULT_ADDRESS).email(DEFAULT_EMAIL).password(DEFAULT_PASSWORD).name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static People createUpdatedEntity() {
        return new People().address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        people = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPeople != null) {
            peopleRepository.delete(insertedPeople);
            insertedPeople = null;
        }
    }

    @Test
    @Transactional
    void createPeople() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the People
        var returnedPeople = om.readValue(
            restPeopleMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            People.class
        );

        // Validate the People in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPeopleUpdatableFieldsEquals(returnedPeople, getPersistedPeople(returnedPeople));

        insertedPeople = returnedPeople;
    }

    @Test
    @Transactional
    void createPeopleWithExistingId() throws Exception {
        // Create the People with an existing ID
        people.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPeopleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        people.setEmail(null);

        // Create the People, which fails.

        restPeopleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPasswordIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        people.setPassword(null);

        // Create the People, which fails.

        restPeopleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        people.setName(null);

        // Create the People, which fails.

        restPeopleMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPeople() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        // Get all the peopleList
        restPeopleMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(people.getId().intValue())))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].password").value(hasItem(DEFAULT_PASSWORD)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getPeople() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        // Get the people
        restPeopleMockMvc
            .perform(get(ENTITY_API_URL_ID, people.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(people.getId().intValue()))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.password").value(DEFAULT_PASSWORD))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingPeople() throws Exception {
        // Get the people
        restPeopleMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPeople() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the people
        People updatedPeople = peopleRepository.findById(people.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPeople are not directly saved in db
        em.detach(updatedPeople);
        updatedPeople.address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);

        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPeople.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPeopleToMatchAllProperties(updatedPeople);
    }

    @Test
    @Transactional
    void putNonExistingPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, people.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(people)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePeopleWithPatch() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the people using partial update
        People partialUpdatedPeople = new People();
        partialUpdatedPeople.setId(people.getId());

        partialUpdatedPeople.password(UPDATED_PASSWORD).name(UPDATED_NAME);

        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPeople.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPeopleUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPeople, people), getPersistedPeople(people));
    }

    @Test
    @Transactional
    void fullUpdatePeopleWithPatch() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the people using partial update
        People partialUpdatedPeople = new People();
        partialUpdatedPeople.setId(people.getId());

        partialUpdatedPeople.address(UPDATED_ADDRESS).email(UPDATED_EMAIL).password(UPDATED_PASSWORD).name(UPDATED_NAME);

        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPeople.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPeople))
            )
            .andExpect(status().isOk());

        // Validate the People in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPeopleUpdatableFieldsEquals(partialUpdatedPeople, getPersistedPeople(partialUpdatedPeople));
    }

    @Test
    @Transactional
    void patchNonExistingPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, people.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(people))
            )
            .andExpect(status().isBadRequest());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPeople() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        people.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPeopleMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(people)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the People in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePeople() throws Exception {
        // Initialize the database
        insertedPeople = peopleRepository.saveAndFlush(people);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the people
        restPeopleMockMvc
            .perform(delete(ENTITY_API_URL_ID, people.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return peopleRepository.count();
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

    protected People getPersistedPeople(People people) {
        return peopleRepository.findById(people.getId()).orElseThrow();
    }

    protected void assertPersistedPeopleToMatchAllProperties(People expectedPeople) {
        assertPeopleAllPropertiesEquals(expectedPeople, getPersistedPeople(expectedPeople));
    }

    protected void assertPersistedPeopleToMatchUpdatableProperties(People expectedPeople) {
        assertPeopleAllUpdatablePropertiesEquals(expectedPeople, getPersistedPeople(expectedPeople));
    }
}
