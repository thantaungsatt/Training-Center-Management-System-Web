package com.trainingcenter.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.trainingcenter.entities.Role;
import com.trainingcenter.entities.User;
import com.trainingcenter.enums.ActiveStatus;
import com.trainingcenter.repositories.RoleRepository;
import com.trainingcenter.repositories.UserRepository;

@Component
@Profile("dev")
public class DevelopmentHandler implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		Role roles= roleRepository.findByCode("ADMIN")
				.orElseGet(()->{
					Role role=new Role();
					role.setName("Admin");
					role.setCode("ADMIN");
					return roleRepository.save(role);
				});
		
		if(!userRepository.existsByUsername("admin")) {
			User user =new User();
			user.setName("Admin");
			user.setUsername("admin");
			user.setPhoneNumber("0963637172");
			user.setEmail("tas@gmail.com");
			user.setPassword(passwordEncoder.encode("123456"));
			user.setStatus(ActiveStatus.ACTIVE);
			user.setRole(roles);
			
			userRepository.save(user);
		}
		
	}
}
