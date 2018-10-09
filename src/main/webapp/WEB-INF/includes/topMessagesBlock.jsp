<fmt:setBundle basename="webproject.i18n.validation" var="langValidation"/>

<div class="row">
    <div class="col-md-12">
        <c:forEach items="${messages['topMessages']}" var="message">
            <div class="topMessages alert
                ${message.messageType == 'SUCCESS' ? 'alert-success' :
                    (message.messageType == 'INFO' ? 'alert-info' :
                    (message.messageType == 'ERROR' ? 'alert-danger' :
                    (message.messageType == 'WARNING' ? 'alert-warning' : '')))}" role="alert">
                <fmt:message key="${message.messageKey}" bundle="${langValidation}"/>
            </div>
        </c:forEach>
    </div>
</div>