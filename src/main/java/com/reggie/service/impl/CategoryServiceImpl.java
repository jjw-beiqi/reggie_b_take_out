package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.Setmeal;
import com.reggie.exception.CustomException;
import com.reggie.mapper.CategoryMapper;
import com.reggie.service.CategoryService;
import com.reggie.service.DishService;
import com.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类信息，删除之前判断当前分类是否关联了菜品或套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);

        // 是否关联菜品，关联了就抛出异常
        int count = dishService.count(dishLambdaQueryWrapper);
        if (count > 0){
            // 已经关联了菜品，抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }

        // 条件构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        // 是否关联套餐，关联了就抛出异常
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if (count1 > 0){
            // 已经关联了套餐，抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除！");
        }

        // 正常删除
        super.removeById(id);
    }
}
