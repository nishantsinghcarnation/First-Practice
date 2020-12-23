package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.Occupation;

public interface OccupService {
	
	public List<Occupation> getOccupationList();
	
	public Occupation findByName(String name);	
	
}
