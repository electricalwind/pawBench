package amazon.greycat.paw;

import amazon.greycat.api.Product;
import amazon.greycat.api.Review;
import amazon.greycat.api.User;
import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import greycat.struct.IntArray;
import paw.greycat.actions.Pawctions;
import paw.tokeniser.tokenisation.TokenizerType;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.ifEmptyThen;

@SuppressWarnings("Duplicates")
public class PReview extends Review {

    public PReview(){
        this.user = new PUser();
    }

    @Override
    protected Task handleSummaryAndText(String summary, String text) {
        return newTask()
                .defineAsVar("reviewNode")
                .then(Pawctions.createTokenizer("tokenizer", TokenizerType.ENGLISH, true))

                .inject(summary)
                .defineAsVar("toTokenize")
                .then(Pawctions.setTypOfToken("tokenizer", "summary"))

                .then(Pawctions.updateOrCreateTokenizeRelationFromVar("tokenizer", "reviewNode", "toTokenize", "summary"))

                .inject(text)
                .defineAsVar("toTokenize")
                .then(Pawctions.setTypOfToken("tokenizer", "text"))
                .then(Pawctions.updateOrCreateTokenizeRelationFromVar("tokenizer", "reviewNode", "toTokenize", "text"));
    }
}