package amazon.greycat.api;

import greycat.Task;
import greycat.Type;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.executeAtWorldAndTime;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static paw.PawConstants.NODE_TYPE;
import static paw.PawConstants.RELATION_INDEX_ENTRY_POINT;


public class Product {

    private static String PRODUCTS_VAR = "PRODUCTS";

    private static Task initializeProducts() {
        return newTask()
                .declareIndex(RELATION_INDEX_ENTRY_POINT,NODE_TYPE)
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_PRODUCTS_MAIN)
                                .timeSensitivity("-1", "0")
                                .setAsVar("prod")
                                .updateIndex(RELATION_INDEX_ENTRY_POINT)
                ))
                ;
    }

    static Task retrieveProductsMainNode() {
        return newTask()
                .readVar(PRODUCTS_VAR)
                .then(ifEmptyThen(
                        newTask()
                                .readIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE_PRODUCTS_MAIN)
                                .then(ifEmptyThen(
                                        initializeProducts()
                                )).defineAsGlobalVar(PRODUCTS_VAR)
                ));
    }


    public static Task getOrCreateProduct(String productId) {
        String sub = (productId.length() > SIZE_OF_INDEX) ? productId.substring(0, SIZE_OF_INDEX) : "lessthan";
        return newTask()
                .pipe(retrieveProductsMainNode())
                .traverse(sub, PRODUCT_ID, productId)
                .then(ifEmptyThen(
                        newTask()
                                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                                        newTask()
                                                .createNode()
                                                .setAttribute(PRODUCT_ID, Type.STRING, productId)
                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_PRODUCT)
                                                .timeSensitivity("-1", "0")
                                                .setAsVar("newProduct")
                                                .readVar(PRODUCTS_VAR)
                                                .declareLocalIndex(sub,PRODUCT_ID)
                                                .addVarTo(sub, "newProduct")
                                                .readVar("newProduct")
                                ))
                ));
    }


}
