<%@include file="../../includes/header.jsp" %>
<fmt:setBundle basename="webproject.i18n.backend.user" var="langUser"/>
<fmt:setBundle basename="webproject.i18n.backend.general" var="general"/>
<fmt:setBundle basename="webproject.i18n.credential.credential" var="credential"/>

<div class="row">
    <div class="col-md-12 table-responsive">
        <h1 class="col-md-9"><fmt:message key="user.usersList.title" bundle="${langUser}"/></h1>
        <div class="col-md-3">
            <p><a href="${ApplicationResources.SIGN_UP_URI()}">
                <fmt:message key="signUp.link.label" bundle="${credential}"/></a></p>
        </div>
        <table class="table table-hover table-bordered table-striped">
            <thead>
            <tr>
                <th><fmt:message key="user.id.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.username.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.firstName.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.lastName.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.email.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.address.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.birthday.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.status.label" bundle="${langUser}"/></th>
                <th><fmt:message key="user.roles.label" bundle="${langUser}"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${allUsers}" var="user" varStatus="rowStatus">
                <tr class="${user.status == 'ACTIVE' ? 'success' : 'danger'}">
                    <td>${user.id}</td>
                    <td>${user.userName}</td>
                    <td>${user.firstNameAsString}</td>
                    <td>${user.lastNameAsString}</td>
                    <td>${user.email}</td>
                    <td>${user.addressAsString}</td>
                    <td><fmt:formatDate type="date" value="${user.birthdayAsDate}"/></td>
                    <td><fmt:message key="${user.status}" bundle="${langUser}"/></td>
                    <td>
                        <ul>
                            <c:forEach items="${user.rolesAsJavaCollection}" var="role" varStatus="rowCount">
                                <li><fmt:message key="${role}" bundle="${general}"/></li>
                            </c:forEach>
                        </ul>
                    </td>
                </tr>

            </c:forEach>
            </tbody>

        </table>

    </div>

</div>

<%@include file="../../includes/footer.jsp" %>