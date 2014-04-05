package animals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Remote Wolf<custom-name> wrapper class. 
 */
public class Wolf<custom-name> extends Animal {
	/**
	 * Simple test script that sends some typical commands to the
	 * remote process.
	 */
	public static void main(String[]args){
		Wolf<custom-name>[] wolves = new Wolf<custom-name>[100];
		for(int i=0; i<10; i++) {
			wolves[i] = new Wolf<custom-name>();
		}
		char map[][] = new char[3][3];
		for (int i=0;i<9;i++)
			map[i/3][i%3]=' ';
		map[1][1] = 'W';
		for(int i=0; i<10; i++) {
			wolves[i].surroundings=map;
			System.out.println(wolves[i].move());
		}
		for(int i=0; i<10; i++) {
			System.out.println(wolves[i].fight('S'));
			System.out.println(wolves[i].fight('B'));
			System.out.println(wolves[i].fight('L'));
			System.out.println(wolves[i].fight('W'));
		}
		wolfProcess.endProcess();
	}
	private static WolfProcess wolfProcess = null;

	private static Wolf<custom-name>[] wolves = new Wolf<custom-name>[100];
	private static int nWolves = 0;

	private boolean isDead;
	private int id;

	/**
	 * Sets up a remote process wolf. Note the static components. Only
	 * a single process is generated for all Wolves of this type, new
	 * wolves are "initialized" within the remote process, which is
	 * maintained alongside the primary process.
	 * Note this implementation makes heavy use of threads.
	 */
	public Wolf<custom-name>() {
		super('W');
		if (Wolf<custom-name>.wolfProcess == null) {
			Wolf<custom-name>.wolfProcess = new WolfProcess();
			Wolf<custom-name>.wolfProcess.start();
		}

		if (Wolf<custom-name>.wolfProcess.initWolf(Wolf<custom-name>.nWolves, MAP_SIZE)) {
			this.id = Wolf<custom-name>.nWolves;
			this.isDead = false;
			Wolf<custom-name>.wolves[id] = this;
		} else {
			Wolf<custom-name>.wolfProcess.endProcess();
			this.isDead = true;
		}
		Wolf<custom-name>.nWolves++;
	}

	/**
	 * If the wolf is dead, or all the wolves of this type are dead, SUICIDE.
	 * Otherwise, communicate an attack to the remote process and return
	 * its attack choice.
	 */
	@Override
	public Attack fight(char opponent) {
		if (!Wolf<custom-name>.wolfProcess.getRunning() || isDead) {
			return Attack.SUICIDE;
		}
		try {
			Attack atk = Wolf<custom-name>.wolfProcess.fight(id, opponent);

			if (atk == Attack.SUICIDE) {
				this.isDead = true;
			}

			return atk;
		} catch (Exception e) {
			System.out.printf("Something terrible happened, this wolf has died: %s", e.getMessage());
			isDead = true;
			return Attack.SUICIDE;
		}
	}

	/**
	 * If the wolf is dead, or all the wolves of this type are dead, HOLD.
	 * Otherwise, get a move from the remote process and return that.
	 */
	@Override
	public Move move() {
		if (!Wolf<custom-name>.wolfProcess.getRunning() || isDead) {
			return Move.HOLD;
		}
		try {
			Move mv = Wolf<custom-name>.wolfProcess.move(id, surroundings);

			return mv;
		} catch (Exception e) {
			System.out.printf("Something terrible happened, this wolf has died: %s", e.getMessage());
			isDead = true;
			return Move.HOLD;
		}
	}

	/**
	 * The shared static process manager, that synchronizes all communication
	 * with the remote process.
	 */
	static class WolfProcess extends Thread {
		private Process process;
		private BufferedReader reader;
		private PrintWriter writer;
		private ExecutorService executor;
		private boolean running;

		public boolean getRunning() {
			return running;
		}
		
		public WolfProcess() {
			process = null;
			reader = null;
			writer = null;
			running = true;
			executor = Executors.newFixedThreadPool(1);
		}

		public void endProcess() {
			running = false;
		}

		/**
		 * WolfProcess thread body. Keeps the remote connection alive.
		 */
		public void run() {
			try {
				System.out.println("Starting Wolf<custom-name> remote process");
				ProcessBuilder pb = new ProcessBuilder("<invocation>".split(" "));
				pb.redirectErrorStream(true);
				process = pb.start();
				System.out.println("Wolf<custom-name> process begun");
				// STDOUT of the process.
				reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); 
				System.out.println("Wolf<custom-name> reader stream grabbed");
				// STDIN of the process.
				writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
				System.out.println("Wolf<custom-name> writer stream grabbed");
				while(running){
					this.sleep(0);
				}
				reader.close();
				writer.close();
				process.destroy(); // kill it with fire.
				executor.shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Wolf<custom-name> ended catastrophically.");
			}
		}

		/**
		 * Helper that invokes a read with a timeout
		 */
		private String getReply(long timeout) throws TimeoutException, ExecutionException, InterruptedException{
			Callable<String> readTask = new Callable<String>() {
				@Override
				public String call() throws Exception {
					return reader.readLine();
				}
			};

			Future<String> future = executor.submit(readTask);
			return future.get(timeout, TimeUnit.MILLISECONDS);
		}

		/**
		 * Sends an initialization command to the remote process
		 */
		public synchronized boolean initWolf(int wolf, int map_sz) {
			while(writer == null){
				try {
				this.sleep(0);
				}catch(Exception e){}
			}
			boolean success = false;
			try{
				writer.printf("S%02d%d\n", wolf, map_sz);
				writer.flush();
				String reply = getReply(5000l);
				if (reply != null && reply.length() >= 3 && reply.charAt(0) == 'K') {
					int id = Integer.valueOf(reply.substring(1));
					if (wolf == id) {
						success = true;
					}
				}
				if (reply == null) {
					System.out.println("did not get reply");
				}
			} catch (TimeoutException ie) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to initialize, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to initialize, %s\n", wolf, e.getMessage());
			}
			return success;
		}

		/**
		 * Send an ATTACK command to the remote process.
		 */
		public synchronized Attack fight(int wolf, char opponent) {
			Attack atk = Attack.SUICIDE;
			try{
				writer.printf("A%02d%c\n", wolf, opponent);
				writer.flush();
				String reply = getReply(1000l);
				if (reply.length() >= 3) {
					int id = Integer.valueOf(reply.substring(1));
					if (wolf == id) {
						switch(reply.charAt(0)) {
							case 'R':
								atk = Attack.ROCK;
								break;
							case 'P':
								atk = Attack.PAPER;
								break;
							case 'S':
								atk = Attack.SCISSORS;
								break;
							case 'D':
								atk = Attack.SUICIDE;
								break;
						}
					}
				}
			} catch (TimeoutException ie) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to attack, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to attack, %s\n", wolf, e.getMessage());
			}
			return atk;
		}

		/**
		 * Send a MOVE command to the remote process.
		 */
		public synchronized Move move(int wolf, char[][] map) {
			Move move = Move.HOLD;
			try{
				writer.printf("M%02d", wolf);
				for (int row=0; row<map.length; row++) {
					for (int col=0; col<map[row].length; col++) {
						writer.printf("%c", map[row][col]);
					}
				}
				writer.print("\n");
				writer.flush();
				String reply = getReply(1000l);
				if (reply.length() >= 3) {
					int id = Integer.valueOf(reply.substring(1));
					if (wolf == id) {
						switch(reply.charAt(0)) {
							case 'H':
								move = Move.HOLD;
								break;
							case 'U':
								move = Move.UP;
								break;
							case 'L':
								move = Move.LEFT;
								break;
							case 'R':
								move = Move.RIGHT;
								break;
							case 'D':
								move = Move.DOWN;
								break;
						}
					}
				}
			} catch (TimeoutException ie) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to move, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("Wolf<custom-name> %d failed to move, %s\n", wolf, e.getMessage());
			}
			return move;
		}
	}
}
