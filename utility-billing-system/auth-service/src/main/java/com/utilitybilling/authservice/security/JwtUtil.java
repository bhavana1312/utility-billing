package com.utilitybilling.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.utilitybilling.authservice.feign.ConsumerClient;
import com.utilitybilling.authservice.feign.ConsumerResponse;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;
	private final ConsumerClient consumerClient;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String username, List<String> roles) {
		if("ROLE_USER".equals(roles.get(0))) {
			ConsumerResponse c = consumerClient.getByUsername(username);
			
			return Jwts.builder().setSubject(c.getId()).claim("roles", roles).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + 3600000))
					.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
		}
		
		return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
	}
}
