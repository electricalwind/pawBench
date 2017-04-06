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

    private Task initializeUser() {
        return newTask()
                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_USER_MAIN)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                ));
    }

    protected Task retrieveUserMainNode() {
        return newTask()
                .readGlobalIndex(PawConstants.RELATION_INDEX_ENTRY_POINT, NODE_TYPE, NODE_TYPE_USER_MAIN)
                .then(ifEmptyThen(
                        initializeUser()
                ));
    }

    public Task getUserById(String id) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : id;
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(RELATION_INDEX_USERS_TO_USERINDEX, NODE_NAME_INDEXING, sub)
                .traverse(RELATION_INDEX_USERINDEX_TO_USER, USER_ID, id);
    }

    public abstract Task getUserByName(String name);



    public Task getOrCreateUser(String id, String name) {
        String sub = (id.length() > SIZE_OF_INDEX) ? id.substring(0, SIZE_OF_INDEX) : id;
        return newTask()
                .pipe(retrieveUserMainNode())
                .defineAsVar("USERS")
                .traverse(RELATION_INDEX_USERS_TO_USERINDEX, NODE_NAME_INDEXING, sub)
                .then(ifEmptyThen(
                        createIndexingUserNode(sub)
                ))
                .defineAsVar("uindex")
                .traverse(RELATION_INDEX_USERINDEX_TO_USER, USER_ID, id)
                .then(ifEmptyThen(
                        createUserNode(id, name)
                ));
    }


    private Task createIndexingUserNode(String sub) {
        return newTask()
                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                        newTask()
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(NODE_NAME_INDEXING, Type.STRING, sub)
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_USERINDEX)
                                .defineAsVar("uindex")
                                .then(readUpdatedTimeVar("USERS"))
                                .addVarToRelation(RELATION_INDEX_USERS_TO_USERINDEX, "uindex", NODE_NAME_INDEXING)
                                .readVar("uindex")
                ));
    }

    private Task createUserNode(String id, String name) {
        return newTask()
                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                        newTask()
                                .createNode()
                                .setAttribute(USER_ID, Type.STRING, id)
                                .pipe(handleName(name))
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_USER)
                                .timeSensitivity("-1", "0")
                                .setAsVar("newProduct")
                                .readVar("pindex")
                                .addVarToRelation(RELATION_INDEX_USERINDEX_TO_USER, "newProduct", USER_ID)
                                .readVar("newProduct")
                ));
    }


    protected abstract Task handleName(String name);

}
