package amazon.greycat.regular;

import amazon.greycat.api.User;
import greycat.Task;
import greycat.Type;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Tasks.newTask;

public class RUser extends User {

    @Override
    public Task getUserByName(String name) {
        throw new RuntimeException("not implemented Yet!");
    }

    @Override
    protected Task handleName(String name) {
        return newTask().setAttribute(USER_NAME, Type.STRING, name);
    }
}
