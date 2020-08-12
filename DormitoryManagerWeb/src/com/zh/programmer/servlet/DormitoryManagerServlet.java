package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.DormitoryManagerDao;
import com.zh.programmer.domain.DormitoryManager;
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

@WebServlet("/dormitoryManagerServlet")
public class DormitoryManagerServlet extends HttpServlet {

    private static final long serialVersionUID = 2523658662763733031L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if("toDormitoryManagerListView".equals(method)){
            request.getRequestDispatcher("view/dormitoryManagerList.jsp").forward(request,response);
        }
        if("AddDormitoryManager".equals(method)){
            addDormitoryManager(request,response);
        }
        if("DormitoryManagerList".equals(method)){
            getDormitoryManagerList(request,response);
        }
        if("EditDormitoryManager".equals(method)){
            editEditDormitoryManager(request,response);
        }
        if("DeleteDormitoryManager".equals(method)){
            deleteDeleteDormitoryManager(request,response);
        }
    }

    private void deleteDeleteDormitoryManager(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
        String msg = "";
        if (dormitoryManagerDao.delete(ids)){
            msg = "success";
        }
        dormitoryManagerDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editEditDormitoryManager(HttpServletRequest request, HttpServletResponse response) {

        int id = StringUtil.isEmpty(request.getParameter("id")) ? 0 : Integer.parseInt(request.getParameter("id"));
        String sn = request.getParameter("sn");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String sex = request.getParameter("sex");
        DormitoryManager dormitoryManager = new DormitoryManager();
        dormitoryManager.setId(id);
        dormitoryManager.setName(name);
        dormitoryManager.setSn(sn);
        dormitoryManager.setPassword(password);
        dormitoryManager.setSex(sex);
        DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
        String msg = "";
        if (dormitoryManagerDao.update(dormitoryManager)){
            msg = "success";
        }
        dormitoryManagerDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getDormitoryManagerList(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter("from");
        //如果来自下拉框查询
        if ("combox".equals(from)){
            returnByCombox(request,response);
            return;
        }

        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String name = request.getParameter("name");

        if(StringUtil.isEmpty(name)){
            name = "";
        }
        Map<String, Object> ret = new HashMap<String, Object>();
        DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
        Page<DormitoryManager> page = new Page<DormitoryManager>(pageNumber, pageSize);
        page.getSearchProperties().add(new SearchProperty("name", "%"+name+"%", Operator.LIKE));
        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("id", loginedDormitoryManager.getId(), Operator.EQ));
        }
        Page<DormitoryManager> findList = dormitoryManagerDao.findAll(page);
        ret.put("rows", findList.getConten());
        ret.put("total", findList.getTotal());
        dormitoryManagerDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.fromObject(ret).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnByCombox(HttpServletRequest request, HttpServletResponse response) {

        DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
        Page<DormitoryManager> page = new Page<>(1, 9999);
        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("id", loginedDormitoryManager.getId(), Operator.EQ));
        }
        page = dormitoryManagerDao.findAll(page);
        dormitoryManagerDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONArray.fromObject(page.getConten()).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDormitoryManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        DormitoryManager dormitoryManager = new DormitoryManager();
        dormitoryManager.setName(name);
        dormitoryManager.setPassword(password);
        dormitoryManager.setSex(sex);
        dormitoryManager.setSn(StringUtil.generateSn("DM",""));

        DormitoryManagerDao dormitoryManagerDao = new DormitoryManagerDao();
        String msg = "添加失败";
        if (dormitoryManagerDao.add(dormitoryManager)){
            msg = "success";
        }
        dormitoryManagerDao.closeConnection();
        response.getWriter().write(msg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
