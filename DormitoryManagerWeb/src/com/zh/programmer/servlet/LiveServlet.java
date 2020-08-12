package com.zh.programmer.servlet;

import com.alibaba.fastjson.JSONObject;
import com.zh.programmer.bean.Operator;
import com.zh.programmer.bean.Page;
import com.zh.programmer.bean.SearchProperty;
import com.zh.programmer.dao.BuildingDao;
import com.zh.programmer.dao.DormitoryDao;
import com.zh.programmer.dao.LiveDao;
import com.zh.programmer.domain.*;
import com.zh.programmer.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/liveServlet")
public class LiveServlet extends HttpServlet {

    private static final long serialVersionUID = 7321270008878595334L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if("toLiveListView".equals(method)){
            request.getRequestDispatcher("view/liveList.jsp").forward(request,response);
        }
        if("AddLive".equals(method)){
            addLive(request,response);
        }
        if("LiveList".equals(method)){
            liveList(request,response);
        }
        if("EditLive".equals(method)){
            editLive(request,response);
        }
        if("DeleteLive".equals(method)){
            deleteLive(request,response);
        }
    }

    private void deleteLive(HttpServletRequest request, HttpServletResponse response) {
        String[] ids = request.getParameterValues("ids[]");
        String[] dormitoryIds = request.getParameterValues("dormitoryIds[]");
        LiveDao liveDao = new LiveDao();
        DormitoryDao dormitoryDao = new DormitoryDao();
        String msg = "删除失败 ";
        response.setCharacterEncoding("utf-8");
        if (liveDao.delete(ids)){
            for (String dormitoryId : dormitoryIds) {
                dormitoryDao.updateLivedNumber(Integer.parseInt(dormitoryId),-1);
            }
            msg = "success";
        }
        liveDao.closeConnection();
        dormitoryDao.closeConnection();
        try {
            response.getWriter().write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editLive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = 0;
        int dormitoryId = 0;
        int oldDormitoryId = 0;
        int id = 0;
        String msg = "success";
        response.setCharacterEncoding("utf-8");
        try {
            studentId = Integer.parseInt(request.getParameter("studentId"));
            dormitoryId = Integer.parseInt(request.getParameter("dormitoryId"));
            oldDormitoryId = Integer.parseInt(request.getParameter("oldDormitoryId"));
            id = Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            msg = "所选学生信息或宿舍信息有误！";
        }
        DormitoryDao dormitoryDao = new DormitoryDao();
        if(dormitoryDao.isFull(dormitoryId)){
            msg = "该宿舍已经住满，请更换宿舍！";
            response.getWriter().write(msg);
            return;
        }
        Live live = new Live();
        live.setDormitoryId(dormitoryId);
        live.setStudentId(studentId);
        live.setLiveDate(new Date(System.currentTimeMillis()));
        live.setId(id);
        LiveDao liveDao = new LiveDao();
//		if(liveDao.isLived(studentId)){
//			msg = "该学生已经办理住宿，请勿重复入住！";
//			response.getWriter().write(msg);
//			return;
//		}
        if(!liveDao.update(live)){
            msg = "调整失败！";
        }
        liveDao.closeConnection();
        if(!dormitoryDao.updateLivedNumber(dormitoryId, 1)&&!dormitoryDao.updateLivedNumber(oldDormitoryId, -1)){
            msg = "更新宿舍信息失败！";
        }
        if(!dormitoryDao.updateLivedNumber(oldDormitoryId, -1)){
            msg = "更新宿舍信息失败！";
        }
        dormitoryDao.closeConnection();
        response.getWriter().write(msg);
    }

    private void liveList(HttpServletRequest request, HttpServletResponse response) {
        int pageNumber = Integer.parseInt(request.getParameter("page"));
        int pageSize = Integer.parseInt(request.getParameter("rows"));
        String studentId = request.getParameter("studentId");
        String dormitoryId = request.getParameter("dormitoryId");

        Map<String, Object> ret = new HashMap<String, Object>();
        LiveDao liveDao = new LiveDao();
        Page<Live> page = new Page<Live>(pageNumber, pageSize);
        if(!StringUtil.isEmpty(studentId)){
            page.getSearchProperties().add(new SearchProperty("student_id", studentId, Operator.EQ));
        }
        if(!StringUtil.isEmpty(dormitoryId)){
            page.getSearchProperties().add(new SearchProperty("dormitory_id", dormitoryId, Operator.EQ));
        }
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
            DormitoryDao dormitoryDao = new DormitoryDao();
            Page<Dormitory> dormitoryPage = new Page<>(1, 10);
            dormitoryPage.getSearchProperties().add(new SearchProperty("building_id", buildPage.getConten().get(0).getId(), Operator.EQ));
            dormitoryPage = dormitoryDao.findAll(dormitoryPage);
            dormitoryDao.closeConnection();
            List<Dormitory> dormitoryList = dormitoryPage.getConten();
            String dormitoryIds = "";
            for(Dormitory dormitory : dormitoryList){
                dormitoryIds += dormitory.getId() + ",";
            }
            dormitoryIds = dormitoryIds.substring(0,dormitoryIds.length()-1);
            page.getSearchProperties().add(new SearchProperty("dormitory_id",dormitoryIds , Operator.IN));
        }else if(userType == 2){
            //学生，只能查看自己的住宿信息
            Student loginedStudent = (Student)request.getSession().getAttribute("user");
            page.getSearchProperties().add(new SearchProperty("student_id", loginedStudent.getId(), Operator.EQ));
        }
        Page<Live> findList = liveDao.findAll(page);
        ret.put("rows", findList.getConten());
        ret.put("total", findList.getTotal());
        liveDao.closeConnection();
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONObject.toJSONString(ret));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addLive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int studentId = 0;
        int dormitoryId = 0;
        String msg = "success";
        try {
            studentId = Integer.parseInt(request.getParameter("studentId"));
            dormitoryId = Integer.parseInt(request.getParameter("dormitoryId"));
        }catch (Exception e){
            msg = "所选学生信息或宿舍信息有误！";
        }
        DormitoryDao dormitoryDao = new DormitoryDao();
        if (dormitoryDao.isFull(dormitoryId)){
            msg = "该宿舍已经住满，请更换宿舍！";
            response.getWriter().write(msg);
            return;
        }
        Live live = new Live();
        live.setDormitoryId(dormitoryId);
        live.setStudentId(studentId);
        live.setLiveDate(new Date(System.currentTimeMillis()));
        LiveDao liveDao = new LiveDao();
        if (liveDao.isLived(studentId)){
            msg = "该学生已经办理住宿，请勿重复入住！";
            response.getWriter().write(msg);
            return;
        }
        if (!liveDao.add(live)){
            msg = "添加失败";
        }
        liveDao.closeConnection();
        if (!dormitoryDao.updateLivedNumber(dormitoryId,1)){
            msg = "更新宿舍信息失败！";
        }
        dormitoryDao.closeConnection();
        response.getWriter().write(msg);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
