package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //使用了最先进的bcrypt加密解密
        if (!passwordEncoder.matches(password, employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @Override
    public void Add(EmployeeDTO employee) {
        Employee employeeEntity = new Employee();
        BeanUtils.copyProperties(employee, employeeEntity);
        employeeEntity.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        employeeEntity.setStatus(StatusConstant.ENABLE);
        employeeEntity.setCreateTime(LocalDateTime.now());
        employeeEntity.setUpdateTime(LocalDateTime.now());

        long empID = BaseContext.getCurrentId();
        employeeEntity.setCreateUser(empID);
        employeeEntity.setUpdateUser(empID);

        log.info("添加一个员工，用户名: "+employeeEntity.getUsername());
        employeeMapper.Insert(employeeEntity);
    }

    /**
     * 分页查询
     *
     * @param employeeDTO
     * @return
     */
    @Override
    public PageResult PageQuery(EmployeePageQueryDTO employeeDTO) {
        PageHelper.startPage(employeeDTO.getPage(), employeeDTO.getPageSize());
        List<Employee> list = employeeMapper.pageQuery(employeeDTO.getName());
        PageInfo<Employee> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 更改员工的status，1启用，0停用，停用者无法通过登录认证
     *
     * @param id, status
     * @return
     */
    @Override
    public void SetStatus(Long id, Integer status) {
        Employee _employee = Employee.builder()
                                    .status(status)
                                    .id(id)
                                    .build();
        employeeMapper.Update(_employee);

    }

    @Override
    public Employee GetById(Long id) {
        return employeeMapper.GetById(id);
    }

    @Override
    public void UpdateEmployee(EmployeeDTO employee) {
        Employee _employee = new Employee();
        BeanUtils.copyProperties(employee, _employee);
        _employee.setUpdateTime(LocalDateTime.now());
        _employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.Update(_employee);
    }

}
