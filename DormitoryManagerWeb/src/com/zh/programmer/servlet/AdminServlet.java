package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.AdminDao;
import com.zh.programmer.domain.Admin;
import com.zh.programmer.util.StringUtil;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/adminServlet")
public class AdminServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if("toAdminListView".equals(method)){
            request.getRequestDispatcher("view/adminList.jsp").forward(request,response);
        }
        if("AddAdmin".equals(method)){
            addAdmin(request,response);
        }
        if("AdminList".equals(method)){
            getAdminList(request,response);
        }
        if("EditAdmin".equals(method)){
            editAdmin(request,response);
        }
        if("DeleteAdmin".equals(method)){
            deleteAdmin(request,response);
        }
    }

    private void deleteAdmin(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        AdminDao adminDao = new AdminDao();
        String msg = "";
        if(adminDao.delete(ids)){
            msg = "success";
        }
        adminDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String status = request.getParameter("status");
        String id = request.getParameter("id");
        response.setCharacterEncoding("utf-8");
        if(StringUtil.isEmpty(id)){
            response.getWriter().write("请选择要编辑的管理员!");
            return;
        }
        if(StringUtil.isEmpty(name)){
            response.getWriter().write("姓名不能为空!");
            return;
        }
        if(StringUtil.isEmpty(password)){
            response.getWriter().write("密码不能为空!");
            return;
        }
        if(StringUtil.isEmpty(status)){
            response.getWriter().write("性别不能为空!");
            return;
        }
        Admin admin = new Admin();
        admin.setId(Integer.parseInt(id));
        admin.setName(name);
        admin.setPassword(password);
        admin.setStatus(Integer.parseInt(status));
        AdminDao adminDao = new AdminDao();
        String msg = "修改失败!";
        if (adminDao.update(admin)){
            msg = "success";
        }
        adminDao.closeConnection();
        response.getWriter().write(msg);
    }

    private void getAdminList(HttpServletRequest request, HttpServletResponse response) {
        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String name = request.getParameter("name");
        if(StringUtil.isEmpty(name)){
            name = "";
        }
        Map<String, Object> ret = new HashMap<>();
        AdminDao adminDao = new AdminDao();
        Page<Admin> page = new Page<>(pageNumber, pageSize);
        page.getSearchProperties().add(new SearchProperty("name", "%"+name+"%", Operator.LIKE));
        Page<Admin> findList = adminDao.findAll(page);
        ret.put("rows", findList.getConten());
        ret.put("total", findList.getTotal());
        adminDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.fromObject(ret).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String status = request.getParameter("status");
        response.setCharacterEncoding("utf-8");
        if (StringUtil.isEmpty(name)){
            response.getWriter().write("姓名不能为空!");
            return;
        }
        if(StringUtil.isEmpty(password)){
            response.getWriter().write("密码不能为空!");
            return;
        }
        if(StringUtil.isEmpty(status)){
            response.getWriter().write("性别不能为空!");
            return;
        }
        Admin admin = new Admin();
        admin.setName(name);
        admin.setPassword(password);
        admin.setStatus(Integer.parseInt(status));
        AdminDao adminDao = new AdminDao();
        String msg = "添加失败!";
        if (adminDao.add(admin)){
            msg = "success";
        }
        adminDao.closeConnection();
        response.getWriter().write(msg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
