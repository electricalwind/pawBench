package amazon.greycat.paw;

import amazon.greycat.api.Review;
import greycat.Task;
import paw.graph.tasks.AddingContent;
import paw.tokenizer.UTFTokenizer;
import paw.tokenizer.token.Token;

import java.util.List;

import static greycat.Tasks.newTask;

@SuppressWarnings("Duplicates")
public class PReview extends Review {

    private final UTFTokenizer tokenizer;

    public PReview() {
        tokenizer = new UTFTokenizer();
    }

    @Override
    protected Task handleSummaryAndText(String summary, String text) {
        List<Token> summaryTokens = tokenizer.tokenize(summary);
        List<Token> textTokens = tokenizer.tokenize(text);
        return newTask().pipePar(
                AddingContent.addTokenizeContentToNode(summaryTokens,"summary","summary"),
                AddingContent.addTokenizeContentToNode(textTokens,"text","newReview")
        );
    }
}