package amazon.greycat.paw;

import amazon.greycat.api.User;
import greycat.Task;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerFactory;
import paw.tokeniser.tokenisation.TokenizerType;

import static amazon.greycat.AmazonConstants.USER_NAME;
import static greycat.Tasks.newTask;
import static paw.greycat.tasks.TokenizationTasks.setTypeOfToken;
import static paw.greycat.tasks.TokenizedRelationTasks.updateOrCreateTokenizeRelationFromString;

public class PUser extends User {

    Tokenizer tokenizer = TokenizerFactory.getTokenizer(TokenizerType.UTF);

    PUser() {
        tokenizer.setKeepDelimiter(true);
    }

    @Override
    public Task getUserByName(String name) {
        throw new RuntimeException("not implemented Yet!");
    }

    @Override
    protected Task handleName(String name) {
        return newTask()
                .defineAsVar("newUserNode")
                .inject(tokenizer)
                .defineAsVar("tokenizer")
                .pipe(setTypeOfToken("tokenizer", "username"))
                .pipe(updateOrCreateTokenizeRelationFromString("tokenizer", "newUserNode", name, USER_NAME));
    }
}
