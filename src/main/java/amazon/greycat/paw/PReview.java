package amazon.greycat.paw;

import amazon.greycat.api.Review;
import greycat.Task;
import paw.tokeniser.Tokenizer;

import static greycat.Tasks.newTask;
import static paw.greycat.tasks.TokenizationTasks.setTypeOfToken;
import static paw.greycat.tasks.TokenizedRelationTasks.updateOrCreateTokenizeRelationFromString;

@SuppressWarnings("Duplicates")
public class PReview extends Review {

    private final Tokenizer tokenizer;

    public PReview() {
        this.user = new PUser();
        this.tokenizer = ((PUser) user).tokenizer;
    }

    @Override
    protected Task handleSummaryAndText(String summary, String text) {
        return newTask()
                .defineAsVar("reviewNode")
                .inject(tokenizer)
                .defineAsVar("tokenizer")


                .pipe(setTypeOfToken("tokenizer", "summary"))
                .pipe(updateOrCreateTokenizeRelationFromString("tokenizer", "reviewNode", summary, "summary"))

                .pipe(setTypeOfToken("tokenizer", "text"))
                .pipe(updateOrCreateTokenizeRelationFromString("tokenizer", "reviewNode", text, "text"));
    }
}