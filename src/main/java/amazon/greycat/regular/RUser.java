package amazon.greycat.regular;

import amazon.greycat.api.User;
import greycat.Task;
import greycat.Type;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Tasks.newTask;

public class RUser extends User{

    @Override
    public Task getUserByName(String name) {
        return newTask()
                .pipe(retrieveUserMainNode())
                .traverse(RELATION_INDEX_USERS_TO_USERINDEX)
                .map(
                        newTask()
                                .traverse(RELATION_INDEX_USERINDEX_TO_USER)
                                .selectWith(USER_NAME, name)
                )
                .flat();

    }

    @Override
    protected Task handleName(String name) {
        return newTask().setAttribute(USER_NAME, Type.STRING,name);
    }
}
