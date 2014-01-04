

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/style/style.css" rel="stylesheet" type="text/css">
        <title>Login</title>
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/top.jspf" %>
        <div class="content">
            <div class="login">
                <%@ include file="/WEB-INF/jspf/title.jspf" %>              
                <applet class="applet" style="width: 200px; height: 50px" codebase="/applet/" code="LoginApplet.class" archive="ASW_Applet1.jar,ASW_Lib1.jar,org.json.jar" width=350 height=200>
                    <param name="sessionId" value="<%= session.getId()%>" />
                </applet>
            </div>
        </div>
    </body>
</html>
