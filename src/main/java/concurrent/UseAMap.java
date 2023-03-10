package concurrent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UseAMap {
  private static final int CHUNK_SIZE = 32_768;
  private static final int THREAD_COUNT = 1_000;

  static class Loader implements Runnable {
    private int start;
    private Map<Integer, String> mis;
    public Loader(int start, Map<Integer, String> mis) {
      this.start = start;
      this.mis = mis;
    }

    @Override
    public void run() {
      for (int i = start; i < start + CHUNK_SIZE; i++) {
        mis.put(i, "value " + i);
      }
      System.out.println("Loader at " + start + " finished");
    }
  }

  static class Reader implements Runnable {
    private int start;
    private Map<Integer, String> mis;
    public Reader(int start, Map<Integer, String> mis) {
      this.start = start;
      this.mis = mis;
    }

    @Override
    public void run() {
      for (int i = start; i < start + CHUNK_SIZE; i++) {
        String s;
        long miscount = 0;
        while ((s = mis.get(i)) == null) {
          miscount++;
          if (miscount % 1_000_000 == 0) {
            System.out.println("missing " + i);
          }
        }

        if (!s.equals("value " + i)) {
          System.out.println("**** ERROR at value " + i + " => " + s);
        }
      }
      System.out.println("Reader at " + start + " finished");
    }
  }

  public static void main(String[] args) throws Throwable {

//    Map<Integer, String> mis = new HashMap<>();
//    Map<Integer, String> mis = Collections.synchronizedMap(new HashMap<>());
    Map<Integer, String> mis = new ConcurrentHashMap<>();

    long start = System.nanoTime();

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < THREAD_COUNT; i++) {
      Thread t = new Thread(new Loader(CHUNK_SIZE * i, mis));
      threads.add(t);
      t.start();
    }
    for (int i = 0; i < THREAD_COUNT; i++) {
      Thread t = new Thread(new Reader(CHUNK_SIZE * i, mis));
      threads.add(t);
      t.start();
    }

    System.out.println("all started");
    for (Thread t : threads) {
      t.join();
    }
    long time = System.nanoTime() - start;
    System.out.println("elapsed time " + (time / 1_000_000_000.0));
  }
}