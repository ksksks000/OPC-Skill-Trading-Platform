package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.dto.SkillDTO;
import com.sky.dto.SkillPageQueryDTO;
import com.sky.entity.Skill;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SkillMapper;
import com.sky.result.PageResult;
import com.sky.service.SkillService;
import com.sky.vo.SkillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SkillServiceImpl implements SkillService {

    @Autowired
    private SkillMapper skillMapper;

    @Override
    public void save(SkillDTO skillDTO) {
        Skill skill = new Skill();
        BeanUtils.copyProperties(skillDTO, skill);
        skill.setStatus(StatusConstant.ENABLE);
        skillMapper.insert(skill);
    }

    @Override
    public PageResult pageQuery(SkillPageQueryDTO dto) {
        Page<Skill> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(dto.getName() != null && !dto.getName().isEmpty(), Skill::getName, dto.getName())
               .eq(dto.getCategoryId() != null, Skill::getCategoryId, dto.getCategoryId())
               .eq(dto.getStatus() != null, Skill::getStatus, dto.getStatus())
               .eq(dto.getSellerId() != null, Skill::getSellerId, dto.getSellerId())
               .orderByDesc(Skill::getCreateTime);

        Page<Skill> result = skillMapper.selectPage(page, wrapper);

        List<SkillVO> voList = new ArrayList<>();
        for (Skill skill : result.getRecords()) {
            SkillVO vo = new SkillVO();
            BeanUtils.copyProperties(skill, vo);
            voList.add(vo);
        }

        return new PageResult(result.getTotal(), voList);
    }

    @Override
    public void update(SkillDTO skillDTO) {
        Skill skill = new Skill();
        BeanUtils.copyProperties(skillDTO, skill);
        skillMapper.updateById(skill);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Skill> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Skill::getId, id)
               .set(Skill::getStatus, status);
        skillMapper.update(null, wrapper);
    }

    @Override
    public SkillVO getById(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            return null;
        }
        SkillVO vo = new SkillVO();
        BeanUtils.copyProperties(skill, vo);
        return vo;
    }

    @Override
    public List<Skill> searchSkills(String category, String tag, Double minPrice) {
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Skill::getStatus, StatusConstant.ENABLE);

        if (category != null && !category.isEmpty()) {
            wrapper.like(Skill::getTags, category);
        }
        if (tag != null && !tag.isEmpty()) {
            wrapper.like(Skill::getTags, tag);
        }
        if (minPrice != null) {
            wrapper.ge(Skill::getPrice, minPrice);
        }

        wrapper.orderByDesc(Skill::getCreateTime);
        return skillMapper.selectList(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        Skill skill = skillMapper.selectById(id);
        if (skill != null && skill.getStatus().equals(StatusConstant.ENABLE)) {
            throw new DeletionNotAllowedException("上架中的技能不能删除");
        }
        skillMapper.deleteById(id);
    }
}
