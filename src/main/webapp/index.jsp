<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>
<form method="post" action="product?action=import" enctype="multipart/form-data">
    ${error}
    Excel File <input type="file" name="excelFile">
    <br>
    <input type="submit" value="Import">
    <br>
    <c:if test="${productList != null}">
        <h3>Products</h3>
        <table cellpadding="2" cellspacing="2" border="1">
            <tr>
                <th>Id</th>
                <th>code</th>
                <th>name</th>
                <th>price</th>
                <th>quantity</th>
                <th>status</th>
                <th>createdAt</th>
                <th>updatedAt</th>
            </tr>
            <c:forEach var="product" items="${productList}">
                <tr>
                    <td>${product.id}</td>
                    <td>${product.code}</td>
                    <td>${product.name}</td>
                    <td>${product.price}</td>
                    <td>${product.quantity}</td>
                    <td>${product.status}</td>
                    <td>${product.createdAt}</td>
                    <td>${product.updatedAt}</td>
                </tr>
            </c:forEach>
        </table>
    </c:if>
</form>
</body>
</html>