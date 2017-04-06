package amazon.greycat.paw;

import amazon.greycat.api.User;
import greycat.Task;
import greycat.Type;
import paw.greycat.actions.Pawctions;
import paw.tokeniser.tokenisation.TokenizerType;

import static amazon.greycat.AmazonConstants.*;
import static amazon.greycat.AmazonConstants.RELATION_INDEX_USERINDEX_TO_USER;
import static amazon.greycat.AmazonConstants.USER_ID;
import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.executeAtWorldAndTime;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static mylittleplugin.MyLittleActions.readUpdatedTimeVar;
import static paw.PawConstants.NODE_TYPE;

public class PUser extends User {
    @Override
    public Task getUserByName(String name) {
        throw new RuntimeException("not implemented Yet!");
    }

    @Override
    protected Task handleName(String name) {
        return newTask()
                .defineAsVar("newUserNode")
                .then(Pawctions.createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .inject(name)
                .defineAsVar("toTokenize")
                .then(Pawctions.setTypOfToken("tokenizer", "name"))

                .then(Pawctions.updateOrCreateTokenizeRelationFromVar("tokenizer", "newUserNode", "toTokenize", USER_NAME));
    }
}
