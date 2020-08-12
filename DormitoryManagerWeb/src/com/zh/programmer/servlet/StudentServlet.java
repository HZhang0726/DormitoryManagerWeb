package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.StudentDao;
import com.zh.programmer.domain.Student;
import com.zh.programmer.util.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/studentServlet")
public class StudentServlet extends HttpServlet {

    private static final long serialVersionUID = -5520199800783490900L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if ("toStudentListView".equals(method)){
            request.getRequestDispatcher("view/studentList.jsp").forward(request,response);
        }
        if ("AddStudent".equals(method)){
            addStudent(request,response);
        }
        if ("StudentList".equals(method)){
            getStudentList(request,response);
        }
        if ("EditStudent".equals(method)){
            editStudent(request,response);
        }
        if ("DeleteStudent".equals(method)){
            deleteStudent(request,response);
        }

    }


    private void deleteStudent(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        StudentDao studentDao = new StudentDao();
        String msg = "";
        if (studentDao.delete(ids)){
            msg = "success";
        }
        studentDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void editStudent(HttpServletRequest request, HttpServletResponse response) {

        Integer id = StringUtil.isEmpty(request.getParameter("id")) ? 0 : Integer.parseInt(request.getParameter("id"));
        String sn = request.getParameter("sn");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String sex = request.getParameter("sex");
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setSn(sn);
        student.setPassword(password);
        student.setSex(sex);
        StudentDao studentDao = new StudentDao();
        String msg = "";
        if (studentDao.update(student)){
            msg = "success";
        }
        studentDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void getStudentList(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter("from");
        //如果来自下拉框查询
        if ("combox".equals(from)){
            returnByCombox(request,response);
            return;
        }

        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String name = request.getParameter("name");
        if (StringUtil.isEmpty(name)){
            name = "";
        }
        Student student = new Student();
        Map<String,Object> ret = new HashMap<>();
        student.setName(name);
        StudentDao studentDao = new StudentDao();
        Page<Student> page = new Page<>(pageNumber,pageSize);
        page.getSearchProperties().add(new SearchProperty("name", "%"+name+"%", Operator.LIKE));
        //判断当前用户是否是学生
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if (userType == 2){
            //如果是学生，则只能查看他自己的信息
            Student loginedStudent = (Student)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("id", loginedStudent.getId(), Operator.EQ));
        }
        Page<Student> findList = studentDao.findAll(page);
        ret.put("rows",findList.getConten());
        ret.put("total",findList.getTotal());
        studentDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.fromObject(ret).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnByCombox(HttpServletRequest request, HttpServletResponse response) {

        StudentDao studentDao = new StudentDao();
        Page<Student> page = new Page<>(1, 9999);
        //判断当前用户是否是学生
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 2){
            //如果是宿管，则只能查看他自己的信息
            Student loginedStudent = (Student) request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("id", loginedStudent.getId(), Operator.EQ));
        }
        page = studentDao.findAll(page);
        studentDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONArray.fromObject(page.getConten()).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStudent(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String sex = request.getParameter("sex");
        response.setCharacterEncoding("utf-8");
        if (StringUtil.isEmpty(name)){
            response.getWriter().write("姓名不能为空！");
            return;
        }
        if (StringUtil.isEmpty(password)){
            response.getWriter().write("密码不能为空！");
            return;
        }
        if (StringUtil.isEmpty(sex)){
            response.getWriter().write("性别不能为空！");
            return;
        }
//        System.out.println(name+password+sex);
        Student student = new Student();
        student.setName(name);
        student.setPassword(password);
        student.setSex(sex);
        student.setSn(StringUtil.generateSn("C","S"));

        StudentDao studentDao = new StudentDao();
        String msg = "添加失败";
        if (studentDao.add(student)){
            msg = "success";
        }
        studentDao.closeConnection();
        response.getWriter().write(msg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
