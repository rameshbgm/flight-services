package com.dxc.application.web.rest;

import com.dxc.application.FlightserviceApp;

import com.dxc.application.domain.FlightDetails;
import com.dxc.application.repository.FlightDetailsRepository;
import com.dxc.application.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;


import static com.dxc.application.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the FlightDetailsResource REST controller.
 *
 * @see FlightDetailsResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlightserviceApp.class)
public class FlightDetailsResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String DEFAULT_IATA = "AAAAAAAAAA";
    private static final String UPDATED_IATA = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_LATITUDE = new BigDecimal(1);
    private static final BigDecimal UPDATED_LATITUDE = new BigDecimal(2);

    private static final BigDecimal DEFAULT_LONGITUDE = new BigDecimal(1);
    private static final BigDecimal UPDATED_LONGITUDE = new BigDecimal(2);

    @Autowired
    private FlightDetailsRepository flightDetailsRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restFlightDetailsMockMvc;

    private FlightDetails flightDetails;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final FlightDetailsResource flightDetailsResource = new FlightDetailsResource(flightDetailsRepository);
        this.restFlightDetailsMockMvc = MockMvcBuilders.standaloneSetup(flightDetailsResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FlightDetails createEntity(EntityManager em) {
        FlightDetails flightDetails = new FlightDetails()
            .name(DEFAULT_NAME)
            .city(DEFAULT_CITY)
            .country(DEFAULT_COUNTRY)
            .iata(DEFAULT_IATA)
            .latitude(DEFAULT_LATITUDE)
            .longitude(DEFAULT_LONGITUDE);
        return flightDetails;
    }

    @Before
    public void initTest() {
        flightDetails = createEntity(em);
    }

    @Test
    @Transactional
    public void createFlightDetails() throws Exception {
        int databaseSizeBeforeCreate = flightDetailsRepository.findAll().size();

        // Create the FlightDetails
        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isCreated());

        // Validate the FlightDetails in the database
        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeCreate + 1);
        FlightDetails testFlightDetails = flightDetailsList.get(flightDetailsList.size() - 1);
        assertThat(testFlightDetails.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testFlightDetails.getCity()).isEqualTo(DEFAULT_CITY);
        assertThat(testFlightDetails.getCountry()).isEqualTo(DEFAULT_COUNTRY);
        assertThat(testFlightDetails.getIata()).isEqualTo(DEFAULT_IATA);
        assertThat(testFlightDetails.getLatitude()).isEqualTo(DEFAULT_LATITUDE);
        assertThat(testFlightDetails.getLongitude()).isEqualTo(DEFAULT_LONGITUDE);
    }

    @Test
    @Transactional
    public void createFlightDetailsWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = flightDetailsRepository.findAll().size();

        // Create the FlightDetails with an existing ID
        flightDetails.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        // Validate the FlightDetails in the database
        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setName(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCityIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setCity(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCountryIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setCountry(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkIataIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setIata(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLatitudeIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setLatitude(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkLongitudeIsRequired() throws Exception {
        int databaseSizeBeforeTest = flightDetailsRepository.findAll().size();
        // set the field null
        flightDetails.setLongitude(null);

        // Create the FlightDetails, which fails.

        restFlightDetailsMockMvc.perform(post("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllFlightDetails() throws Exception {
        // Initialize the database
        flightDetailsRepository.saveAndFlush(flightDetails);

        // Get all the flightDetailsList
        restFlightDetailsMockMvc.perform(get("/api/flight-details?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(flightDetails.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY.toString())))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY.toString())))
            .andExpect(jsonPath("$.[*].iata").value(hasItem(DEFAULT_IATA.toString())))
            .andExpect(jsonPath("$.[*].latitude").value(hasItem(DEFAULT_LATITUDE.intValue())))
            .andExpect(jsonPath("$.[*].longitude").value(hasItem(DEFAULT_LONGITUDE.intValue())));
    }
    
    @Test
    @Transactional
    public void getFlightDetails() throws Exception {
        // Initialize the database
        flightDetailsRepository.saveAndFlush(flightDetails);

        // Get the flightDetails
        restFlightDetailsMockMvc.perform(get("/api/flight-details/{id}", flightDetails.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(flightDetails.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY.toString()))
            .andExpect(jsonPath("$.country").value(DEFAULT_COUNTRY.toString()))
            .andExpect(jsonPath("$.iata").value(DEFAULT_IATA.toString()))
            .andExpect(jsonPath("$.latitude").value(DEFAULT_LATITUDE.intValue()))
            .andExpect(jsonPath("$.longitude").value(DEFAULT_LONGITUDE.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingFlightDetails() throws Exception {
        // Get the flightDetails
        restFlightDetailsMockMvc.perform(get("/api/flight-details/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateFlightDetails() throws Exception {
        // Initialize the database
        flightDetailsRepository.saveAndFlush(flightDetails);

        int databaseSizeBeforeUpdate = flightDetailsRepository.findAll().size();

        // Update the flightDetails
        FlightDetails updatedFlightDetails = flightDetailsRepository.findById(flightDetails.getId()).get();
        // Disconnect from session so that the updates on updatedFlightDetails are not directly saved in db
        em.detach(updatedFlightDetails);
        updatedFlightDetails
            .name(UPDATED_NAME)
            .city(UPDATED_CITY)
            .country(UPDATED_COUNTRY)
            .iata(UPDATED_IATA)
            .latitude(UPDATED_LATITUDE)
            .longitude(UPDATED_LONGITUDE);

        restFlightDetailsMockMvc.perform(put("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedFlightDetails)))
            .andExpect(status().isOk());

        // Validate the FlightDetails in the database
        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeUpdate);
        FlightDetails testFlightDetails = flightDetailsList.get(flightDetailsList.size() - 1);
        assertThat(testFlightDetails.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testFlightDetails.getCity()).isEqualTo(UPDATED_CITY);
        assertThat(testFlightDetails.getCountry()).isEqualTo(UPDATED_COUNTRY);
        assertThat(testFlightDetails.getIata()).isEqualTo(UPDATED_IATA);
        assertThat(testFlightDetails.getLatitude()).isEqualTo(UPDATED_LATITUDE);
        assertThat(testFlightDetails.getLongitude()).isEqualTo(UPDATED_LONGITUDE);
    }

    @Test
    @Transactional
    public void updateNonExistingFlightDetails() throws Exception {
        int databaseSizeBeforeUpdate = flightDetailsRepository.findAll().size();

        // Create the FlightDetails

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFlightDetailsMockMvc.perform(put("/api/flight-details")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(flightDetails)))
            .andExpect(status().isBadRequest());

        // Validate the FlightDetails in the database
        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteFlightDetails() throws Exception {
        // Initialize the database
        flightDetailsRepository.saveAndFlush(flightDetails);

        int databaseSizeBeforeDelete = flightDetailsRepository.findAll().size();

        // Get the flightDetails
        restFlightDetailsMockMvc.perform(delete("/api/flight-details/{id}", flightDetails.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<FlightDetails> flightDetailsList = flightDetailsRepository.findAll();
        assertThat(flightDetailsList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FlightDetails.class);
        FlightDetails flightDetails1 = new FlightDetails();
        flightDetails1.setId(1L);
        FlightDetails flightDetails2 = new FlightDetails();
        flightDetails2.setId(flightDetails1.getId());
        assertThat(flightDetails1).isEqualTo(flightDetails2);
        flightDetails2.setId(2L);
        assertThat(flightDetails1).isNotEqualTo(flightDetails2);
        flightDetails1.setId(null);
        assertThat(flightDetails1).isNotEqualTo(flightDetails2);
    }
}
