package amazon.greycat.api;

import greycat.Task;
import greycat.Type;
import paw.PawConstants;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.executeAtWorldAndTime;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static mylittleplugin.MyLittleActions.readUpdatedTimeVar;
import static paw.PawConstants.NODE_TYPE;

public class Product {

    private static Task initializeProducts() {
        return newTask()
                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_PRODUCTS_MAIN)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                ));
    }

    static Task retrieveProductsMainNode() {
        return newTask()
                .readGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE, NODE_TYPE_PRODUCTS_MAIN)
                .then(ifEmptyThen(
                        initializeProducts()
                ));
    }


    public static Task getOrCreateProduct(String productId) {
        String sub = (productId.length() > SIZE_OF_INDEX) ? productId.substring(0, SIZE_OF_INDEX) : productId;
        return newTask()
                .pipe(retrieveProductsMainNode())
                .defineAsVar("PRODUCTS")
                .traverse(RELATION_INDEX_PRODUCTS_TO_PRODUCTINDEX, NODE_NAME_INDEXING, sub)
                .then(ifEmptyThen(
                        newTask()
                                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                        newTask()
                                                .createNode()
                                                .timeSensitivity("-1", "0")
                                                .setAttribute(NODE_NAME_INDEXING, Type.STRING, sub)
                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_PRODUCTINDEX)
                                                .defineAsVar("pindex")
                                                .then(readUpdatedTimeVar("PRODUCTS"))
                                                .addVarToRelation(RELATION_INDEX_PRODUCTS_TO_PRODUCTINDEX, "pindex", NODE_NAME_INDEXING)
                                                .readVar("pindex")
                                ))
                ))
                .defineAsVar("pindex")
                .traverse(RELATION_INDEX_PRODUCTINDEX_TO_PRODUCT, PRODUCT_ID, productId)
                .then(ifEmptyThen(
                        newTask()
                                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                        newTask()
                                                .createNode()
                                                .setAttribute(PRODUCT_ID, Type.STRING, productId)
                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_PRODUCT)
                                                .timeSensitivity("-1", "0")
                                                .setAsVar("newProduct")
                                                .readVar("pindex")
                                                .addVarToRelation(RELATION_INDEX_PRODUCTINDEX_TO_PRODUCT, "newProduct", PRODUCT_ID)
                                                .readVar("newProduct")
                                ))
                ));
    }


}
