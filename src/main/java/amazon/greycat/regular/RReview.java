package amazon.greycat.regular;

import amazon.greycat.api.Product;
import amazon.greycat.api.Review;
import amazon.greycat.api.User;
import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import greycat.struct.IntArray;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.ifEmptyThen;

public class RReview extends Review {

    public RReview(){
        this.user = new RUser();
    }


    @Override
    protected Task handleSummaryAndText(String summary, String text) {
        return newTask()
                .setAttribute("summary", Type.STRING, summary)
                .setAttribute("text", Type.STRING, text);
    }
}
