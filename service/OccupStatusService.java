package com.cts.cj.service;

import java.util.List;

import com.cts.cj.domain.OccupationStaus;

public interface OccupStatusService {
	
	public List<OccupationStaus> getOccupStatusList();
	
	public OccupationStaus findByName(String name);

}
