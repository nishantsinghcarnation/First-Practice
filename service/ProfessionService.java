package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.ProfessionType;

public interface ProfessionService {
	
	public List<ProfessionType> getProfessionList();
	
	public ProfessionType getProfessionById(Long Id);

}
