package com.cts.cj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.Country;
import com.cts.cj.domain.ProfessionType;
import com.cts.cj.repository.ProfessionRepository;

@Service
public class ProfessionServiceImpl implements ProfessionService {
	
	@Autowired
	ProfessionRepository professionRepository;

	@Override
	public List<ProfessionType> getProfessionList() {
		return professionRepository.getProfessionList();
		
	}

	@Override
	public ProfessionType getProfessionById(Long Id) {
		return professionRepository.getProfessionById(Id);
	}

}
