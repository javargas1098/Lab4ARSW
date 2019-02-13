package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

	private ImmortalUpdateReportCallback updateCallback = null;

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	private int health;

	private int defaultDamageValue;

	private final List<Immortal> immortalsPopulation;

	private final String name;

	private final Random r = new Random(System.currentTimeMillis());
	private volatile Boolean isPaused = false;
	private boolean runningThread = false;
	private volatile boolean stop = true;

	public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
			ImmortalUpdateReportCallback ucb) {
		super(name);
		this.updateCallback = ucb;
		this.name = name;
		this.immortalsPopulation = immortalsPopulation;
		this.health = health;
		this.defaultDamageValue = defaultDamageValue;
	}

	public void run() {

		while (true) {
			Immortal im;

			int myIndex = immortalsPopulation.indexOf(this);

			int nextFighterIndex = r.nextInt(immortalsPopulation.size());

			// avoid self-fight
			if (nextFighterIndex == myIndex) {
				nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
			}

			im = immortalsPopulation.get(nextFighterIndex);

			this.fight(im);
			try {
				if (isPaused) {
					synchronized (this) {
						while (isPaused) {
							wait();
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
//				if (!runningThread) {
//	                return;
//	            }
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public void pausa() {

		if (isPaused == false) {
			isPaused = true;
		}
	}

	public synchronized void resumeGame() {
		if (isPaused == true) {
			isPaused = false;
			notifyAll();

		}
	}

	public void fight(Immortal i2) {
		synchronized (updateCallback) {

			if (i2.getHealth() > 0) {
				i2.changeHealth(i2.getHealth() - defaultDamageValue);
				this.health += defaultDamageValue;
				updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
			} else {
				updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
			}
		}

	}

	public void changeHealth(int v) {
		health = v;
	}

	public int getHealth() {
		return health;
	}

	@Override
	public String toString() {

		return name + "[" + health + "]";
	}


	

}
