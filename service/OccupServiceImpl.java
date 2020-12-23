package com.cts.cj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.Occupation;
import com.cts.cj.repository.OccupationRepository;

@Service
public class OccupServiceImpl  implements OccupService{
	
	@Autowired
	OccupationRepository occupationRepository;
	
	@Override
	public List<Occupation> getOccupationList() {
		
		return occupationRepository.getOccupationList();

	}

	@Override
	public Occupation findByName(String name) {
		return occupationRepository.findByName(name);
	}

}
