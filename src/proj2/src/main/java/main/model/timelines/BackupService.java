package main.model.timelines;

import java.io.File;
import java.io.IOException;

public class BackupService implements Runnable {

    private Timeline timeline;
    private File timelinesFolder;

    public BackupService(File timelinesFolder) {
        this.timeline = null;
        this.timelinesFolder = timelinesFolder;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    @Override
    public void run() {
        try {
            if (this.timeline != null)
                this.timeline.save(this.timelinesFolder);
        } catch (IOException e) {
            System.err.println("Error: Couldn't save timeline.");
            e.printStackTrace();
        } 
    }
    
}
