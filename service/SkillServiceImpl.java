package com.cts.cj.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cts.cj.domain.Skill;
import com.cts.cj.dto.SkillDto;
import com.cts.cj.repository.SkillRepository;

@Service
public class SkillServiceImpl implements SkillService{
	@Autowired
	private SkillRepository skillRepo; 
	@Override
	public String addSkill(List<SkillDto> skilldto) {

		List<Skill> dbSkill= new ArrayList<Skill>();
		
		skilldto.forEach(s->{
			Skill lskill= new Skill();
			BeanUtils.copyProperties(s, lskill);
			dbSkill.add(lskill);
		});
		
		skillRepo.saveAll(dbSkill);
		return "{\"skillsaved\": \" " + true + "\"}";
	}

	@Override
	public String deleteSkill(SkillDto skilldto) {
		Skill skill;
		skill=skillRepo.getRelationShip(skilldto.getName());
		if(skill!=null) {
			skill= new Skill();
		BeanUtils.copyProperties(skill, skilldto);
		skillRepo.delete(skill);
		return "{\"skilldelete\": \" true\"}";
		}else {
			return "{\"error\": \" Can not delete SKill ,a RelationShip exists.\"}";
		}
	}

	@Override
	public String updateSkill(SkillDto skilldto) {
		Skill skill= new Skill();
		BeanUtils.copyProperties(skill, skilldto);
		skillRepo.save(skill);
		return "{\"skillupdate\": \" true\"}";
	}

	@Override
	public List<SkillDto> getAllSkill() {
		List<Skill> dbskill=skillRepo.getSkillList();
		List<SkillDto> skill=new ArrayList<SkillDto>();
		dbskill.forEach(ds->{
			
			SkillDto lskill= new SkillDto();
			BeanUtils.copyProperties(ds, lskill);
			skill.add(lskill);
		});
		return skill;
	}

	@Override
	public List<SkillDto> serachSkillList(String skillName) {
		// TODO Auto-generated method stub
		List<Skill> dbskill=skillRepo.serachSkillList("(?i)"+skillName+".*");
		List<SkillDto> skill=new ArrayList<SkillDto>();
		dbskill.forEach(ds->{
			SkillDto lskill= new SkillDto();
			BeanUtils.copyProperties(ds, lskill);
			skill.add(lskill);
		});
		return skill;
	}

}
