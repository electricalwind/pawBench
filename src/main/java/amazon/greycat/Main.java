package amazon.greycat;

import amazon.greycat.api.Review;
import amazon.greycat.paw.PReview;
import amazon.greycat.regular.RReview;
import amazon.greycat.regular.RUser;
import greycat.Callback;
import greycat.DeferCounter;
import greycat.Graph;
import greycat.GraphBuilder;
import greycat.plugin.Job;
import greycat.rocksdb.RocksDBStorage;
import greycat.scheduler.NoopScheduler;
import greycat.scheduler.TrampolineScheduler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static greycat.Tasks.newTask;

public class Main {

    public static void main(String[] args) {
        String urlMovies = "/Users/youradmin/Desktop/Programmation/utils/movies.txt";

        Graph graphNT = new GraphBuilder()
                .withMemorySize(1000000)
                .withStorage(new RocksDBStorage("/Users/youradmin/Desktop/Programmation/utils/meowbench/rocks/RegularBench"))
                .withScheduler(new TrampolineScheduler())
                .build();


        graphNT.connect(result -> {
            DeferCounter counter = graphNT.newCounter(1);
            try {
                Review review = new RReview();
                addingContent(urlMovies, graphNT, 1000, counter,review);
            } catch (IOException e) {
                e.printStackTrace();
                counter.count();
            }
            counter.then(() -> graphNT.disconnect(null));
        });

        Graph graphP = new GraphBuilder()
                .withMemorySize(1000000)
                .withStorage(new RocksDBStorage("/Users/youradmin/Desktop/Programmation/utils/meowbench/rocks/PawBench"))
                .withScheduler(new TrampolineScheduler())
                .build();


        graphP.connect(result -> {
            DeferCounter counter = graphP.newCounter(1);
            try {
                Review review = new PReview();
                addingContent(urlMovies, graphP, 1000, counter,review);
            } catch (IOException e) {
                e.printStackTrace();
                counter.count();
            }
            counter.then(() -> graphP.disconnect(null));
        });




    }


    private static void addingContent(String urlMovies, Graph graph, int saveEvery, DeferCounter counter, Review review) throws IOException {
        Path path = Paths.get(urlMovies);
        Stream<String> lines = Files.lines(path, StandardCharsets.ISO_8859_1);
        Iterator<String> sc = lines.iterator();
        long timeStart = System.currentTimeMillis();

        Pattern regexpProductId = Pattern.compile("product/productId: ([A-Za-z0-9]*)");
        Pattern regexpUserId = Pattern.compile("review/userId: ([A-Za-z0-9]*)");
        Pattern regexpProfile = Pattern.compile("review/profileName: (.*)");
        Pattern regexpHelpfulness = Pattern.compile("review/helpfulness: ([0-9]*)/([0-9]*)");
        Pattern regexpScore = Pattern.compile("review/score: ([0-9.]*)");
        Pattern regexpTime = Pattern.compile("review/time: ([0-9]*)");
        Pattern regexpSummary = Pattern.compile("review/summary: (.*)");
        Pattern regexpText = Pattern.compile("review/text: (.*)");


        newTask()
                .inject(0)
                .setAsVar("i")
                .whileDo(ctx -> sc.hasNext(),
                        newTask()

                                .thenDo(ctx -> {
                                    ctx.setVariable("i", ctx.intVar("i") + 1);
                                    for (int i = 0; i < 9; i++) {
                                        String line = sc.next();
                                        switch (i) {
                                            case 0:
                                                Matcher matcher = regexpProductId.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("pid", matcher.group(1));
                                                break;
                                            case 1:
                                                matcher = regexpUserId.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("uid", matcher.group(1));
                                                break;
                                            case 2:
                                                matcher = regexpProfile.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("profileName", matcher.group(1));
                                                break;
                                            case 3:
                                                boolean find = regexpHelpfulness.matcher(line).find();
                                                while (!find) {
                                                    String profileName = (String) ctx.variable("profileName").get(0);
                                                    ctx.setVariable("profileName", profileName + "\\n" + line);
                                                    line = sc.next();
                                                    find = regexpHelpfulness.matcher(line).find();
                                                    System.out.println("I'm Stuck in find");
                                                }
                                                matcher = regexpHelpfulness.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("helpfulnessIn", matcher.group(1));
                                                ctx.setVariable("helpfulnessOut", matcher.group(2));
                                                break;
                                            case 4:
                                                matcher = regexpScore.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("score", matcher.group(1));
                                                break;
                                            case 5:
                                                matcher = regexpTime.matcher(line);
                                                matcher.find();
                                                ctx.setVariable("time", matcher.group(1));
                                                break;
                                            case 6:
                                                matcher = regexpSummary.matcher(line);
                                                matcher.find();
                                                String summary = matcher.group(1);
                                                summary = summary.replace("<br /><br />", " ");
                                                summary = summary.replace("<br />", " ");
                                                ctx.setVariable("summary", summary);
                                                break;
                                            case 7:
                                                find = regexpText.matcher(line).find();
                                                while (!find) {
                                                    summary = (String) ctx.variable("summary").get(0);
                                                    ctx.setVariable("summary", summary + "\\n" + line);
                                                    line = sc.next();
                                                    find = regexpText.matcher(line).find();
                                                    System.out.println("I'm Stuck in sumary");
                                                }
                                                matcher = regexpText.matcher(line);
                                                matcher.find();
                                                String text = matcher.group(1);
                                                text = text.replace("<br /><br />", " ");
                                                text = text.replace("<br />", " ");
                                                ctx.setVariable("text", text);
                                                break;
                                            case 8:
                                                while (!line.isEmpty()) {
                                                    text = (String) ctx.variable("text").get(0);
                                                    ctx.setVariable("text", text + "\\n" + line);
                                                    line = sc.next();
                                                    System.out.println("I'm Stuck in text");
                                                }
                                                break;
                                        }
                                    }
                                    ctx.continueTask();
                                })
                                .then(review.addReview("pid", "uid", "profileName", "helpfulnessIn", "helpfulnessOut", "score", "time", "summary", "text"))
                                .ifThen(
                                        ctx -> (ctx.intVar("i") % saveEvery == 0),
                                        newTask()
                                                .thenDo(
                                                        ctx -> {
                                                            if ((ctx.intVar("i") / saveEvery) % 100 == 0) {
                                                            long timeEnd = System.currentTimeMillis();
                                                            System.out.println("saved " + ctx.intVar("i") + " in " + (timeEnd - timeStart) + " ms");
                                                             }
                                                            ctx.continueTask();
                                                        })
                                                .save()
                                )
                )
                .save()
                .execute(graph,
                        taskRes -> {
                            System.out.println(taskRes.exception());
                            long timeEnd = System.currentTimeMillis();
                            System.out.println("time to add everything: " + (timeEnd - timeStart));
                            counter.count();
                        }
                );
    }
}