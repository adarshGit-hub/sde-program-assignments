package org.apache.ofbiz.customermanagement

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.service.LocalDispatcher
import org.apache.ofbiz.service.ServiceUtil
import java.sql.Timestamp

class CustomerEvents {

    public static String createCustomerEvent(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin")

        Map serviceCtx = [
            emailAddress: request.getParameter("emailAddress"),
            firstName: request.getParameter("firstName"),
            lastName: request.getParameter("lastName"),
            contactNumber: request.getParameter("contactNumber"),
            postalAddress: request.getParameter("postalAddress"),
            city: request.getParameter("city"),
            postalCode: request.getParameter("postalCode"),
            stateProvinceGeoId: request.getParameter("stateProvinceGeoId"),
            userLogin: userLogin
        ]

        try {
            Map result = dispatcher.runSync("createCustomer", serviceCtx)
            if (ServiceUtil.isError(result)) {
                request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result))
                return "error"
            }
            request.setAttribute("_EVENT_MESSAGE_", "Customer created successfully with Party ID: " + result.partyId)
            return "success"
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage())
            return "error"
        }
    }

    public static String updateCustomerEvent(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin")

        Map serviceCtx = [
            emailAddress: request.getParameter("emailAddress"),
            contactNumber: request.getParameter("contactNumber"),
            postalAddress: request.getParameter("postalAddress"),
            city: request.getParameter("city"),
            postalCode: request.getParameter("postalCode"),
            stateProvinceGeoId: request.getParameter("stateProvinceGeoId"),
            userLogin: userLogin
        ]

        try {
            Map result = dispatcher.runSync("updateCustomer", serviceCtx)
            if (ServiceUtil.isError(result)) {
                request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result))
                return "error"
            }
            request.setAttribute("_EVENT_MESSAGE_", "Customer details updated successfully.")
            return "success"
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage())
            return "error"
        }
    }

    public static String createCustomerRelationshipEvent(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin")

        Map serviceCtx = [
            partyIdFrom: request.getParameter("partyIdFrom"),
            partyIdTo: request.getParameter("partyIdTo"),
            roleTypeIdFrom: request.getParameter("roleTypeIdFrom"),
            roleTypeIdTo: request.getParameter("roleTypeIdTo"),
            partyRelationshipTypeId: request.getParameter("partyRelationshipTypeId"),
            statusId: request.getParameter("statusId"),
            userLogin: userLogin
        ]

        try {
            Map result = dispatcher.runSync("createCustomerRelationship", serviceCtx)
            if (ServiceUtil.isError(result)) {
                request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result))
                return "error"
            }
            request.setAttribute("_EVENT_MESSAGE_", "Relationship created successfully.")
            return "success"
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage())
            return "error"
        }
    }

    public static String updateCustomerRelationshipEvent(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher")
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin")

        String fromDateStr = request.getParameter("fromDate")
        Timestamp fromDate = null
        if (fromDateStr) {
            try {
                fromDate = Timestamp.valueOf(fromDateStr)
            } catch (Exception e) {
                request.setAttribute("_ERROR_MESSAGE_", "Invalid fromDate format. Must be YYYY-MM-DD HH:MM:SS.SSS")
                return "error"
            }
        } else {
            request.setAttribute("_ERROR_MESSAGE_", "fromDate is required for updating relationships.")
            return "error"
        }

        String thruDateStr = request.getParameter("thruDate")
        Timestamp thruDate = null
        if (thruDateStr) {
            try {
                thruDate = Timestamp.valueOf(thruDateStr)
            } catch (Exception e) {}
        }

        Map serviceCtx = [
            partyIdFrom: request.getParameter("partyIdFrom"),
            partyIdTo: request.getParameter("partyIdTo"),
            roleTypeIdFrom: request.getParameter("roleTypeIdFrom"),
            roleTypeIdTo: request.getParameter("roleTypeIdTo"),
            partyRelationshipTypeId: request.getParameter("partyRelationshipTypeId"),
            fromDate: fromDate,
            thruDate: thruDate,
            statusId: request.getParameter("statusId"),
            userLogin: userLogin
        ]

        try {
            Map result = dispatcher.runSync("updateCustomerRelationship", serviceCtx)
            if (ServiceUtil.isError(result)) {
                request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result))
                return "error"
            }
            request.setAttribute("_EVENT_MESSAGE_", "Relationship updated successfully.")
            return "success"
        } catch (Exception e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage())
            return "error"
        }
    }
}
