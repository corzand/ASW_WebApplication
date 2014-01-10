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
        <div class="container horizontal-box fill-box-pack">
            <div class="categories fixed-box-pack">
                <div class="toggle-arrow">
                    <a class="arrow arrow-left" data-bind="click: actions.toggleCategories "></a>
                </div>
                <div class="categories-content">
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
                    <div class="categories-filter">                    
                        <div><h2>Categorie</h2></div>
                        <div data-bind="foreach : Categories">
                            <div class="row">
                                <div class="checkbox">
                                    <input type="checkbox" data-bind="attr : { name : title, id : 'cat_'+id() }, checked : state"/><label data-bind="attr: { for : 'cat_'+id()} "></label>
                                </div>
                                <span data-bind="text : title"></span>
                            </div>
                        </div> 
                    </div>
                </div>
            </div>
            <div class="tasks fill-box-pack vertical-box">                
                <div class='fixed-box-pack' data-bind="with : NewTask">
                    <div class="add-bar">
                        <h1>Nuovo Task</h1>
                        <div class="row">
                            <div class="cell label">Titolo</div>
                            <div class="cell"><input type="text" placeholder="Inserire titolo..." data-bind="value : title" /></div>                            
                            <div class="cell label">Data</div>
                            <div class="cell"><input type='text' id='fastAddDate' /><!-- Manual binding --></div>  
                        </div>
                        <div class="row buttons">
                            <a class="button edit-button" data-bind="click : $root.actions.edit"></a>
                            <a class="button" data-bind="click : $root.actions.addFast">Aggiungi</a>
                        </div>
                    </div>
                </div>          
                <div class='days-list fill-box-pack vertical-box'>
                    <div class="filters fixed-box-pack toolbar">
                        <div>
                            <a class="arrow arrow-down" data-bind="click: actions.toggleFilters "></a>
                        </div>
                        <div class="filters-content hidden">
                            <div class="table-row">
                                <div class="cell">
                                    <span>Data Inizio</span>
                                </div>
                                <div class="cell">
                                    <span>Data Fine</span>
                                </div>    
                            </div>
                            <div class="table-row">
                                <div class="cell">
                                    <input type='text' id='startDate' /><!-- Manual binding -->
                                </div>
                                <div class="cell">
                                    <input type='text' id='endDate' /><!-- Manual binding -->
                                </div> 
                                <a class="button" data-bind="click : actions.search">Applica</a>  
                            </div>
                        </div>
                    </div>
                    <div class='fixed-box-pack users-bar horizontal-box' data-bind="foreach : Users">
                        <div class="fixed-box-pack">
                            <div class="draggable">
                                <img data-bind="attr : { src : picture, title : username, 'data-id' : id }" />
                            </div>
                        </div>
                    </div>
                    <div class="timeline horizontal-box fill-box-pack" data-bind="foreach : Days">
                        <div class="day fixed-box-pack vertical-box">
                            <h2 class="fixed-box-pack" data-bind="text: $root.utils.getDayHeader($data)"></h2>
                            <div class="task-list" class="fill-box-pack" data-bind="foreach : Tasks">
                                <div class="task" data-bind="visible: visible, attr : { 'data-id' : id}">
                                    <div class="assigned cell">
                                        <!-- ko if: assigned() -->
                                        <div class="dropped-user">
                                            <img data-bind=" attr : { src : AssignedUser().picture }" />
                                        </div>
                                        <!-- /ko -->
                                        <!-- ko if: !assigned() -->
                                        <div class="drop-user" title="Trascina un utente per assegnarlo al task"></div>
                                        <!-- /ko -->
                                    </div>
                                    <div class="task-title cell">
                                        <a data-bind="text : title, click : $root.actions.edit"></a>
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
