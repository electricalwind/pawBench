package amazon.greycat.api;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import greycat.struct.IntArray;

import static amazon.greycat.AmazonConstants.*;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static paw.PawConstants.NODE_TYPE;

public abstract class Review {


    protected User user;

    public Task addReview(String pid,
                          String uid,
                          String profileName,
                          int helpfulnessIn,
                          int helpfulnessOut,
                          double score,
                          long time,
                          String summary,
                          String text) {
        return newTask()
                .travelInTime("" + time)
                .pipe(user.getOrCreateUser(uid, profileName))
                .defineAsVar("user")
                .thenDo(ctx -> {
                    ctx.setVariable("uid", "[" + ctx.resultAsNodes().get(0).id() + "]");
                    ctx.continueTask();
                })
                .pipe(Product.getOrCreateProduct(pid))
                .setAsVar("product")
                .traverse(RELATION_INDEX_PRODUCT_TO_REVIEW, REVIEW_USER, "{{uid}}")
                .then(ifEmptyThen(
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_REVIEW)
                                .thenDo(ctx -> {
                                    Node node = ctx.resultAsNodes().get(0);
                                    IntArray help = (IntArray) node.getOrCreate("helpfulness", Type.INT_ARRAY);
                                    help.initWith(new int[]{helpfulnessIn, helpfulnessOut});
                                    ctx.continueTask();
                                })
                                .setAttribute("score", Type.DOUBLE, "" + score)
                                .addVarToRelation(REVIEW_PRODUCT, "product")
                                .addVarToRelation(REVIEW_USER, "user")
                                .pipe(handleSummaryAndText(summary, text))
                                .defineAsVar("newReview")

                                .readVar("user")
                                .addVarToRelation(RELATION_INDEX_USER_TO_REVIEW, "newReview", REVIEW_PRODUCT)
                                .readVar("product")
                                .addVarToRelation(RELATION_INDEX_PRODUCT_TO_REVIEW, "newReview", REVIEW_USER)
                                .readVar("newReview"))
                );
    }

    public Action addReview(String pidV, String uidV, String profileNameV, String helpfulnessInV, String helpfulnessOutV, String scoreV, String timeV, String summaryV, String textV) {
        return new ActionAddReview(pidV, uidV, profileNameV, helpfulnessInV, helpfulnessOutV, scoreV, timeV, summaryV, textV);
    }

    public class ActionAddReview implements Action {

        private final String _pid;
        private final String _uid;
        private final String _profileName;
        private final String _helpfulnessIn;
        private final String _helpfulnessOut;
        private final String _score;
        private final String _time;
        private final String _summary;
        private final String _text;

        public ActionAddReview(String pid,
                               String uid,
                               String profileName,
                               String helpfulnessIn,
                               String helpfulnessOut,
                               String score,
                               String time,
                               String summary,
                               String text) {
            this._pid = pid;
            this._uid = uid;
            this._profileName = profileName;
            this._helpfulnessIn = helpfulnessIn;
            this._helpfulnessOut = helpfulnessOut;
            this._score = score;
            this._time = time;
            this._summary = summary;
            this._text = text;
        }

        @Override
        public void eval(TaskContext ctx) {
            ctx.result().free();
            final TaskResult newResult = ctx.newResult();
            String pid = (String) ctx.variable(ctx.template(_pid)).get(0);
            String uid = (String) ctx.variable(ctx.template(_uid)).get(0);
            String profileName = (String) ctx.variable(ctx.template(_profileName)).get(0);
            int helpfulnessIn = Integer.parseInt((String) ctx.variable(ctx.template(_helpfulnessIn)).get(0));
            int helpfulnessOut = Integer.parseInt((String) ctx.variable(ctx.template(_helpfulnessOut)).get(0));
            double score = Double.parseDouble((String) ctx.variable(ctx.template(_score)).get(0));
            long time = Long.parseLong((String) ctx.variable(ctx.template(_time)).get(0));
            String summary = (String) ctx.variable(ctx.template(_summary)).get(0);
            String text = (String) ctx.variable(ctx.template(_text)).get(0);
            addReview(pid, uid, profileName, helpfulnessIn, helpfulnessOut, score, time, summary, text)
                    .executeFrom(ctx, newResult, SchedulerAffinity.SAME_THREAD,
                            res -> {
                                Exception exceptionDuringTask = null;
                                if (res != null) {
                                    if (res.output() != null) {
                                        ctx.append(res.output());
                                    }
                                    if (res.exception() != null) {
                                        exceptionDuringTask = res.exception();
                                    }
                                }
                                if (exceptionDuringTask != null) {
                                    ctx.endTask(res, exceptionDuringTask);
                                } else {
                                    ctx.continueWith(res);
                                }
                            });
        }

        @Override
        public void serialize(Buffer builder) {
            builder.writeString("addReview");
            builder.writeChar(Constants.TASK_PARAM_OPEN);
            TaskHelper.serializeString(_pid, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_uid, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_profileName, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_helpfulnessIn, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_helpfulnessOut, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_score, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_time, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_summary, builder, true);
            builder.writeChar(Constants.TASK_PARAM_SEP);
            TaskHelper.serializeString(_text, builder, true);
            builder.writeChar(Constants.TASK_PARAM_CLOSE);
        }
    }


    protected abstract Task handleSummaryAndText(String summary, String text);
}
