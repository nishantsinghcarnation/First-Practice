package com.cts.cj.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.OccupationStaus;
import com.cts.cj.repository.OccupStatusRepository;

@Service
public class OccupStatusSeriviceImpl  implements OccupStatusService {
	
	@Autowired
	OccupStatusRepository occupStatusRepository;

	@Override
	public List<OccupationStaus> getOccupStatusList() {
		
		return occupStatusRepository.getstatusList();
	}

	@Override
	public OccupationStaus findByName(String name) {
		return occupStatusRepository.findByName(name);
	}

}
