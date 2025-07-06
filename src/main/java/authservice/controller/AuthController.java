package authservice.controller;

import authservice.entities.RefreshToken;
import authservice.model.UserInfoDto;
import authservice.request.LoginRequest;
import authservice.request.RefreshTokenRequestDTO;
import authservice.response.JwtResponseDTO;
import authservice.service.JwtService;
import authservice.service.RefreshTokenService;
import authservice.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private AuthenticationManager authenticationManager;


	@PostMapping("/v1/signup")
	public ResponseEntity<?> SignUp(@RequestBody UserInfoDto userInfoDto){
		try{
			String userId = userDetailsService.signupUser(userInfoDto);
			if(Objects.isNull(userId)){
				return new ResponseEntity<>("Already Exist", HttpStatus.BAD_REQUEST);
			}
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
			String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());
			return new ResponseEntity<>(JwtResponseDTO.builder().accessToken(jwtToken).
					token(refreshToken.getToken()).userId(userId).build(), HttpStatus.OK);
		}catch (Exception ex){
			ex.printStackTrace();
			return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/v1/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request){
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		if(authenticate.isAuthenticated()){
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());
			String userId = userDetailsService.getUserByUsername(request.getUsername());

			if(Objects.nonNull(userId) && Objects.nonNull(refreshToken)){
				return new ResponseEntity<>(
								JwtResponseDTO.builder()
								.accessToken(jwtService.GenerateToken(request.getUsername()))
								.token(refreshToken.getToken())
								.build(),HttpStatus.OK);
			}
		}
		return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@PostMapping("/v1/refreshToken")
	public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
		return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
				.map(refreshTokenService::verifyExpiration)
				.map(RefreshToken::getUserInfo)
				.map(userInfo -> {
					String accessToken = jwtService.GenerateToken(userInfo.getUsername());
					return JwtResponseDTO.builder()
							.accessToken(accessToken)
							.token(refreshTokenRequestDTO.getToken()).build();
				}).orElseThrow(() ->new RuntimeException("Refresh Token is not in DB..!!"));
	}
}
