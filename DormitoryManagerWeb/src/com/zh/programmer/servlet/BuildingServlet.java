package com.zh.programmer.servlet;

import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.BuildingDao;
import com.zh.programmer.dao.DormitoryManagerDao;
import com.zh.programmer.domain.Building;
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

@WebServlet("/buildingServlet")
public class BuildingServlet extends HttpServlet {

    private static final long serialVersionUID = -1450815261969968299L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String method = request.getParameter("method");
        if("toBuildingListView".equals(method)){
            request.getRequestDispatcher("view/buildingList.jsp").forward(request,response);
        }
        if("AddBuilding".equals(method)){
            addBuilding(request,response);
        }
        if("BuildingList".equals(method)){
            getBuildingList(request,response);
        }
        if("EditBuilding".equals(method)){
            editBuilding(request,response);
        }
        if("DeleteBuilding".equals(method)){
            deleteBuilding(request,response);
        }
    }

    private void deleteBuilding(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        BuildingDao buildingDao = new BuildingDao();
        String msg = "删除失败";
        if(buildingDao.delete(ids)){
            msg = "success";
        }
        buildingDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editBuilding(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String location = request.getParameter("location");
        int dormitoryManagerId = 0;
        int id = 0;
        try {
            dormitoryManagerId = Integer.parseInt(request.getParameter("dormitoryManagerId"));
            id = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            response.getWriter().write("选择的宿管不正确!");
            return;
        }
        response.setCharacterEncoding("utf-8");
        if(StringUtil.isEmpty(name)){
            response.getWriter().write("名称不能为空!");
            return;
        }
        if(StringUtil.isEmpty(location)){
            response.getWriter().write("所属位置不能为空!");
            return;
        }
        Building building = new Building();
        building.setName(name);
        building.setDormitoryManagerId(dormitoryManagerId);
        building.setLocation(location);
        building.setId(id);
        BuildingDao buildingDao = new BuildingDao();
        String msg = "修改失败!";
        if(buildingDao.update(building)){
            msg = "success";
        }
        buildingDao.closeConnection();
        response.getWriter().write(msg);
    }

    private void getBuildingList(HttpServletRequest request, HttpServletResponse response) {

        String from = request.getParameter("from");
        //如果来自下拉框查询
        if ("combox".equals(from)){
            returnByCombox(request,response);
            return;
        }

        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String name = request.getParameter("name");
        String dormitoryManagerId = request.getParameter("dormitoryManagerId");
        if(StringUtil.isEmpty(name)){
            name = "";
        }
        Map<String, Object> ret = new HashMap<String, Object>();
        BuildingDao buildingDao = new BuildingDao();
        Page<Building> page = new Page<>(pageNumber, pageSize);
        page.getSearchProperties().add(new SearchProperty("name", "%"+name+"%", Operator.LIKE));
        if(!StringUtil.isEmpty(dormitoryManagerId)){
            page.getSearchProperties().add(new SearchProperty("dormitory_manager_id", dormitoryManagerId, Operator.EQ));
        }
        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("dormitory_manager_id", loginedDormitoryManager.getId(), Operator.EQ));
        }
        Page<Building> findList = buildingDao.findAll(page);
        ret.put("rows", findList.getConten());
        ret.put("total", findList.getTotal());
        buildingDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.fromObject(ret).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnByCombox(HttpServletRequest request, HttpServletResponse response) {

        BuildingDao buildingDao = new BuildingDao();
        Page<Building> page = new Page<>(1, 9999);
        //判断当前用户是否是宿管
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        if(userType == 3){
            //如果是宿管，则只能查看他自己的信息
            DormitoryManager loginedDormitoryManager = (DormitoryManager)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("dormitory_manager_id", loginedDormitoryManager.getId(), Operator.EQ));
        }
        page = buildingDao.findAll(page);
        buildingDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONArray.fromObject(page.getConten()).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBuilding(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String location = request.getParameter("location");
        int dormitoryManagerId = 0;
        try {
            dormitoryManagerId = Integer.parseInt(request.getParameter("dormitoryManagerId"));
        } catch (Exception e) {
            response.getWriter().write("选择的宿管不正确!");
            return;
        }
        response.setCharacterEncoding("utf-8");
        if(StringUtil.isEmpty(name)){
            response.getWriter().write("名称不能为空!");
            return;
        }
        if(StringUtil.isEmpty(location)){
            response.getWriter().write("所属位置不能为空!");
            return;
        }
        Building building = new Building();
        building.setName(name);
        building.setDormitoryManagerId(dormitoryManagerId);
        building.setLocation(location);
        BuildingDao buildingDao = new BuildingDao();
        String msg = "添加失败!";
        if(buildingDao.add(building)){
            msg = "success";
        }
        buildingDao.closeConnection();
        response.getWriter().write(msg);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
