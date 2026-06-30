import org.apache.ofbiz.service.ServiceUtil

Map serviceCtx = [:]
if (parameters.emailAddress) serviceCtx.emailAddress = parameters.emailAddress
if (parameters.firstName) serviceCtx.firstName = parameters.firstName
if (parameters.lastName) serviceCtx.lastName = parameters.lastName
if (parameters.contactNumber) serviceCtx.contactNumber = parameters.contactNumber
if (parameters.postalAddress) serviceCtx.postalAddress = parameters.postalAddress

int viewIndex = 0
try {
    if (parameters.VIEW_INDEX) {
        viewIndex = Integer.parseInt(parameters.VIEW_INDEX)
    } else if (parameters.viewIndex) {
        viewIndex = Integer.parseInt(parameters.viewIndex)
    }
} catch (Exception e) {}

int viewSize = 10
try {
    if (parameters.VIEW_SIZE) {
        viewSize = Integer.parseInt(parameters.VIEW_SIZE)
    } else if (parameters.viewSize) {
        viewSize = Integer.parseInt(parameters.viewSize)
    }
} catch (Exception e) {}

serviceCtx.viewIndex = viewIndex
serviceCtx.viewSize = viewSize

Map result = dispatcher.runSync("findCustomer", serviceCtx)
if (!ServiceUtil.isError(result)) {
    context.customerList = result.customerList ?: []
    context.listSize = result.listSize ?: 0
} else {
    context.customerList = []
    context.listSize = 0
}

context.viewIndex = viewIndex
context.viewSize = viewSize
