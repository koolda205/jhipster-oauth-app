package da.kool.app.web.rest;

import da.kool.app.domain.People;
import da.kool.app.repository.PeopleRepository;
import da.kool.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link da.kool.app.domain.People}.
 */
@RestController
@RequestMapping("/api/people")
@Transactional
public class PeopleResource {

    private static final Logger LOG = LoggerFactory.getLogger(PeopleResource.class);

    private static final String ENTITY_NAME = "people";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PeopleRepository peopleRepository;

    public PeopleResource(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    /**
     * {@code POST  /people} : Create a new people.
     *
     * @param people the people to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new people, or with status {@code 400 (Bad Request)} if the people has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<People> createPeople(@Valid @RequestBody People people) throws URISyntaxException {
        LOG.debug("REST request to save People : {}", people);
        if (people.getId() != null) {
            throw new BadRequestAlertException("A new people cannot already have an ID", ENTITY_NAME, "idexists");
        }
        people = peopleRepository.save(people);
        return ResponseEntity.created(new URI("/api/people/" + people.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, people.getId().toString()))
            .body(people);
    }

    /**
     * {@code PUT  /people/:id} : Updates an existing people.
     *
     * @param id the id of the people to save.
     * @param people the people to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated people,
     * or with status {@code 400 (Bad Request)} if the people is not valid,
     * or with status {@code 500 (Internal Server Error)} if the people couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<People> updatePeople(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody People people
    ) throws URISyntaxException {
        LOG.debug("REST request to update People : {}, {}", id, people);
        if (people.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, people.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!peopleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        people = peopleRepository.save(people);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, people.getId().toString()))
            .body(people);
    }

    /**
     * {@code PATCH  /people/:id} : Partial updates given fields of an existing people, field will ignore if it is null
     *
     * @param id the id of the people to save.
     * @param people the people to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated people,
     * or with status {@code 400 (Bad Request)} if the people is not valid,
     * or with status {@code 404 (Not Found)} if the people is not found,
     * or with status {@code 500 (Internal Server Error)} if the people couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<People> partialUpdatePeople(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody People people
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update People partially : {}, {}", id, people);
        if (people.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, people.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!peopleRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<People> result = peopleRepository
            .findById(people.getId())
            .map(existingPeople -> {
                if (people.getAddress() != null) {
                    existingPeople.setAddress(people.getAddress());
                }
                if (people.getEmail() != null) {
                    existingPeople.setEmail(people.getEmail());
                }
                if (people.getPassword() != null) {
                    existingPeople.setPassword(people.getPassword());
                }
                if (people.getName() != null) {
                    existingPeople.setName(people.getName());
                }

                return existingPeople;
            })
            .map(peopleRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, people.getId().toString())
        );
    }

    /**
     * {@code GET  /people} : get all the people.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of people in body.
     */
    @GetMapping("")
    public List<People> getAllPeople() {
        LOG.debug("REST request to get all People");
        return peopleRepository.findAll();
    }

    /**
     * {@code GET  /people/:id} : get the "id" people.
     *
     * @param id the id of the people to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the people, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<People> getPeople(@PathVariable("id") Long id) {
        LOG.debug("REST request to get People : {}", id);
        Optional<People> people = peopleRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(people);
    }

    /**
     * {@code DELETE  /people/:id} : delete the "id" people.
     *
     * @param id the id of the people to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeople(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete People : {}", id);
        peopleRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
