package com.digitalbridge.mongodb.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;
import org.springframework.security.core.authority.AuthorityUtils;

import com.digitalbridge.MongoESConfigTest;
import com.digitalbridge.domain.User;
import com.digitalbridge.util.KeyGeneratorUtil;

public class UserRepositoryTest extends MongoESConfigTest {

  @Test
  public void createUsers() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
      IllegalBlockSizeException, BadPaddingException {
    User user = new User("appUser", KeyGeneratorUtil.encrypt("appPassword"),
        AuthorityUtils.createAuthorityList("ROLE_USER"));
    User adminUser = new User("appAdmin", KeyGeneratorUtil.encrypt("appPassword"),
        AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    User superAdmin = new User("admin", KeyGeneratorUtil.encrypt("secret"),
        AuthorityUtils.createAuthorityList("ROLE_SUPERUSER"));
    List<User> userList = new ArrayList<User>();
    userList.add(user);
    userList.add(adminUser);
    userList.add(superAdmin);
    userRepository.deleteAll();
    userRepository.save(userList);
    assertTrue(userRepository.count() == 3);
    User user1 = userRepository.findByUserName("admin");
    assertTrue(user1.getUserName().equals("admin"));
    assertNotNull(user1);
  }

}
