package net.curxxed.dev.agent;

public class Util {
   public static void print(String msg) {
      if (msg != null && !msg.isEmpty()) {
         System.out.println("[Mod-Agent]: " + msg);
      }
   }
}
