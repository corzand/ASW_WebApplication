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
                <%@ include file="/WEB-INF/jspf/title.jspf" %>      
                <div class="fixed-box-pack applet">
                    <applet type="application/x-java-applet">
                        <param name="code" value="LoginApplet.class" />
                        <param name="codebase" value="/applet/"/>   
                        <param name="archive" value = "ASW_Applet1.jar,ASW_Lib1.jar,org.json.jar" />
                        <param name="sessionId" value="<%= session.getId()%>" />
                    </applet> 
                </div>
            </div>
        </div>
    </body>
</html>
