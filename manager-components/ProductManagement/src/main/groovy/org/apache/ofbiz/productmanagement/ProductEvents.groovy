package org.apache.ofbiz.productmanagement

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.ofbiz.base.util.UtilHttp
import org.apache.ofbiz.service.LocalDispatcher
import org.apache.ofbiz.service.ServiceUtil

public static String createProductEvent(HttpServletRequest request, HttpServletResponse response) {
    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
    Map paramMap = UtilHttp.getParameterMap(request)
    
    Map serviceContext = [
        productId: paramMap.productId,
        internalName: paramMap.internalName,
        productName: paramMap.productName,
        description: paramMap.description,
        isVirtual: paramMap.isVirtual,
        isVariant: paramMap.isVariant,
        productCategoryId: paramMap.productCategoryId,
        price: paramMap.price ? new BigDecimal(paramMap.price) : null
    ]

    try {
        Map result = dispatcher.runSync("createProduct", serviceContext)
        if (ServiceUtil.isError(result)) {
            String errorMsg = ServiceUtil.getErrorMessage(result)
            request.setAttribute("_ERROR_MESSAGE_", errorMsg)
            return "error"
        }
        request.setAttribute("_EVENT_MESSAGE_", "Product created successfully.")
        return "success"
    } catch (Exception e) {
        request.setAttribute("_ERROR_MESSAGE_", "Error creating product: " + e.getMessage())
        return "error"
    }
}

public static String updateProductEvent(HttpServletRequest request, HttpServletResponse response) {
    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
    Map paramMap = UtilHttp.getParameterMap(request)

    Map serviceContext = [
        productId: paramMap.productId,
        internalName: paramMap.internalName,
        productName: paramMap.productName,
        description: paramMap.description,
        isVirtual: paramMap.isVirtual,
        isVariant: paramMap.isVariant,
        productCategoryId: paramMap.productCategoryId,
        price: paramMap.price ? new BigDecimal(paramMap.price) : null
    ]

    try {
        Map result = dispatcher.runSync("updateProduct", serviceContext)
        if (ServiceUtil.isError(result)) {
            String errorMsg = ServiceUtil.getErrorMessage(result)
            request.setAttribute("_ERROR_MESSAGE_", errorMsg)
            return "error"
        }
        request.setAttribute("_EVENT_MESSAGE_", "Product updated successfully.")
        return "success"
    } catch (Exception e) {
        request.setAttribute("_ERROR_MESSAGE_", "Error updating product: " + e.getMessage())
        return "error"
    }
}
