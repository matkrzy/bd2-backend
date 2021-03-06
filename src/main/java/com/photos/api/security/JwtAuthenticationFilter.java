package com.photos.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photos.api.models.User;
import com.photos.api.models.repositories.UserRepository;
import com.photos.api.services.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.photos.api.security.SecurityConstants.JWT;
import static com.photos.api.security.SecurityConstants.generateToken;

/**
 * @author Micha Królewski on 2018-04-21.
 * @version x
 */


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        } catch (IOException e) {
            throw new AuthenticationCredentialsNotFoundException("");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
        Cookie cookie = new Cookie(JWT, generateToken(auth));
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        String email = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email);

        JSONObject responseObject = new JSONObject();
        try {
            responseObject.put("email", user.getEmail());
            responseObject.put("uuid", user.getUuid());

        }catch (Exception e){

        }

        response.setContentType("application/json");
        ServletOutputStream responseStream = response.getOutputStream();
        responseStream.print(responseObject.toString());
    }
}
