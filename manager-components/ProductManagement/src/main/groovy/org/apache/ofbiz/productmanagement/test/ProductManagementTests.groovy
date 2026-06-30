package org.apache.ofbiz.productmanagement.test

import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.service.ServiceUtil
import org.apache.ofbiz.service.testtools.OFBizTestCase

class ProductManagementTests extends OFBizTestCase {

    ProductManagementTests(String name) {
        super(name)
    }

    void testProductManagementFlow() {
        // 1. Create a Category and a Feature for testing
        GenericValue testCategory = delegator.makeValue("ProductCategory", [
            productCategoryId: "TEST_PM_CAT",
            productCategoryTypeId: "CATALOG_CATEGORY",
            categoryName: "Test PM Category"
        ])
        testCategory.create()

        GenericValue testFeature = delegator.makeValue("ProductFeature", [
            productFeatureId: "TEST_PM_FEAT",
            productFeatureTypeId: "COLOR",
            description: "Test PM Feature"
        ])
        testFeature.create()

        // 2. Test createProduct
        Map createCtx = [
            productId: "TEST_PM_PROD1",
            internalName: "TEST_PM_INTERNAL_NAME_1",
            productName: "Test PM Product 1",
            description: "First test product",
            isVirtual: "N",
            isVariant: "N",
            productCategoryId: "TEST_PM_CAT",
            price: new BigDecimal("25.99"),
            userLogin: userLogin
        ]
        Map createResult = dispatcher.runSync("createProduct", createCtx)
        assert ServiceUtil.isSuccess(createResult)
        assert createResult.productId == "TEST_PM_PROD1"

        // Verify product in database
        GenericValue prod1 = from("Product").where("productId", "TEST_PM_PROD1").queryOne()
        assert prod1 != null
        assert prod1.internalName == "TEST_PM_INTERNAL_NAME_1"

        // 3. Test uniqueness constraint on internalName in createProduct
        Map createDuplicateCtx = [
            productId: "TEST_PM_PROD2",
            internalName: "TEST_PM_INTERNAL_NAME_1", // Same internalName
            productName: "Test PM Product 2",
            userLogin: userLogin
        ]
        Map createDuplicateResult = dispatcher.runSync("createProduct", createDuplicateCtx)
        assert ServiceUtil.isError(createDuplicateResult)

        // 4. Test updateProduct
        Map updateCtx = [
            productId: "TEST_PM_PROD1",
            internalName: "TEST_PM_INTERNAL_NAME_1_UPD",
            productName: "Test PM Product 1 Updated",
            price: new BigDecimal("29.99"),
            userLogin: userLogin
        ]
        Map updateResult = dispatcher.runSync("updateProduct", updateCtx)
        assert ServiceUtil.isSuccess(updateResult)

        // Verify updates
        GenericValue prod1Updated = from("Product").where("productId", "TEST_PM_PROD1").queryOne()
        assert prod1Updated.internalName == "TEST_PM_INTERNAL_NAME_1_UPD"

        // Verify updated price in database
        GenericValue priceGv = from("ProductPrice")
            .where("productId", "TEST_PM_PROD1", "productPriceTypeId", "DEFAULT_PRICE")
            .filterByDate()
            .queryFirst()
        assert priceGv != null
        assert priceGv.price == new BigDecimal("29.99")

        // 5. Test findProduct
        Map findCtx = [
            productName: "Updated",
            productCategoryId: "TEST_PM_CAT",
            userLogin: userLogin
        ]
        Map findResult = dispatcher.runSync("findProduct", findCtx)
        assert ServiceUtil.isSuccess(findResult)
        List productList = findResult.productList
        assert productList != null
        assert productList.size() > 0
        assert productList.any { it.productId == "TEST_PM_PROD1" }

        // 6. Test virtual & variant association
        // Create Virtual Parent Product
        Map createVirtualCtx = [
            productId: "TEST_PM_VIRT",
            internalName: "TEST_PM_VIRT_INTERNAL",
            productName: "Test PM Virtual Parent",
            isVirtual: "Y",
            isVariant: "N",
            userLogin: userLogin
        ]
        Map createVirtualResult = dispatcher.runSync("createProduct", createVirtualCtx)
        assert ServiceUtil.isSuccess(createVirtualResult)

        // Create Variant Child Product
        Map createVariantCtx = [
            productId: "TEST_PM_VAR",
            internalName: "TEST_PM_VAR_INTERNAL",
            productName: "Test PM Variant Child",
            isVirtual: "N",
            isVariant: "Y",
            userLogin: userLogin
        ]
        Map createVariantResult = dispatcher.runSync("createProduct", createVariantCtx)
        assert ServiceUtil.isSuccess(createVariantResult)

        // Associate Variant to Virtual
        Map assocCtx = [
            virtualProductId: "TEST_PM_VIRT",
            variantProductId: "TEST_PM_VAR",
            userLogin: userLogin
        ]
        Map assocResult = dispatcher.runSync("assocProductToVirtual", assocCtx)
        assert ServiceUtil.isSuccess(assocResult)

        // Verify association in DB
        GenericValue assocGv = from("ProductAssoc")
            .where("productId", "TEST_PM_VIRT", "productIdTo", "TEST_PM_VAR", "productAssocTypeId", "PRODUCT_VARIANT")
            .filterByDate()
            .queryFirst()
        assert assocGv != null

        // 7. Test updateProductVariant (price & features)
        Map updateVarCtx = [
            productId: "TEST_PM_VAR",
            price: new BigDecimal("35.50"),
            productFeatureId: "TEST_PM_FEAT",
            userLogin: userLogin
        ]
        Map updateVarResult = dispatcher.runSync("updateProductVariant", updateVarCtx)
        assert ServiceUtil.isSuccess(updateVarResult)

        // Verify variant price in DB
        GenericValue varPriceGv = from("ProductPrice")
            .where("productId", "TEST_PM_VAR", "productPriceTypeId", "DEFAULT_PRICE")
            .filterByDate()
            .queryFirst()
        assert varPriceGv != null
        assert varPriceGv.price == new BigDecimal("35.50")

        // Verify feature applied to variant in DB
        GenericValue featApplGv = from("ProductFeatureAppl")
            .where("productId", "TEST_PM_VAR", "productFeatureId", "TEST_PM_FEAT", "productFeatureApplTypeId", "STANDARD_FEATURE")
            .filterByDate()
            .queryFirst()
        assert featApplGv != null
    }
}
