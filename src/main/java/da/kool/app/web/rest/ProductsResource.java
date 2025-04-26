package da.kool.app.web.rest;

import da.kool.app.domain.Products;
import da.kool.app.repository.ProductsRepository;
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
 * REST controller for managing {@link da.kool.app.domain.Products}.
 */
@RestController
@RequestMapping("/api/products")
@Transactional
public class ProductsResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProductsResource.class);

    private static final String ENTITY_NAME = "products";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProductsRepository productsRepository;

    public ProductsResource(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    /**
     * {@code POST  /products} : Create a new products.
     *
     * @param products the products to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new products, or with status {@code 400 (Bad Request)} if the products has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Products> createProducts(@Valid @RequestBody Products products) throws URISyntaxException {
        LOG.debug("REST request to save Products : {}", products);
        if (products.getId() != null) {
            throw new BadRequestAlertException("A new products cannot already have an ID", ENTITY_NAME, "idexists");
        }
        products = productsRepository.save(products);
        return ResponseEntity.created(new URI("/api/products/" + products.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, products.getId().toString()))
            .body(products);
    }

    /**
     * {@code PUT  /products/:id} : Updates an existing products.
     *
     * @param id the id of the products to save.
     * @param products the products to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated products,
     * or with status {@code 400 (Bad Request)} if the products is not valid,
     * or with status {@code 500 (Internal Server Error)} if the products couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Products> updateProducts(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Products products
    ) throws URISyntaxException {
        LOG.debug("REST request to update Products : {}, {}", id, products);
        if (products.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, products.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        products = productsRepository.save(products);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, products.getId().toString()))
            .body(products);
    }

    /**
     * {@code PATCH  /products/:id} : Partial updates given fields of an existing products, field will ignore if it is null
     *
     * @param id the id of the products to save.
     * @param products the products to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated products,
     * or with status {@code 400 (Bad Request)} if the products is not valid,
     * or with status {@code 404 (Not Found)} if the products is not found,
     * or with status {@code 500 (Internal Server Error)} if the products couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Products> partialUpdateProducts(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Products products
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Products partially : {}, {}", id, products);
        if (products.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, products.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!productsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Products> result = productsRepository
            .findById(products.getId())
            .map(existingProducts -> {
                if (products.getTitle() != null) {
                    existingProducts.setTitle(products.getTitle());
                }
                if (products.getCategory() != null) {
                    existingProducts.setCategory(products.getCategory());
                }
                if (products.getPrice() != null) {
                    existingProducts.setPrice(products.getPrice());
                }

                return existingProducts;
            })
            .map(productsRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, products.getId().toString())
        );
    }

    /**
     * {@code GET  /products} : get all the products.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of products in body.
     */
    @GetMapping("")
    public List<Products> getAllProducts() {
        LOG.debug("REST request to get all Products");
        return productsRepository.findAll();
    }

    /**
     * {@code GET  /products/:id} : get the "id" products.
     *
     * @param id the id of the products to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the products, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Products> getProducts(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Products : {}", id);
        Optional<Products> products = productsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(products);
    }

    /**
     * {@code DELETE  /products/:id} : delete the "id" products.
     *
     * @param id the id of the products to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducts(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Products : {}", id);
        productsRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
