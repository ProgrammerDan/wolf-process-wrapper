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

public class Wolf extends Animal {
	private static WolfProcess wolfProcess = null;

	private static Wolf[] wolves = new Wolf[100];
	private static int nWolves = 0;
	public static int MAP_SIZE = 100;


	private boolean isDead;
	private int id;

	public Wolf() {
		super('W');
		if (Wolf.wolfProcess == null) {
			Wolf.wolfProcess = new WolfProcess();
			Wolf.wolfProcess.start();
		}

		if (Wolf.wolfProcess.initWolf(Wolf.nWolves, Wolf.MAP_SIZE)) {
			this.id = Wolf.nWolves;
			this.isDead = false;
			Wolf.wolves[nWolves++] = this;
		} else {
			Wolf.wolfProcess.endProcess();
			this.isDead = true;
		}
	}

	@Override
	public Attack fight(char opponent) {
		if (!Wolf.wolfProcess.getRunning() || isDead) {
			return Attack.SUICIDE;
		}
		try {
			Attack atk = Wolf.wolfProcess.fight(id, opponent);

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

	@Override
	public Move move() {
		if (!Wolf.wolfProcess.getRunning() || isDead) {
			return Move.HOLD;
		}
		try {
			Move mv = Wolf.wolfProcess.move(id, surroundings);

			return mv;
		} catch (Exception e) {
			System.out.printf("Something terrible happened, this wolf has died: %s", e.getMessage());
			isDead = true;
			return Move.HOLD;
		}
	}

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

		public void run() {
			try {
				ProcessBuilder pb = new ProcessBuilder("<invocation>");
				process = pb.start();
				// STDOUT of the process.
				reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")); 
				// STDIN of the process.
				writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
				while(running){
					this.sleep(0);
				}
				reader.close();
				writer.close();
				process.destroy(); // kill it with fire.
				executor.shutdownNow();
			} catch (Exception e) {
				System.out.println("Wolf<custom-name> ended catastrophically.");
			}
		}

		private String getReply() throws TimeoutException, ExecutionException, InterruptedException{
			Callable<String> readTask = new Callable<String>() {
				@Override
				public String call() throws Exception {
					return reader.readLine();
				}
			};

			Future<String> future = executor.submit(readTask);
			return future.get(1000l, TimeUnit.MILLISECONDS);
		}

		public synchronized boolean initWolf(int wolf, int mapsize) {
			boolean success = false;
			try{
				writer.printf("S%02d%d\n", wolf, mapsize);
				writer.flush();
				String reply = getReply();
				if (reply.length() >= 3 && reply.charAt(0) == 'K') {
					int id = Integer.valueOf(reply.substring(1));
					if (wolf == id) {
						success = true;
					}
				}
			} catch (TimeoutException ie) {
				endProcess();
				System.out.printf("%d failed to initialize, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("%d failed to initialize, %s\n", e.getMessage());
			}
			return success;
		}

		public synchronized Attack fight(int wolf, char opponent) {
			Attack atk = Attack.SUICIDE;
			try{
				writer.printf("A%02d%c\n", wolf, opponent);
				writer.flush();
				String reply = getReply();
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
				System.out.printf("%d failed to attack, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("%d failed to attack, %s\n", e.getMessage());
			}
			return atk;
		}

		public synchronized Move move(int wolf, char[][] map) {
			Move move = Move.HOLD;
			try{
				writer.printf("S%02d", wolf);
				for (int row=0; row<map.length; row++) {
					for (int col=0; col<map[row].length; col++) {
						writer.printf("%c", map[row][col]);
					}
				}
				writer.print("\n");
				writer.flush();
				String reply = getReply();
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
				System.out.printf("%d failed to initialize, timeout\n", wolf);
			} catch (Exception e) {
				endProcess();
				System.out.printf("%d failed to initialize, %s\n", e.getMessage());
			}
			return move;
		}
	}
}
