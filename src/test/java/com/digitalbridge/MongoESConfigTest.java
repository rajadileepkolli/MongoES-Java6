package com.digitalbridge;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.digitalbridge.mongodb.repository.AddressRepository;
import com.digitalbridge.mongodb.repository.AssetWrapperRepository;
import com.digitalbridge.mongodb.repository.NotesRepository;
import com.digitalbridge.mongodb.repository.UserRepository;
import com.digitalbridge.service.AddressService;
import com.digitalbridge.service.AssetWrapperService;
import com.mongodb.MongoClient;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MongoESConfig.class)
@WebAppConfiguration
@ActiveProfiles("local")
public abstract class MongoESConfigTest {

  protected static final String assetID = "56094694bd51636546272ee8";
  protected static final String USERNAME = "JUNIT_TEST";
  protected static final String PASSWORD = "JUNIT_PASSWORD";
  protected static final String ROLE_USER = "ROLE_USER";
  protected static final String ROLE_ADMIN = "ROLE_ADMIN";

  @Autowired protected AssetWrapperService assetWrapperService;
  @Autowired protected AddressService addressService;

  @Autowired protected AssetWrapperRepository assetWrapperRepository;
  @Autowired protected NotesRepository notesRepository;
  @Autowired protected AddressRepository addressRepository;
  @Autowired protected UserRepository userRepository;

  @Autowired protected MongoClient mongoClient;

  @Autowired private WebApplicationContext context;
  protected MockMvc mockMvc;

  protected Pageable pageable = new PageRequest(0, 10);

  @Before
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
  }

  @After
  public void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
  }

}
