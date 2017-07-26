package amazon.greycat.regular;

import amazon.greycat.api.Review;
import greycat.Task;
import greycat.Type;

import static greycat.Tasks.newTask;

public class RReview extends Review {

    public RReview(){
    }


    @Override
    protected Task handleSummaryAndText(String summary, String text) {
        return newTask()
                .setAttribute("summary", Type.STRING, summary)
                .setAttribute("text", Type.STRING, text);
    }
}
