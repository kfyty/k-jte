<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
package com.kfyty.kjte;

public class HelloImpl implements com.kfyty.kjte.Hello {
    @Override
    public String hello(String hello) {
        return "hello" + hello;
    }
}
