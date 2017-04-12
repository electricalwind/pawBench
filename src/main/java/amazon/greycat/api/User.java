package amazon.greycat.api;

import greycat.Task;
import greycat.Type;
import paw.PawConstants;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.*;
import static paw.PawConstants.NODE_TYPE;

public abstract class User {

    private static String USERS_VAR = "USERS";
    private Task initializeUser() {
        return newTask()
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_USER_MAIN)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                ));
    }

    protected Task retrieveUserMainNode() {
        return newTask()
                .readVar(USERS_VAR)
                .then(ifEmptyThen(
                        newTask()
                                .readGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE, NODE_TYPE_USER_MAIN)
                                .then(ifEmptyThen(
                                        initializeUser()
                                )).defineAsGlobalVar(USERS_VAR)
                ))
                ;
    }

    public Task getUserById(String id) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : "lessthan";
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(sub, USER_ID, id);
    }

    public abstract Task getUserByName(String name);



    public Task getOrCreateUser(String id, String name) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : "lessthan";
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(sub, USER_ID, id)
                .then(ifEmptyThen(
                        createUserNode(id, name, sub)
                ));
    }

    private Task createUserNode(String id, String name,String sub) {
        return newTask()
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(USER_ID, Type.STRING, id)
                                .pipe(handleName(name))
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_USER)
                                .timeSensitivity("-1", "0")
                                .setAsVar("newUser")
                                .readVar(USERS_VAR)
                                .addVarToRelation(sub, "newUser", USER_ID)
                                .readVar("newUser")
                ));
    }


    protected abstract Task handleName(String name);

}
