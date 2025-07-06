package authservice.service;

import authservice.entities.UserInfo;
import authservice.event.producer.UserInfoProducer;
import authservice.model.UserInfoDto;
import authservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserInfoProducer userInfoProducer;

	@Autowired
	public UserDetailsServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserInfoProducer userInfoProducer) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userInfoProducer = userInfoProducer;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("Inside loadUserByUsername");
		UserInfo user = userRepository.findByUsername(username);
		if(user == null){
			System.out.println("Username not found: " + username);
			throw new UsernameNotFoundException("could not found user..!!");
		}
		System.out.println("User Authenticated Successfully..!!!");
		return new CustomUserDetails(user);
	}

	public UserInfo checkIfUserAlreadyExist(UserInfoDto userInfoDto){
		return userRepository.findByUsername(userInfoDto.getUsername());
	}

	public String signupUser(UserInfoDto userInfoDto) throws JsonProcessingException {

		userInfoDto.setPassword(passwordEncoder.encode(userInfoDto.getPassword()));
		if(Objects.nonNull(checkIfUserAlreadyExist(userInfoDto))){
			return null;
		}
		String userId = UUID.randomUUID().toString();
		UserInfo userInfo = new UserInfo(userId, userInfoDto.getUsername(), userInfoDto.getPassword(), new HashSet<>());
		userRepository.save(userInfo);
		// pushEventToQueue
		userInfoProducer.sendKafkaEvent(userInfoDto);
		return userId;
	}

	public String getUserByUsername(String username){
		return Optional.of(userRepository.findByUsername(username)).map(UserInfo::getUserId).orElse(null);
	}
}
