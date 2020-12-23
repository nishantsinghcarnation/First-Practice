package com.cts.cj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.Country;
import com.cts.cj.repository.CountryRepository;

@Service
public class CountryServiceImpl implements CountryService{
	
	
	@Autowired
	CountryRepository countryRepository;

	@Override
	public List<Country> getCountryList() {
		
		return countryRepository.getCountryList();	
		
	}

	@Override
	public Country getCountryById(String name) {
	
		return null;
	}

}
