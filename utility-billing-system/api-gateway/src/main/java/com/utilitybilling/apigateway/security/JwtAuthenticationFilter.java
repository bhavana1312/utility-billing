package com.utilitybilling.apigateway.security;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Value("${jwt.secret}")
	private String secret;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {

		try {
			String header = request.getHeader("Authorization");

			if (header != null && header.startsWith("Bearer ")) {
				String token = header.substring(7);

				Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

				String username = claims.getSubject();
				List<String> roles = claims.get("roles", List.class);

				var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r))
						.collect(Collectors.toList());

				var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

				SecurityContextHolder.getContext().setAuthentication(auth);
			}

			chain.doFilter(request, response);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
