package action;

import bean.DownloadTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by Aliaksei_Kisialiou on 7/19/2017.
 */

public class Downloader implements Callable<Downloader> {
    private final String source;
    private final String dest;
    private int size;
    private Queue<DownloadTask> listForDownload;
    public static final BlockingQueue<DownloadTask> buffer = new ArrayBlockingQueue<>(15000);

    public Downloader(String source, String dest) {
        this.source = source;
        this.dest = dest;
        fillingQueue(source, dest);
    }

    public Downloader call() throws Exception {
        download();
        downloadHelp();
        return null;
    }

    public  void download() {
        while (!listForDownload.isEmpty() ) {
            DownloadTask downloadTask = listForDownload.poll();
            if (buffer.contains(downloadTask)) {
                buffer.remove(downloadTask);
                try {
                    Files.move(Paths.get(downloadTask.getSource()), Paths.get(downloadTask.getDest()), REPLACE_EXISTING);
                    waitDownload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread = " + Thread.currentThread().getName() + ", File source = "
                        + downloadTask.getSource() + ", dest = " + downloadTask.getDest());
            }
        }
        System.out.println("Thread = " + Thread.currentThread().getName() + "END ITS JOB!!!");
    }

    public void downloadHelp() {

        while (!buffer.isEmpty()) {
                DownloadTask downloadTask = buffer.poll();
                try {
                    Files.move(Paths.get(downloadTask.getSource()), Paths.get(downloadTask.getSource()));
                    waitDownload();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("HELPER!!!Thread = " + Thread.currentThread().getName() + ", File source = "
                        + downloadTask.getSource() + ", dest = " + downloadTask.getDest());
            }
    }

    private void fillingQueue(String source, String dest) {
        File dir = new File(source);
        File[] list = dir.listFiles();
        listForDownload = new LinkedList<>();
        for (int i = 0; i < list.length; i++) {
            DownloadTask downloadTask = new DownloadTask(list[i].toString(), dest + list[i].getName());
            listForDownload.add(downloadTask);
            buffer.add(downloadTask);
        }
    }
    private void waitDownload() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void waitDownload(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
