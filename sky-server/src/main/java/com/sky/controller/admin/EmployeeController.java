package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工模块")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation("员工登录")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation("员工登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @ApiOperation("新增员工")
    @PostMapping
    public Result<String> AddEmployee(@RequestBody EmployeeDTO employee) {
        employeeService.Add(employee);

        return Result.success();
    }

    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> GetEmployeePage(EmployeePageQueryDTO employeeDTO) {
        PageResult pageResult = employeeService.PageQuery(employeeDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("启停员工账号")
    @PostMapping("/status/{status}")
    public Result<String> SetStatus(@PathVariable Integer status, Long id) {
        employeeService.SetStatus(id, status);
        return Result.success();
    }

    @ApiOperation("根据员工ID查询")
    @GetMapping("/{id}")
    public Result<Employee> GetEmployeeById(@PathVariable Long id) {
        Employee _employee = employeeService.GetById(id);
        return Result.success(_employee);
    }

    @ApiOperation("修改员工数据")
    @PutMapping
    public Result<String> UpdateEmployee(@RequestBody EmployeeDTO employee) {
        employeeService.UpdateEmployee(employee);
        return Result.success();
    }
}
