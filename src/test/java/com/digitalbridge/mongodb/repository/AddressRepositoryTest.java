package com.digitalbridge.mongodb.repository;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.digitalbridge.MongoESConfigTest;
import com.digitalbridge.domain.Address;

public class AddressRepositoryTest extends MongoESConfigTest {

  @Test
  public final void testFindByLocationNearMVC() throws Exception {

    this.mockMvc
        .perform(MockMvcRequestBuilders
            .get("/address/search/findByLocationNear?point=40.7408231,-74.0014541&distance=1.0miles&page=0&size=10")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));

  }

  @Test
  public final void testFindByLocationNear() throws Exception {
    Distance distance = new Distance(1, Metrics.MILES);
    Point point = new Point(-74.0014541, 40.7408231);
    List<Address> results = addressRepository.findByLocationNear(point, distance, pageable);
    assertTrue(results.size() > 0);
  }

}
