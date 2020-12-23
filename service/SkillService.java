package com.cts.cj.service;

import java.util.List;

import com.cts.cj.dto.SkillDto;

public interface SkillService {
	String addSkill(List<SkillDto> skilldto);
	String deleteSkill(SkillDto skilldto);
	String updateSkill(SkillDto skilldto);
	List<SkillDto> getAllSkill();
	List<SkillDto> serachSkillList(String skillName);

}
