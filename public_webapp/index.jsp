<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Login</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
    </head>
    <body class="vertical-box">
        <%@ include file="/WEB-INF/jspf/top.jspf" %>
        <div class="container horizontal-box fill-box-pack">
            <div class="content fill-box-pack vertical-box">
                 <%if (session.getAttribute("user") != null) {
                     response.sendRedirect(request.getContextPath() + "/application/tasks");
                 }%>
                <%@ include file="/WEB-INF/jspf/title.jspf" %> 
                <%
                    //String username = request.getParameter("username");
                    //String password = request.getParameter("password");
                    Cookie[] cookies = request.getCookies();
                    String username = "";
                    String password = "";
                    

                    if (cookies == null) {                        
                        System.out.println("nessun cookies");
                    } else {
                        for (int i = 0; i < cookies.length ; i++) {
                            String name = cookies[i].getName();
                            String value = cookies[i].getValue();
                            if (name.equals("username")) {
                                username = value;
                            }
                            if (name.equals("password")) {
                                password = value;
                            }
                        }
                    }
                %>
                
                <div class="fixed-box-pack">
                    <object type="application/x-java-applet" class="applet">
                        <param name="code" value="asw1009.LoginApplet.class" />
                        <param name="codebase" value="<%= request.getContextPath() %>/applet/"/>   
                        <param name="archive" value = "ASW_Applet1.jar,ASW_Lib1.jar,org.json.jar" />
                        <param name="sessionId" value="<%= session.getId()%>" />
                        <param name="context" value="<%= request.getContextPath() %>/" />
                        <param name="username" value="<%= username%>" />
                        <param name="password" value="<%= password%>" />
                    </object> 
                </div>
            </div>
        </div>
    </body>
</html>
