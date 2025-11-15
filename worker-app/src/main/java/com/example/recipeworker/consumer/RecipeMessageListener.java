package com.example.recipeworker.consumer;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

@Service
public class RecipeMessageListener {

    @RabbitListener(queues = "recipes.publish.queue")
    public void handleMessage(Map<String,Object> msg) throws Exception {
        System.out.println("Worker received: " + msg);
        if (!msg.containsKey("tempImagePaths")) return;
        List<String> tempPaths = (List<String>) msg.get("tempImagePaths");
        String recipeId = (String) msg.get("externalRecipeId");
        if (recipeId == null) recipeId = "unknown";
        Path storageRoot = Paths.get(System.getProperty("storage.root", System.getProperty("java.io.tmpdir") + "/recipe-images"));
        Path recipeDir = storageRoot.resolve(recipeId);
        Files.createDirectories(recipeDir);
        int idx=0;
        for (String tp : tempPaths) {
            File in = new File(tp);
            if (!in.exists()) {
                System.out.println("Temp file missing: " + tp);
                continue;
            }
            Path large = recipeDir.resolve("img_" + idx + "_lg.jpg");
            Path thumb = recipeDir.resolve("img_" + idx + "_thumb.jpg");
            Thumbnails.of(in).size(1200,1200).outputFormat("jpg").toFile(large.toFile());
            Thumbnails.of(in).size(300,300).outputFormat("jpg").toFile(thumb.toFile());
            System.out.println("Wrote " + large + " and " + thumb);
            in.delete();
            idx++;
        }
        // create a marker file indicating processed
        Files.writeString(recipeDir.resolve("processed.txt"), "processed");
    }
}
