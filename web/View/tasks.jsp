<%-- 
    Document   : index
    Created on : 20-dic-2013, 17.21.07
    Author     : Andrea
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Multi-User Task List</title>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-min.js"></script>
        <script src="/scripts/utility.js"></script>
        <script src="/scripts/application.js"></script>
        <script src="/scripts/tasks.js"></script>
        <link rel="stylesheet" href="style/jquery-ui-1.10.3.custom.css" />
        <link href="/style/style.css" rel="stylesheet" type="text/css">
    </head>
    <body> 
		<script type="text/javascript">
			tasksInitialData = JSON.parse('<%= qualcosa %>');
		</script>
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="container">
            <div class="categories">
                <div class="private-filter">
                    <div class="row"><input type="radio" name="private" value="true" />I miei task</div>
                    <div class="row"><input type="radio" name="private" value="false" />Tutti i task</div>                    
                </div>
                <div class="categories-filter">
                    <ul data-bind="foreach : Categories">
                        <li>
                            <input type="checkbox" data-bind="attr : { name : title }, value : id, checked : state"/>
                        </li>
                    </ul> 
                </div>
            </div>
            <div class="task">
                <div class='users-bar' data-bind="foreach : Users">
                    <div><img data-bind="attr : { src : picture, title : username, 'data-id' : id }" /></div>
                </div>
                <div class='add-bar'>
                    <div>
                        <div>
                            <span>Nuovo task</span>
                            <input type="text" data-bind="value : CurrentTask.title" />
                        </div>
                        <div>
                            <input type='text' id='CurrentTaskDate' /><!-- Manual binding -->
                            <button data-bind="edit : editTask"></button>
                            <button data-bind="click : addTask">Fine</button>
                        </div>
                    </div>
                </div>
                <div class='task-list'>
                    <div class="filters">
                        <div>
                            <input type='text' id='StartDate' /><!-- Manual binding -->
                            <input type='text' id='EndDate' /><!-- Manual binding -->
                            <button data-bind="click : search">Applica</button>
                        </div>
                    </div>
                    <div class="timeline" data-bind="foreach : Days">
                        <div class="day" data-bind="foreach : Tasks">
                            <ul>
                                <li class="task">
                                    <div>
                                        <input type="checkbox" data-bind="value : id, checked : done, click : editTask" />
                                        <a href="#" data-bind="text : title, click : editTask"></a>
                                        <div data-bind="visible : assigned">
                                            <img data-bind=" attr : { src : assignedUserPicture }" />
                                        </div>
                                    </div>
                                </li>                                
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="edit-task-popup">
            <div>
                <div class="row">
                    <div>Titolo</div>
                    <div><input type="text" data-bind="value : title" /></div>
                </div>
                <div class="row">
                    <div>Descrizione</div>
                    <div><textarea data-bind="text : description" ></textarea></div>
                </div>
                <div class="row">
                    <div>Data</div>
                    <div><input type='text' id='TaskDate' /><!-- Manual binding --></div>
                </div>
                <div class="row">
                    <div>Fatto</div>
                    <div><input type="checkbox" data-bind="checked : done"/></div>
                </div>
                <div class="row">
                    <div>Privato</div>
                    <div><input type="checkbox" data-bind="checked : personal"/></div>
                </div>
                <div class="row">
                    <div>Categoria</div>
                    <div>
                        <select data-bind="foreach : Categories">
                            <option data-bind="text : title, attr : {'data-id' : id}"></option>
                        </select>
                    </div>
                </div>
                <div class="row">
                    <div>Utente assegnato</div>
                    <div>
                        <select data-bind="foreach : Users">
                            <option data-bind="text : username, attr : {'data-id' : id}"></option>
                        </select>
                    </div>
                </div>
                <div>
                    <div>Posizione</div>
                    <div>
                        <!-- TODO -->
                    </div>                    
                </div>
            </div>
        </div>
    </body>
</html>
