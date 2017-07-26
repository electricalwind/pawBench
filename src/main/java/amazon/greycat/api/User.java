package amazon.greycat.api;

import greycat.Task;
import greycat.Type;
import paw.PawConstants;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.executeAtWorldAndTime;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static paw.PawConstants.NODE_TYPE;
import static paw.PawConstants.RELATION_INDEX_ENTRY_POINT;


public class User {

    private static String USERS_VAR = "USERS";

    private static Task initializeUser() {
        return newTask()
                .declareIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_USER_MAIN)
                                .timeSensitivity("-1", "0")
                                .setAsVar("use")
                                .updateIndex(RELATION_INDEX_ENTRY_POINT)

                ));
    }

    protected static Task retrieveUserMainNode() {
        return newTask()
                .readVar(USERS_VAR)
                .then(ifEmptyThen(
                        newTask()
                                .readIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE_USER_MAIN)
                                .then(ifEmptyThen(
                                        initializeUser()
                                )).defineAsGlobalVar(USERS_VAR)
                ))
                ;
    }

    public static Task getUserById(String id) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : "lessthan";
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(sub, USER_ID, id);
    }

    public static Task getOrCreateUser(String id, String name) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : "lessthan";
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(sub, USER_ID, id)
                .then(ifEmptyThen(
                        createUserNode(id, name, sub)
                ));
    }

    private static Task createUserNode(String id, String name, String sub) {
        return newTask()
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(USER_ID, Type.STRING, id)
                                .setAttribute(USER_NAME, Type.STRING, name)
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_USER)
                                .timeSensitivity("-1", "0")
                                .setAsVar("newUser")
                                .readVar(USERS_VAR)
                                .declareLocalIndex(sub, USER_ID)
                                .addVarTo(sub, "newUser")
                                .readVar("newUser")
                ));
    }


}
