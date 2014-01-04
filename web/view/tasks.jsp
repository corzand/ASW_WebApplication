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
        <link type="text/css" rel="stylesheet" href="/style/jquery-ui/jquery-ui-1.10.3.custom.css" />
        <link type="text/css" rel="stylesheet" href="/style/style.css" />
    </head>
    <body> 
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="content">
            <div class="container horizontal-box">
                <div class="categories fixed-box-pack">
                    <div class="private-filter">
                        <div class="row"><input type="radio" name="private" value="true" data-bind="checked : personal, checkedValue : true " />I miei task</div>
                        <div class="row"><input type="radio" name="private" value="false" data-bind="checked : personal, checkedValue : false " />Tutti i task</div>                    
                    </div>
                    <div class="categories-filter">
                        <ul data-bind="foreach : Categories">
                            <li>
                                <input type="checkbox" data-bind="attr : { name : title }, checked : state"/><span data-bind="text : title"></span>
                            </li>
                        </ul> 
                    </div>
                </div>
                <div class="tasks fill-box-pack vertical-box">

                    <div class='fixed-box-pack users-bar' data-bind="foreach : Users">
                        <div><img data-bind="attr : { src : picture, title : username, 'data-id' : id }" /></div>
                    </div>
                    <div class='fixed-box-pack add-bar' data-bind="with : NewTask">
                        <div>
                            <div>
                                <span>Nuovo task</span>
                                <input type="text" data-bind="value : title" />
                            </div>
                            <div>
                                <input type='text' id='fastAddDate' /><!-- Manual binding -->
                                <button data-bind="click : $root.actions.edit">Edit Icon</button>
                                <button data-bind="click : $root.actions.addFast">Fine</button>
                            </div>
                        </div>
                    </div>          
                    <div class='task-list fill-box-pack vertical-box'>
                        <div class="filters fixed-box-pack">
                            <div>
                                <input type='text' id='startDate' /><!-- Manual binding -->
                                <input type='text' id='endDate' /><!-- Manual binding -->
                                <button data-bind="click : actions.search">Applica</button>
                            </div>
                        </div>
                        <div class="timeline horizontal-box fill-box-pack" data-bind="foreach : Days">
                            <div class="day fixed-box-pack">
                                <span data-bind="text: day"></span>
                                <ul class="vertical-box" data-bind="foreach : Tasks">
                                    <li class="task fixed-box-pack" data-bind="visible: visible">
                                        <div>
                                            <input type="checkbox" data-bind="checked : done, click : $root.actions.markTask" />
                                            <a data-bind="text : title, click : $root.actions.edit"></a>
                                            <div data-bind="if : assigned">
                                                <img data-bind=" attr : { src : AssignedUser().picture }" />
                                            </div>
                                        </div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="edit-task-popup" style="display:none;">
            <div>
                <div class="row">
                    <div>Titolo</div>
                    <div><input type="text" data-bind="value : title" /></div>
                </div>
                <div class="row">
                    <div>Descrizione</div>
                    <div><textarea data-bind="value : description" ></textarea></div>
                </div>
                <div class="row">
                    <div>Data</div>
                    <div><input type='text' id='taskDate' /><!-- Manual binding --></div>
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
                        <select data-bind="options: Categories, optionsText: 'title', value: Category"></select>                        
                    </div>
                </div>
                <div class="row">
                    <div>Utente assegnato</div>
                    <div>
                        <select data-bind="options : Users, optionsText: 'username', value: AssignedUser, optionsCaption: 'Assegna...'"></select>
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
