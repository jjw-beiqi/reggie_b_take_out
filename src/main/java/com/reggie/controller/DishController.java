package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Executor taskExecutor;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
//        // 构造 redis 中的key
//        String key = "dish_"+ dishDto.getCategoryId() + "_1";
//        // 清理缓存数据
//        redisTemplate.delete(key);

        dishService.saveWithFlavor(dishDto);

//        // 采用延迟双删策略，保证数据的一致性
//        taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                    // 清理缓存数据
//                    redisTemplate.delete(key);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        return R.success("新增菜品成功！");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加过滤条件
        queryWrapper.like(name != null, Dish::getName,name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        // 对象拷贝
        // 参数一：原对象
        // 参数二：拷贝对象
        // 参数三：除了哪些属性不拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        // 查询出来到数据
        List<Dish> records = pageInfo.getRecords();

        // 进行属性拷贝，以及设置菜品分类名称
        List<DishDto> list = records.stream().map(record ->{
            DishDto dishDto = new DishDto();

            // 拷贝所有属性
            BeanUtils.copyProperties(record,dishDto);

            // 根据菜品id查询菜品
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);

            // 获取分类名称，并给 dishDto 设置菜品分类名称
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应到口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 根据id修改菜品信息，以及对应的口味信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
//        // 构造 redis 中的key
//        String key = "dish_"+ dishDto.getCategoryId() + "_1";
//        // 清理缓存数据
//        redisTemplate.delete(key);

        dishService.updateWithFlavor(dishDto);

//        // 采用延迟双删策略，保证数据的一致性
//        taskExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(500);
//                    // 清理缓存数据
//                    redisTemplate.delete(key);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        return R.success("更新菜品信息成功！");
    }

    /**
     * 菜品停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,Long ids){
        dishService.updateStatus(status,ids);

        return R.success("菜品已停售！");
    }

    /**
     * 根据条件查询菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = null;

//        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
//        // 先从redis中获取缓存数据
//        dishDtoList = (List<DishDto>)redisTemplate.opsForValue().get(key);
//
//        if (dishDtoList!=null){
//            // 如果存在，直接返回，无需查询数据库
//            return R.success(dishDtoList);
//        }

        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        queryWrapper.eq(Dish::getStatus,1); // 1代表正在售卖的菜品
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map(record ->{
            DishDto dishDto = new DishDto();

            // 拷贝所有属性
            BeanUtils.copyProperties(record,dishDto);

            // 根据菜品id查询菜品
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);

            // 获取分类名称，并给 dishDto 设置菜品分类名称
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = record.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());
//
//        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
