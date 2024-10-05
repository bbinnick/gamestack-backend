package com.bbinnick.gamestack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
class UserRepositoryTests {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository repo;

	// test methods go below
	@Test
	public void testCreateUser() {
		User user = new User();
		user.setEmail("binnick@gmail.com");
		user.setPassword("capstone");
		user.setFirstName("Brandon");
		user.setLastName("Binnick");

		User savedUser = repo.save(user);

		User existUser = entityManager.find(User.class, savedUser.getId());

		assertEquals(user.getFirstName(), existUser.getFirstName());

	}
}