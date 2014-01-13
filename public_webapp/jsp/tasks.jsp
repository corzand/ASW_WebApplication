<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Multi-User Task List</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
        <script src="/scripts/tasks.js"></script>
    </head>
    <body class="vertical-box"> 
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <%@ include file="/WEB-INF/jspf/validation-dialog.jspf" %>
        <div class="container horizontal-box fill-box-pack">
            <div class="categories fixed-box-pack">
                <div class="toggle-arrow">
                    <a class="arrow arrow-left" data-bind="click: actions.toggleCategories "></a>
                </div>
                <div class="categories-content">
                    <h2>Filtri</h2>
                    <div class="private-filter">
                        <div class="row">
                            <div class="radiobutton" data-bind="css : { checked : personal() }">
                                <input id="private" type="radio" name="private" value="true" data-bind="checked : personal, checkedValue : true " />
                                <label for="private"></label>
                            </div>
                            <span>I miei tasks</span>
                        </div>
                        <div class="row">
                            <div class="radiobutton"  data-bind="css : { checked : !personal() }">
                                <input id="public" type="radio" name="private" value="false" data-bind="checked : personal, checkedValue : false " />
                                <label for="public"></label>
                            </div>
                            <span>Tutti i tasks</span>
                        </div>
                    </div>                    
                    <div class="todo-filter">                    
                        <div>
                            <div class="row">
                                <div class="checkbox">
                                    <input id='chk_todo' type="checkbox" data-bind="checked : todo"/><label for='chk_todo'></label>
                                </div>
                                <span>Da fare</span>
                            </div>
                            <div class="row">
                                <div class="checkbox">
                                    <input id='chk_done' type="checkbox" data-bind="checked : done"/><label for='chk_done'></label>
                                </div>
                                <span>Fatti</span>
                            </div>
                        </div> 
                    </div>
                    <div class="categories-filter">                    
                        <h2>Categorie</h2>
                        <div data-bind="foreach : Categories">
                            <div class="row">
                                <div class="checkbox">
                                    <input type="checkbox" data-bind="attr : { name : title, id : 'cat_'+id() }, checked : state"/><label data-bind="attr: { for : 'cat_'+id()}, style : { 'backgroundColor' : color } "></label>
                                </div>
                                <span data-bind="text : title"></span>
                            </div>
                        </div> 
                    </div>
                </div>
            </div>
            <div class="tasks fill-box-pack vertical-box">                
                <div class='fixed-box-pack' data-bind="with : NewTask">
                    <form id="add-bar-form" class="add-bar">
                        <h1>Nuovo Task</h1>
                        <div class="row">
                            <div class="cell label">Titolo</div>
                            <div class="cell"><input type="text" name="fastTitle" placeholder="Inserire titolo..." data-bind="value : title, valueUpdate: 'keypress'" /></div>                            
                            <div class="cell label">Data</div>
                            <div class="cell"><input type='text' id='fastAddDate'  disabled /><!-- Manual binding --></div>  
                            <div class="cell buttons">
                                <button class="button image-button edit-button" data-bind="click : $root.actions.edit"><span></span></button>
                                <button class="button" data-bind="click : $root.actions.addFast"><span>Aggiungi</span></button>
                            </div>                            
                        </div>
                    </form>
                </div>          
                <div class='days-list fill-box-pack vertical-box'>
                    <div class="filters fixed-box-pack toolbar">
                        <div>
                            <a class="arrow arrow-down" data-bind="click: actions.toggleFilters "></a>
                            <span class="header">Time-Line</span>
                        </div>
                        <div class="filters-content hidden">
                            <div class="table-row">
                                <div class="cell">
                                    <span class="right-label">Da</span><input type='text' id='startDate'  disabled /><!-- Manual binding -->
                                </div>
                                <div class="cell">
                                    <span class="right-label">A</span><input type='text' id='endDate'  disabled /><!-- Manual binding -->
                                </div> 
                                <button class="button" data-bind="click : actions.search"><span>Applica</span></button>  
                            </div>
                        </div>
                    </div>
                    <div class='fixed-box-pack users-bar horizontal-box' data-bind="foreach : Users">
                        <div class="fixed-box-pack">
                            <div class="draggable" data-bind="attr : { 'data-id' : id, title : username}">
                                <!-- ko if: picture() !== "" -->
                                <img data-bind="attr : { src : picture}" />
                                <!-- /ko -->
                                <!-- ko if: picture() === '' -->
                                <div class="username-initial" data-bind='text : username().charAt(0)'></div>
                                <!-- /ko -->
                            </div>
                        </div>
                    </div>
                    <div class="timeline horizontal-box fill-box-pack" data-bind="foreach : Days">
                        <div class="day fixed-box-pack vertical-box" data-bind="css : { 'today' : day.getTime() === $root.today.getTime() }">
                            <h2 class="fixed-box-pack" data-bind="text: $root.utils.getDayHeader($data)"></h2>
                            <div class="task-list" class="fill-box-pack" data-bind="foreach : Tasks">
                                <div class="task" data-bind="visible: visible, attr : { 'data-id' : id}, css: {'expired' : expired}">
                                    <div class="user cell" data-bind="css : { 'assigned' : assigned() }, style: { 'backgroundColor' : Category().color }">
                                        <!-- ko if: assigned() -->
                                        <div class="dropped-user">
                                            <!-- ko if: AssignedUser().picture() !== "" -->
                                            <img data-bind=" attr : { src : AssignedUser().picture }" />
                                            <!-- /ko -->
                                            <!-- ko if: AssignedUser().picture() === '' -->
                                            <div class="username-initial" data-bind='text : AssignedUser().username().charAt(0)'></div>
                                            <!-- /ko -->
                                        </div>
                                        <!-- /ko -->
                                        <!-- ko if: !assigned() -->
                                        <div class="drop-user" title="Trascina un utente per assegnarlo al task"></div>
                                        <!-- /ko -->
                                    </div>
                                    <div class="task-title cell" data-bind="css : { 'done' : done }, click : $root.actions.edit, attr: { title : description}">
                                        <a data-bind="text : title"></a>
                                    </div>
                                    <div  class="task-delay cell">
                                        <a class="delay-button" title="posticipa" data-bind="click: $root.actions.delay"></a>
                                    </div>
                                    <div class="task-check cell">
                                        <div class="checkbox">
                                            <input type="checkbox" data-bind="attr : { id : 'task_'+id() }, checked : done, click : $root.actions.markTask"/><label data-bind="attr: { for : 'task_'+id()} "></label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="edit-task-popup" style="display:none;">
            <form id="edit-task-form">
                <div class="table-row form-row">
                    <div class="cell right-label">Titolo</div>
                    <div class="cell"><input type="text" name="title" data-bind="value : title" /></div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Descrizione</div>
                    <div class="cell"><textarea data-bind="value : description" ></textarea></div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Data</div>
                    <div class="cell"><input type='text' id='taskDate' disabled /><!-- Manual binding --></div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Fatto</div>
                    <div class="cell">
                        <div class="checkbox">
                            <input id="chkDone" type="checkbox" data-bind="checked : done"/>
                            <label for="chkDone"></label>
                        </div>
                    </div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Privato</div>
                    <div class="cell">
                        <div class="checkbox">
                            <input id="chkPersonal" type="checkbox" data-bind="checked : personal"/>
                            <label for="chkPersonal"></label>
                        </div>
                    </div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Categoria</div>
                    <div class="cell">
                        <div class="select">
                            <select data-bind="options: Categories, optionsText: 'title', value: Category"></select>                        
                        </div>
                    </div>
                </div>
                <div class="table-row form-row">
                    <div class="cell right-label">Utente assegnato</div>
                    <div class="cell">
                        <div class="select">
                            <select data-bind="options : Users, optionsText: 'username', value: AssignedUser, optionsCaption: 'Assegna...'"></select>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </body>
</html>
