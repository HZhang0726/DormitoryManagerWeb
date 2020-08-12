package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.config.BaseConfig;
import com.zh.programmer.dao.AdminDao;
import com.zh.programmer.dao.DormitoryManagerDao;
import com.zh.programmer.dao.StudentDao;
import com.zh.programmer.domain.Admin;
import com.zh.programmer.domain.DormitoryManager;
import com.zh.programmer.domain.Student;
import com.zh.programmer.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/loginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 4317530397870140300L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String vCode = request.getParameter("vcode");
        String msg = "success";
        if (StringUtil.isEmpty(name)){
            msg = "用户名不能为空！";
        }
        if (StringUtil.isEmpty(password)){
            msg = "密码不能为空！";
        }
        if (StringUtil.isEmpty(vCode)){
            msg = "验证码不能为空！";
        }
        if ("success".equals(msg)){
            Object loginCpacha = request.getSession().getAttribute("loginCpacha");
            if (loginCpacha == null){
                msg = "session已经过期，请刷新重试";
            }else {
                if (!vCode.toUpperCase().equals(loginCpacha.toString().toUpperCase())){
                    msg = "验证码错误";
                }
            }
        }
        if ("success".equals(msg)){
            String typeString = request.getParameter("type");
            try {
                int type = Integer.parseInt(typeString);
                if (type == 1){
                    //超级管理员用户
                    AdminDao adminDao = new AdminDao();
                    Admin admin = adminDao.getAdmin(name);
                    adminDao.closeConnection();
                    if (admin == null){
                        msg = "不存在该用户";
                    }
                    if (admin != null){
                        if (!password.equals(admin.getPassword())){
                            msg = "密码错误";
                        }else {
                            if (admin.getStatus() == BaseConfig.ADMIN_STATUS_DISAABLE){
                                msg = "该用户状态不可用，请联系管理员！";
                            }else{
                                request.getSession().setAttribute("user",admin);
                                request.getSession().setAttribute("userType",type);
                            }
                        }
                    }
                }else if (type == 2){
                    //学生登录
                    StudentDao studentDao = new StudentDao();
                    Page<Student> page = new Page<>(1,10);
                    page.getSearchProperties().add(new SearchProperty("name",name, Operator.EQ));
                    Page<Student> studentPage = studentDao.findAll(page);
                    studentDao.closeConnection();
                    if (studentPage.getConten().size() == 0){
                        msg = "不存在该用户";
                    }else {
                        Student student = studentPage.getConten().get(0);
                        if (!password.equals(student.getPassword())){
                            msg = "密码错误";
                        }else{
                            request.getSession().setAttribute("user",student);
                            request.getSession().setAttribute("userType",type);
                        }
                    }

                }else if (type == 3){
                    //宿管登录
                    DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
                    Page<DormitoryManager> page = new Page<>(1,10);
                    page.getSearchProperties().add(new SearchProperty("name", name, Operator.EQ));
                    page = dormitoryManagerDao.findAll(page);
                    dormitoryManagerDao.closeConnection();
                    if (page.getConten().size() == 0){
                        msg = "不存在该用户";
                    }else{
                        DormitoryManager dormitoryManager = page.getConten().get(0);
                        if (!password.equals(dormitoryManager.getPassword())){
                            msg = "密码错误";
                        }else{
                            request.getSession().setAttribute("user",dormitoryManager);
                            request.getSession().setAttribute("userType",type);
                        }
                    }
                }else {
                    msg = "用户类型错误";
                }
            }catch (Exception e){
                msg = "用户类型错误";
            }
        }
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(msg);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
