package org.apache.ofbiz.productmanagement

import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilProperties
import org.apache.ofbiz.base.util.UtilValidate
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityFindOptions

Map findProduct() {
    def conds = []
    def now = UtilDateTime.nowTimestamp()

    if (parameters.productId) {
        conds.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE, "%" + parameters.productId + "%"))
    }
    if (parameters.productName) {
        conds.add(EntityCondition.makeCondition(EntityOperator.OR,
            EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + parameters.productName + "%"),
            EntityCondition.makeCondition("internalName", EntityOperator.LIKE, "%" + parameters.productName + "%")
        ))
    }
    if (parameters.productCategoryId) {
        conds.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, parameters.productCategoryId))
        conds.add(EntityCondition.makeCondition(EntityOperator.AND,
            EntityCondition.makeCondition("categoryFromDate", EntityOperator.LESS_THAN_EQUAL_TO, now),
            EntityCondition.makeCondition(EntityOperator.OR,
                EntityCondition.makeCondition("categoryThruDate", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("categoryThruDate", EntityOperator.GREATER_THAN, now)
            )
        ))
    }
    if (parameters.productFeatureId) {
        conds.add(EntityCondition.makeCondition("productFeatureId", EntityOperator.EQUALS, parameters.productFeatureId))
        conds.add(EntityCondition.makeCondition(EntityOperator.AND,
            EntityCondition.makeCondition("featureFromDate", EntityOperator.LESS_THAN_EQUAL_TO, now),
            EntityCondition.makeCondition(EntityOperator.OR,
                EntityCondition.makeCondition("featureThruDate", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("featureThruDate", EntityOperator.GREATER_THAN, now)
            )
        ))
    }
    if (parameters.price) {
        BigDecimal priceVal = null
        if (parameters.price instanceof BigDecimal) {
            priceVal = (BigDecimal) parameters.price
        } else {
            priceVal = new BigDecimal(parameters.price.toString())
        }
        conds.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, priceVal))
        conds.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"))
        conds.add(EntityCondition.makeCondition(EntityOperator.AND,
            EntityCondition.makeCondition("priceFromDate", EntityOperator.LESS_THAN_EQUAL_TO, now),
            EntityCondition.makeCondition(EntityOperator.OR,
                EntityCondition.makeCondition("priceThruDate", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("priceThruDate", EntityOperator.GREATER_THAN, now)
            )
        ))
    }

    def mainCond = conds ? EntityCondition.makeCondition(conds, EntityOperator.AND) : null

    // Find distinct product IDs
    def findOptions = new EntityFindOptions()
    findOptions.setDistinct(true)
    def productIds = delegator.findList("FindProductView", mainCond, ["productId"] as Set, null, findOptions, false)
    def prodIdList = productIds.collect { it.productId }

    int listSize = prodIdList.size()
    int viewIndex = parameters.viewIndex != null ? Integer.parseInt(parameters.viewIndex.toString()) : 0
    int viewSize = parameters.viewSize != null ? Integer.parseInt(parameters.viewSize.toString()) : 10

    int lowIndex = viewIndex * viewSize
    int highIndex = (viewIndex + 1) * viewSize
    if (highIndex > listSize) {
        highIndex = listSize
    }

    def paginatedProductIds = (lowIndex < listSize) ? prodIdList.subList(lowIndex, highIndex) : []
    def productList = []

    for (def pId : paginatedProductIds) {
        def prod = from("Product").where("productId", pId).queryOne()
        if (prod) {
            def prodMap = [:]
            prodMap.putAll(prod.getAllFields())

            // Default Price
            def priceGv = from("ProductPrice")
                .where("productId", pId, "productPriceTypeId", "DEFAULT_PRICE")
                .filterByDate()
                .queryFirst()
            prodMap.price = priceGv ? priceGv.price : null

            // Categories
            def categoriesGvs = from("ProductCategoryMember")
                .where("productId", pId)
                .filterByDate()
                .queryList()
            def categoriesList = []
            for (def catMem : categoriesGvs) {
                def cat = from("ProductCategory").where("productCategoryId", catMem.productCategoryId).queryOne()
                if (cat) {
                    categoriesList.add([
                        productCategoryId: cat.productCategoryId,
                        categoryName: cat.categoryName ?: cat.description ?: cat.productCategoryId
                    ])
                }
            }
            prodMap.categories = categoriesList

            // Features
            def featuresGvs = from("ProductFeatureAndAppl")
                .where("productId", pId)
                .filterByDate()
                .queryList()
            def featuresList = []
            for (def feat : featuresGvs) {
                featuresList.add([
                    productFeatureId: feat.productFeatureId,
                    description: feat.description ?: feat.productFeatureId,
                    productFeatureTypeId: feat.productFeatureTypeId
                ])
            }
            prodMap.features = featuresList

            productList.add(prodMap)
        }
    }

    Map result = success()
    result.productList = productList
    result.listSize = listSize
    return result
}

Map createProduct() {
    // 1. Enforce unique Internal Name
    if (!parameters.internalName) {
        return error("Internal Name is required.")
    }
    
    def existingProduct = from("Product").where("internalName", parameters.internalName).queryFirst()
    if (existingProduct) {
        return error("Product Internal Name must be unique. A product with internal name '" + parameters.internalName + "' already exists.")
    }

    // 2. Determine productId
    String productId = parameters.productId
    if (!productId) {
        productId = delegator.getNextSeqId("Product")
    } else {
        // Validate database ID
        String checkIdError = UtilValidate.checkValidDatabaseId(productId)
        if (checkIdError) {
            return error(checkIdError)
        }
        // Check if productId already exists
        def dupProd = from("Product").where("productId", productId).queryOne()
        if (dupProd) {
            return error("Product with ID '" + productId + "' already exists.")
        }
    }

    // 3. Create Product GenericValue
    GenericValue newProduct = delegator.makeValue("Product")
    newProduct.productId = productId
    newProduct.internalName = parameters.internalName
    newProduct.productName = parameters.productName ?: parameters.internalName
    newProduct.description = parameters.description
    newProduct.isVirtual = parameters.isVirtual ?: "N"
    newProduct.isVariant = parameters.isVariant ?: "N"
    newProduct.productTypeId = "FINISHED_GOOD"
    newProduct.create()

    def now = UtilDateTime.nowTimestamp()

    // 4. Create ProductPrice if price is passed
    if (parameters.price != null) {
        BigDecimal priceVal = null
        if (parameters.price instanceof BigDecimal) {
            priceVal = (BigDecimal) parameters.price
        } else {
            priceVal = new BigDecimal(parameters.price.toString())
        }
        
        GenericValue newPrice = delegator.makeValue("ProductPrice")
        newPrice.productId = productId
        newPrice.productPriceTypeId = "DEFAULT_PRICE"
        newPrice.productPricePurposeId = "PURCHASE"
        newPrice.productStoreGroupId = "_NA_"
        newPrice.currencyUomId = "USD"
        newPrice.fromDate = now
        newPrice.price = priceVal
        newPrice.create()
    }

    // 5. Create ProductCategoryMember if productCategoryId is passed
    if (parameters.productCategoryId) {
        def cat = from("ProductCategory").where("productCategoryId", parameters.productCategoryId).queryOne()
        if (!cat) {
            return error("Product Category with ID '" + parameters.productCategoryId + "' does not exist.")
        }
        GenericValue newMember = delegator.makeValue("ProductCategoryMember")
        newMember.productId = productId
        newMember.productCategoryId = parameters.productCategoryId
        newMember.fromDate = now
        newMember.create()
    }

    Map result = success("Product created successfully.")
    result.productId = productId
    return result
}

Map updateProduct() {
    String productId = parameters.productId
    if (!productId) {
        return error("Product ID is required for updating.")
    }

    GenericValue product = from("Product").where("productId", productId).queryOne()
    if (!product) {
        return error("Product with ID '" + productId + "' not found.")
    }

    // 1. Enforce unique Internal Name if it's being updated
    if (parameters.internalName && parameters.internalName != product.internalName) {
        def existingProduct = from("Product").where("internalName", parameters.internalName).queryFirst()
        if (existingProduct) {
            return error("Product Internal Name must be unique. A product with internal name '" + parameters.internalName + "' already exists.")
        }
        product.internalName = parameters.internalName
    }

    if (parameters.productName != null) {
        product.productName = parameters.productName
    }
    if (parameters.description != null) {
        product.description = parameters.description
    }
    if (parameters.isVirtual != null) {
        product.isVirtual = parameters.isVirtual
    }
    if (parameters.isVariant != null) {
        product.isVariant = parameters.isVariant
    }
    product.store()

    def now = UtilDateTime.nowTimestamp()

    // 2. Update price if price is passed
    if (parameters.price != null) {
        BigDecimal priceVal = null
        if (parameters.price instanceof BigDecimal) {
            priceVal = (BigDecimal) parameters.price
        } else {
            priceVal = new BigDecimal(parameters.price.toString())
        }

        // Find existing DEFAULT_PRICE
        def priceGv = from("ProductPrice")
            .where("productId", productId, "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "productStoreGroupId", "_NA_", "currencyUomId", "USD")
            .filterByDate()
            .queryFirst()
        if (priceGv) {
            priceGv.price = priceVal
            priceGv.store()
        } else {
            // Create new
            GenericValue newPrice = delegator.makeValue("ProductPrice")
            newPrice.productId = productId
            newPrice.productPriceTypeId = "DEFAULT_PRICE"
            newPrice.productPricePurposeId = "PURCHASE"
            newPrice.productStoreGroupId = "_NA_"
            newPrice.currencyUomId = "USD"
            newPrice.fromDate = now
            newPrice.price = priceVal
            newPrice.create()
        }
    }

    // 3. Update category if productCategoryId is passed
    if (parameters.productCategoryId) {
        def cat = from("ProductCategory").where("productCategoryId", parameters.productCategoryId).queryOne()
        if (!cat) {
            return error("Product Category with ID '" + parameters.productCategoryId + "' does not exist.")
        }
        // Check if already a member of this category
        def existingMember = from("ProductCategoryMember")
            .where("productId", productId, "productCategoryId", parameters.productCategoryId)
            .filterByDate()
            .queryFirst()
        if (!existingMember) {
            // Expire all other categories first
            def activeMembers = from("ProductCategoryMember").where("productId", productId).filterByDate().queryList()
            for (def member : activeMembers) {
                member.thruDate = now
                member.store()
            }
            // Create new category membership
            GenericValue newMember = delegator.makeValue("ProductCategoryMember")
            newMember.productId = productId
            newMember.productCategoryId = parameters.productCategoryId
            newMember.fromDate = now
            newMember.create()
        }
    }

    return success("Product updated successfully.")
}

Map assocProductToVirtual() {
    String virtualProductId = parameters.virtualProductId
    String variantProductId = parameters.variantProductId

    if (!virtualProductId || !variantProductId) {
        return error("Both virtualProductId and variantProductId are required.")
    }

    def virtualProduct = from("Product").where("productId", virtualProductId).queryOne()
    if (!virtualProduct) {
        return error("Virtual product with ID '" + virtualProductId + "' not found.")
    }
    if (virtualProduct.isVirtual != "Y") {
        return error("Product with ID '" + virtualProductId + "' is not marked as Virtual.")
    }

    def variantProduct = from("Product").where("productId", variantProductId).queryOne()
    if (!variantProduct) {
        return error("Variant product with ID '" + variantProductId + "' not found.")
    }
    if (variantProduct.isVariant != "Y") {
        return error("Product with ID '" + variantProductId + "' is not marked as Variant.")
    }

    // Check if association already exists
    def now = UtilDateTime.nowTimestamp()
    def existingAssoc = from("ProductAssoc")
        .where("productId", virtualProductId, "productIdTo", variantProductId, "productAssocTypeId", "PRODUCT_VARIANT")
        .filterByDate()
        .queryFirst()
    if (existingAssoc) {
        return success("Variant product is already associated with the virtual product.")
    }

    GenericValue newAssoc = delegator.makeValue("ProductAssoc")
    newAssoc.productId = virtualProductId
    newAssoc.productIdTo = variantProductId
    newAssoc.productAssocTypeId = "PRODUCT_VARIANT"
    newAssoc.fromDate = now
    newAssoc.create()

    return success("Associated variant product '" + variantProductId + "' to virtual parent product '" + virtualProductId + "' successfully.")
}

Map updateProductVariant() {
    String productId = parameters.productId
    if (!productId) {
        return error("Product ID (variant product) is required.")
    }

    def variantProduct = from("Product").where("productId", productId).queryOne()
    if (!variantProduct) {
        return error("Variant product with ID '" + productId + "' not found.")
    }
    if (variantProduct.isVariant != "Y") {
        return error("Product with ID '" + productId + "' is not a variant product.")
    }

    def now = UtilDateTime.nowTimestamp()

    // 1. Update pricing
    if (parameters.price != null) {
        BigDecimal priceVal = null
        if (parameters.price instanceof BigDecimal) {
            priceVal = (BigDecimal) parameters.price
        } else {
            priceVal = new BigDecimal(parameters.price.toString())
        }

        def priceGv = from("ProductPrice")
            .where("productId", productId, "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "productStoreGroupId", "_NA_", "currencyUomId", "USD")
            .filterByDate()
            .queryFirst()
        if (priceGv) {
            priceGv.price = priceVal
            priceGv.store()
        } else {
            GenericValue newPrice = delegator.makeValue("ProductPrice")
            newPrice.productId = productId
            newPrice.productPriceTypeId = "DEFAULT_PRICE"
            newPrice.productPricePurposeId = "PURCHASE"
            newPrice.productStoreGroupId = "_NA_"
            newPrice.currencyUomId = "USD"
            newPrice.fromDate = now
            newPrice.price = priceVal
            newPrice.create()
        }
    }

    // 2. Adjust product features
    if (parameters.productFeatureId) {
        def existingAppl = from("ProductFeatureAppl")
            .where("productId", productId, "productFeatureId", parameters.productFeatureId)
            .filterByDate()
            .queryFirst()
        if (!existingAppl) {
            def feature = from("ProductFeature").where("productFeatureId", parameters.productFeatureId).queryOne()
            if (feature) {
                def activeAppls = from("ProductFeatureAndAppl")
                    .where("productId", productId, "productFeatureTypeId", feature.productFeatureTypeId)
                    .filterByDate()
                    .queryList()
                for (def appl : activeAppls) {
                    def writeableAppl = from("ProductFeatureAppl")
                        .where("productId", productId, "productFeatureId", appl.productFeatureId, "fromDate", appl.fromDate)
                        .queryOne()
                    if (writeableAppl) {
                        writeableAppl.thruDate = now
                        writeableAppl.store()
                    }
                }
            }
            GenericValue newAppl = delegator.makeValue("ProductFeatureAppl")
            newAppl.productId = productId
            newAppl.productFeatureId = parameters.productFeatureId
            newAppl.productFeatureApplTypeId = "STANDARD_FEATURE"
            newAppl.fromDate = now
            newAppl.create()
        }
    }

    return success("Updated product variant '" + productId + "' successfully.")
}
