import org.apache.ofbiz.service.ServiceUtil

Map serviceCtx = [:]
if (parameters.productId) serviceCtx.productId = parameters.productId
if (parameters.productName) serviceCtx.productName = parameters.productName
if (parameters.productCategoryId) serviceCtx.productCategoryId = parameters.productCategoryId
if (parameters.productFeatureId) serviceCtx.productFeatureId = parameters.productFeatureId

if (parameters.price) {
    try {
        serviceCtx.price = new BigDecimal(parameters.price)
    } catch (NumberFormatException e) {
        // Ignore invalid price format
    }
}

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

Map result = dispatcher.runSync("findProduct", serviceCtx)
if (!ServiceUtil.isError(result)) {
    context.productList = result.productList ?: []
    context.listSize = result.listSize ?: 0
} else {
    context.productList = []
    context.listSize = 0
}

context.viewIndex = viewIndex
context.viewSize = viewSize
