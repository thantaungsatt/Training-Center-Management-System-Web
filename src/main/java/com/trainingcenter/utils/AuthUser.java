package com.trainingcenter.utils;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.trainingcenter.entities.User;

import lombok.Getter;

@Getter
public class AuthUser implements UserDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3771096167423209543L;

	@Autowired
	private User user;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(user.getRole().getCode()));
	}

	@Override
	public String getPassword() {

		return user.getPassword();
	}

	@Override
	public String getUsername() {

		return user.getName();
	}

}
