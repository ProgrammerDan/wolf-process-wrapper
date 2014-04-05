public class Wolf extends Animal {
	private static WolfProcess wolfProcess = null;

	private static Wolf[] wolves = new Wolf[];
	private static int nWolves = 0;
	public static int MAP_SIZE = 100;

	private static boolean allDead = false;

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
			Wolf.allDead = true;
			this.isDead = true;
		}
	}

	@Override
	public Attack fight(char opponent) {
		if (Wolf.allDead || !Wolf.wolfProcess.getRunning() || isDead) {
			return Attack.SUICIDE;
		}
		try {
			Attack atk = wolfProcess.fight(id, opponent)

			if (atk == Attack.SUICIDE) {
				this.isDead = true;
			}

			return atk;
		} catch (Exception e) {
			String.out.printf("Something terrible happened, this wolf has died: %s", e.getMessage());
			isDead = true;
			return Attack.SUICIDE;
		}
	}

	@Override
	public Move move() {
		if (Wolf.allDead || !Wolf.wolfProcess.getRunning() || isDead) {
			return Move.HOLD;
		}
		try {
			Move mv = wolfProcess.move(id, surroundings);

			return mv;
		} catch (Exception e) {
			String.out.printf("Something terrible happened, this wolf has died: %s", e.getMessage());
			isDead = true;
			return Move.HOLD;
		}
	}

	static class WolfProcess extends Thread {
		private Process process;
		private BufferedReader reader;
		private PrintWriter writer;
		private boolean running;
		public boolean getRunning() {
			return running;
		}
		public WolfProcess() {
			process = null;
			reader = null;
			writer = null;
			running = true;
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
					this.sleep();
				}
				reader.close();
				writer.close();
				process.destroy(); // kill it with fire.
			} catch (Exception e) {
				System.out.println("Wolf<custom-name> ended catastrophically.");
			}
		}

		public synchronized boolean initWolf(int wolf, int mapsize) {
			boolean success = false;
			try{
				WolfTimeout timeout = new WolfTimeout();
				timeout.setInterrupt(this);
				timeout.start();
				writer.printf("S%02d%d\n", wolf, mapsize);
				writer.flush();
				String reply = reader.readLine();
				if (reply.length >= 3 && reply.charAt(0) == 'K') {
					int id = Integer.valueOf(reply.substring(1));
					if (wolf == id) {
						success = true;
					}
				}
			} catch (InterruptedException ie) {
				System.out.printf("%d failed to initialize, timeout\n", wolf);
			} catch (Exception e) {
				System.out.printf("%d failed to initialize, %s\n", e.getMessage());
			}
			timeout.clearInterrupt();
			timeout.notify();
			return success;
		}

		public synchronized Attack attack(int wolf, char opponent) {
			Attack atk = Attack.SUICIDE;
			try{
				WolfTimeout timeout = new WolfTimeout();
				timeout.setInterrupt(this);
				timeout.start();
				String msize = Integer.toString(mapsize);
				writer.printf("A%02d%c\n", wolf, opponent);
				writer.flush();
				String reply = reader.readLine();
				if (reply.length >= 3) {
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
			} catch (InterruptedException ie) {
				System.out.printf("%d failed to attack, timeout\n", wolf);
			} catch (Exception e) {
				System.out.printf("%d failed to attack, %s\n", e.getMessage());
			}
			timeout.clearInterrupt();
			timeout.notify();
			return atk;
		}

		public synchronized Move move(int wolf, char[][] map) {
			Move move = Move.HOLD;
			try{
				WolfTimeout timeout = new WolfTimeout();
				timeout.setInterrupt(this);
				timeout.start();
				writer.printf("S%02d", wolf);
				for (int row=0; row<map.length; row++) {
					for (int col=0; col<map[row].length; col++) {
						writer.printf("%c", map[row][col]);
					}
				}
				writer.print("\n");
				writer.flush();
				String reply = reader.readLine();
				if (reply.length >= 3) {
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
			} catch (InterruptedException ie) {
				System.out.printf("%d failed to initialize, timeout\n", wolf);
			} catch (Exception e) {
				System.out.printf("%d failed to initialize, %s\n", e.getMessage());
			}
			timeout.clearInterrupt();
			timeout.notify();
			return success;
		}
	}

	static class WolfTimeout extends Thread {
		long timeout = 1000l;
		Thread interrupt = null;

		public WolfTimeout(long timeout) {
			this.timeout = timeout;
			interrupt = null;
		}

		public WolfTimeout() {
			interrupt = null;
		}
	
		public void setInterrupt(Thread interrupt){
			this.interrupt = interrupt;
		}

		public void clearInterrupt(){
			this.interrupt = null;
		}

		public void run() {
			if (interrupt == null) return;
			try{
				this.wait(timeout);
				if (interrupt != null) {
					interrupt.interrupt();
				}
			} catch (InterruptedException ie) {
				return;
			}
		}
	}			
}


