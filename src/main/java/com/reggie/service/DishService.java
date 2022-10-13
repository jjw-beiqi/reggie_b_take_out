package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表
    public void saveWithFlavor(DishDto dishDto);

    // 修改菜品，同时修改菜品对应到口味数据，需要操作两张表
    public void updateWithFlavor(DishDto dishDto);

    // 根据菜品id查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);

    // 根据菜品id修改菜品状态
    public void updateStatus(Integer status, Long id);
}
