package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.BuildingDao;
import com.zh.programmer.dao.DormitoryDao;
import com.zh.programmer.domain.Building;
import com.zh.programmer.domain.Dormitory;
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

@WebServlet("/dormitoryServlet")
public class DormitoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1603127161790292584L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String method = request.getParameter("method");
        if("toDormitoryListView".equals(method)){
            request.getRequestDispatcher("view/dormitoryList.jsp").forward(request,response);
        }
        if("AddDormitory".equals(method)){
            addDormitory(request,response);
        }
        if("DormitoryList".equals(method)){
            getDormitoryList(request,response);
        }
        if("EditDormitory".equals(method)){
            editDormitory(request,response);
        }
        if("DeleteDormitory".equals(method)){
            deleteDormitory(request,response);
        }
    }

    private void deleteDormitory(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        DormitoryDao dormitoryDao = new DormitoryDao();
        String msg = "删除失败";
        if(dormitoryDao.delete(ids)){
            msg = "success";
        }
        dormitoryDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editDormitory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sn = request.getParameter("sn");
        String floor = request.getParameter("floor");
        int buildingId = 0;
        int maxNumber = 0;
        int id = 0;
        response.setCharacterEncoding("utf-8");
        try {
            buildingId = Integer.parseInt(request.getParameter("buildingId"));
            maxNumber = Integer.parseInt(request.getParameter("maxNumber"));
            id = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.getWriter().write("选择的宿管不正确!");
            return;
        }
        response.setCharacterEncoding("utf-8");
        if(StringUtil.isEmpty(sn)){
            response.getWriter().write("编号不能为空!");
            return;
        }
        if(StringUtil.isEmpty(floor)){
            response.getWriter().write("所属楼层不能为空!");
            return;
        }
        Dormitory dormitory = new Dormitory();
        dormitory.setSn(sn);
        dormitory.setBuildingId(buildingId);
        dormitory.setFloor(floor);
        dormitory.setMaxNumber(maxNumber);
        dormitory.setId(id);
        DormitoryDao dormitoryDao = new DormitoryDao();
        String msg = "修改失败!";
        if(dormitoryDao.update(dormitory)){
            msg = "success";
        }
        dormitoryDao.closeConnection();
        response.getWriter().write(msg);
    }

    private void getDormitoryList(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter("from");
        //如果来自下拉框查询
        if("combox".equals(from)){
            returnByCombox(request,response);
            return;
        }
        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String sn = request.getParameter("sn");
        String buildingId = request.getParameter("buildingId");
        Map<String, Object> ret = new HashMap<String, Object>();
        DormitoryDao dormitoryDao = new DormitoryDao();
        Page<Dormitory> page = new Page<Dormitory>(pageNumber, pageSize);
        if(!StringUtil.isEmpty(sn)){
            page.getSearchProperties().add(new SearchProperty("sn", sn, Operator.EQ));
        }

        if(!StringUtil.isEmpty(buildingId)){
            page.getSearchProperties().add(new SearchProperty("building_id", buildingId, Operator.EQ));
        }

        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            BuildingDao buildingDao = new BuildingDao();
            Page<Building> buildPage = new Page<Building>(1, 10);
            buildPage.getSearchProperties().add(new SearchProperty("dormitory_manager_id", loginedDormitoryManager.getId(), Operator.EQ));
            buildPage = buildingDao.findAll(buildPage);
            buildingDao.closeConnection();
            page.getSearchProperties().add(new SearchProperty("building_id", buildPage.getConten().get(0).getId(), Operator.EQ));
        }
        Page<Dormitory> findList = dormitoryDao.findAll(page);
        ret.put("rows", findList.getConten());
        ret.put("total", findList.getTotal());
        dormitoryDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.fromObject(ret).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnByCombox(HttpServletRequest request, HttpServletResponse response) {

        DormitoryDao dormitoryDao = new DormitoryDao();
        Page<Dormitory> page = new Page<>(1, 9999);
        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            BuildingDao buildingDao = new BuildingDao();
            Page<Building> buildPage = new Page<>(1, 10);
            buildPage.getSearchProperties().add(new SearchProperty("dormitory_manager_id", loginedDormitoryManager.getId(), Operator.EQ));
            buildPage = buildingDao.findAll(buildPage);
            buildingDao.closeConnection();
            page.getSearchProperties().add(new SearchProperty("building_id", buildPage.getConten().get(0).getId(), Operator.EQ));
        }
        page = dormitoryDao.findAll(page);
        dormitoryDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONArray.fromObject(page.getConten()).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDormitory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sn = request.getParameter("sn");
        String floor = request.getParameter("floor");
        int buildingId = 0;
        int maxNumber = 0;
        response.setCharacterEncoding("utf-8");
        try {
            buildingId = Integer.parseInt(request.getParameter("buildingId"));
            maxNumber = Integer.parseInt(request.getParameter("maxNumber"));
        } catch (Exception e) {
            response.getWriter().write("选择的宿管不正确!");
            return;
        }
        response.setCharacterEncoding("utf-8");
        if(StringUtil.isEmpty(sn)){
            response.getWriter().write("编号不能为空!");
            return;
        }
        if(StringUtil.isEmpty(floor)){
            response.getWriter().write("所属楼层不能为空!");
            return;
        }
        Dormitory dormitory = new Dormitory();
        dormitory.setSn(sn);
        dormitory.setBuildingId(buildingId);
        dormitory.setFloor(floor);
        dormitory.setMaxNumber(maxNumber);
        DormitoryDao dormitoryDao = new DormitoryDao();
        String msg = "添加失败!";
        if(dormitoryDao.add(dormitory)){
            msg = "success";
        }
        dormitoryDao.closeConnection();
        response.getWriter().write(msg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
