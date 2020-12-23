package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.Country;

public interface CountryService {
	
	public List<Country> getCountryList();
	
	public Country getCountryById(String name);

}
