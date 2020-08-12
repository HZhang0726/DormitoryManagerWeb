package com.zh.programmer.servlet;

import com.zh.programmer.util.CpachaUtil;
import com.zh.programmer.util.StringUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码生成类
 * @author zh
 *
 */
@WebServlet("/cpachaServlet")
public class CpachaServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if ("loginCpacha".equals(method)){
            getLoginCpacha(request,response);
        }
    }

    private void getLoginCpacha(HttpServletRequest request, HttpServletResponse response) {

        String vl = request.getParameter("vl");
        String fs = request.getParameter("fs");
        int vcodeLength = 4;
        int fontSize = 21;
        if(!StringUtil.isEmpty(vl)){
            vcodeLength = Integer.parseInt(vl);
        }
        if(!StringUtil.isEmpty(fs)){
            fontSize = Integer.parseInt(fs);
        }

        CpachaUtil cpachaUtil = new CpachaUtil();
        String generatorVCode = cpachaUtil.generatorVCode();

        //把生成的验证码放入session,用来登陆时验证
        request.getSession().setAttribute("loginCpacha",generatorVCode);
        BufferedImage generatorRotateVCodeImage = cpachaUtil.generatorRotateVCodeImage(generatorVCode, true);
        try {
            ImageIO.write(generatorRotateVCodeImage,"gif", response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
