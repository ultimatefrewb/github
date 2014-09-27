package github;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.parabot.core.Context;
import org.parabot.core.asm.wrappers.Interface;
import org.parabot.environment.api.interfaces.Paintable;
import org.parabot.environment.api.utils.Time;
import org.parabot.environment.input.Keyboard;
import org.parabot.environment.input.Mouse;
import org.parabot.environment.scripts.Script;
import org.parabot.environment.scripts.ScriptManifest;
import org.parabot.environment.scripts.framework.LoopTask;
import org.parabot.environment.scripts.framework.Strategy;
import org.parabot.environment.scripts.Category;
import org.rev317.min.Loader;
import org.rev317.min.accessors.Client;

import org.rev317.min.api.methods.Game;
import org.rev317.min.api.methods.Inventory;
import org.rev317.min.api.methods.Menu;
import org.rev317.min.api.methods.Npcs;
import org.rev317.min.api.methods.Players;
import org.rev317.min.api.methods.SceneObjects;
import org.rev317.min.api.methods.Skill;
import org.rev317.min.api.wrappers.Item;
import org.rev317.min.api.wrappers.Npc;
import org.rev317.min.api.wrappers.Player;
import org.rev317.min.api.wrappers.SceneObject;
import org.rev317.min.api.wrappers.Tile;

@ScriptManifest(author = "Fatboy", category = Category.HERBLORE, description = "", name = "1-99 Herblore.", servers = { "" }, version = 2.0)
public class HerbloreMaxer extends Script implements Paintable, LoopTask {

	enum State {
		IDLE, buyItems, makePot, drop, bank, checkBank, buyNoted, dropBad;

	}

	State state = State.IDLE;
	public int vial = 228;
	public int eye = 222;
	public int limpwurt = 226;
	public int spiderEggs = 224;
	public int toadLegs = 2153;
	public int unicornDust = 236;
	public int notedUnicorn = 237;

	public int notedVial = 229;

	public int guam = 250;
	public int tarromin = 254;
	public int harralander = 256;
	public int toadflax = 2999;

	public int irit = 260;
	public int notedIrit = 261;

	public int birdsNest = 5076;
	public int notedNest = 16056;

	public int toadsFlax = 2999;
	public int notedFlax = 3000;

	public int gp = 996;

	public int unfGuam = 92;
	public int unfTarromin = 96;
	public int unfHarralander = 98;
	public int unfToadflax = 3003;
	public int unfIrit = 102;
	public int unfToad = 3003;

	public int unnotedHerbs[] = { 250, 354, 256, 2999, 260 };
	public int unnotedOthers[] = { 222, 226, 224, 2153, 236, 5076, };

	public int atkPotion = 2429;// guam
	public int strengthPotion = 114;// tarromin
	public int restorePotion = 2431;// harr
	public int agilityPotion = 3033;// toadflax
	public int superAntiPotion = 2449;// irit
	public int saraPotion = 6686;// toadflx and nest

	public int[] finishedPots = { 2429, 114, 2431, 3033, 2449, 6686 };

	Point depositAll = new Point(400, 305);
	long rTimer = 0;
	long lastAnim = 0;

	public boolean checkBank = false;
	public boolean needHerb = false;
	public boolean needOther = false;
	public boolean needVial = false;

	public void getState() {

		try {

			/*
			 * if (hasOther()) { System.out.println("Have other."); } if
			 * (hasHerb()) { System.out.println("Have herb."); } if (hasUnf()) {
			 * System.out.println("Have unf."); } if (hasVial()) {
			 * System.out.println("Have vial."); } if (hasFinished()) {
			 * System.out.println("Have finished."); }
			 */
			//if (hasBadItem()) {

				// return;
			//	state = State.dropBad;
			//	return;
			//}

			if (getLevel() >= 48) {

				if (!checkBank) {
					state = State.checkBank;
					return;
				}

				if (needVial || needHerb || needOther) {
					state = State.buyItems;
					return;
				}

				if (!hasSupplies() && !hasUnf()) {
					state = State.bank;
					return;
				}

				if (hasHerb() && hasOther() && hasVial() || hasUnf()
						&& hasOther()) {
					state = State.makePot;
					return;
				}

				return;
			}

			if (!hasSupplies() && !hasUnf()) {

				if (hasFinished()) {
					state = State.drop;
					return;
				}

				state = State.buyItems;
				return;
			}

			if (hasHerb() && hasOther() && hasVial() || hasUnf() && hasOther()) {
				state = State.makePot;
				return;
			}

		} catch (Exception e) {

		}
	}

	public void handleState() {

		try {

			// if (state == State.buyNoted) {
			// buyNoted();
			// return;
			// }

			if (state == State.dropBad) {
				dropBadItems();
				return;
			}

			if (state == State.checkBank) {
				checkBank();
				return;
			}

			if (state == State.bank) {
				bank();
				return;
			}

			if (state == State.buyItems) {
				buyItems();
				return;
			}

			if (state == State.makePot) {
				combine();
				return;
			}

			if (state == State.drop) {
				drop();
				return;
			}

		} catch (Exception e) {

		}

	}

	public void checkBank() {

		if (!atPrivate()) {
			telePrivate();
			return;
		}

		if (!bankOpen()) {
			openBank();
			return;
		}

		if (Inventory.getCount() >= 25) {
			// depositWrongID();
			// return;
		}

		if (getBankSlot(herb()) == 0) {
			needHerb = true;
		}

		if (getBankSlot(other()) == 0) {
			needOther = true;
		}

		if (getBankSlot(vial) == 0) {
			needVial = true;
		}

		checkBank = true;
	}

	private int getBankSlot(int id) {

		int[] bankIds = Loader.getClient().getInterfaceCache()[5382].getItems();

		for (int i = 0; i < bankIds.length; i++) {
			if (bankIds[i] == id) {
				return i;
			}
		}
		return 0;
	}

	public void depositFinished() {
		for (Item i : Inventory.getItems()) {
			for (int p : finishedPots) {
				if (p == i.getId()) {
					Menu.sendAction(432, i.getId() - 1, i.getSlot(), 5064);
					sleep(1000);
					return;
				}
			}
		}
	}

	public void telePrivate() {
		final Player me = Players.getMyPlayer();

		if (me.getAnimation() > 0) {
			sleep(2000);
			return;
		}

		if (Game.getOpenBackDialogId() == 2400) {
			Mouse.getInstance().click(290, 405, true);
			sleep(2000);
		}

		if (Game.getOpenBackDialogId() > 0) {
			Mouse.getInstance().click(295, 445, true);
			sleep(1000);
			return;
		}

		Keyboard.getInstance().sendKeys("::private " + getName());
		sleep(2000);
	}

	private static String getName() {
		try {
			Class<?> c = Loader.getClient().getClass();
			Field f = c.getDeclaredField("hG");
			f.setAccessible(true);
			return (String) f.get(Loader.getClient());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void bank() {

		if (!atPrivate()) {
			telePrivate();
			return;
		}

		if (!bankOpen()) {
			openBank();
			return;
		}

		if (hasFinished()) {
			depositFinished();
			return;
		}

		if (hasNoted()) {
			depositNoted();
			return;
		}

		checkBank();// checks whenever the bank is open if we need more herbs

		if (moreThanNeeded()) {
			Mouse.getInstance().click(depositAll);
			sleep(1000);
			return;
		}

		if (!hasHerb()) {
			Menu.sendAction(431, herb() - 1, getBankSlot(herb()), 5382);
			sleep(1000);
			Keyboard.getInstance().sendKeys("9");
			sleep(1000);
			return;
		}

		if (!hasVial()) {
			Menu.sendAction(431, herb() - 1, getBankSlot(vial), 5382);
			sleep(1000);
			Keyboard.getInstance().sendKeys("9");
			sleep(1000);
			return;
		}

		if (!hasOther()) {
			Menu.sendAction(431, herb() - 1, getBankSlot(other()), 5382);
			sleep(1000);
			Keyboard.getInstance().sendKeys("9");
			sleep(1000);
			return;
		}
	}

	public boolean hasNoted() {
		if (Inventory.getCount(notedHerb()) > 0
				|| Inventory.getCount(notedOther()) > 0
				|| Inventory.getCount(notedVial) > 0)
			return true;

		return false;
	}

	public void depositNoted() {
		if (Inventory.getCount(notedHerb()) > 0) {
			depositAll(notedHerb());
			return;
		}

		if (Inventory.getCount(notedOther()) > 0) {
			depositAll(notedOther());
			return;
		}

		if (Inventory.getCount(notedVial) > 0) {
			depositAll(notedVial);
			return;
		}
	}
	
	public boolean hasOutlier() {
		if (Inventory.getCount(herb()) > 0 && Inventory.getCount(vial) == 0)
			return true;
		
		if (Inventory.getCount(vial) > 0 && Inventory.getCount(herb()) == 0)
			return true;
		
		if (Inventory.getCount(herb()) > 0 && Inventory.getCount(other()) == 0)
			return true;
		
		if (Inventory.getCount(herb()) > 0 && Inventory.getCount(vial) == 0)
			return true;
		
		return false;
	}

	public boolean hasTooMuch() {
		if (Inventory.getCount(herb()) > 5)
			return true;

		if (Inventory.getCount(other()) > 5)
			return true;

		if (Inventory.getCount(vial) > 5)
			return true;

		return false;
	}

	public boolean needsHerb() {
		if (Inventory.getCount(true, notedHerb()) >= 1000) {
			needHerb = false;
			return false;
		}

		return true;
	}

	public boolean needsOther() {
		if (Inventory.getCount(true, notedOther()) >= 1000) {
			needOther = false;
			return false;
		}

		return true;
	}

	public boolean needsVials() {
		if (Inventory.getCount(true, notedVial) >= 1000) {
			needVial = false;
			return false;
		}

		return true;
	}

	public int notedHerb() {
		if (getLevel() < 81)
			return notedIrit;

		return notedFlax;
	}

	public int notedOther() {
		if (getLevel() < 81)
			return notedUnicorn;

		return notedNest;
	}

	public void depositWrongID() {
		for (Item i : Inventory.getItems()) {
			if (i.getId() != 996 && i.getId() != 995) {
				Menu.sendAction(432, i.getId() - 1, i.getSlot(), 5064);
				sleep(1000);
				return;
			}
		}
	}

	public void depositAll(int itemid) {
		if (!bankOpen()) {
			openBank();
			return;
		}
		for (Item i : Inventory.getItems()) {
			if (i.getId() == itemid) {
				Menu.sendAction(432, i.getId() - 1, i.getSlot(), 5064);
				sleep(1000);
			}
		}
	}

	public boolean bankOpen() {
		if (Game.getOpenInterfaceId() == 23350)
			return true;

		return false;
	}

	public void openBank() {
		SceneObject[] obj = SceneObjects.getNearest(2213);
		SceneObject bank = obj[0];

		if (Game.getOpenInterfaceId() != 23350) {
			bank.interact(0);
			sleep(bank.getLocation().distanceTo() * 500);
			return;
		}
	}

	public void closeBank() {
		if (Game.getOpenInterfaceId() == 23350) {
			Mouse.getInstance().click(486, 27, true);
			sleep(400);
			return;
		}
	}

	public void drop() {
		for (Item i : Inventory.getItems()) {
			for (int p : finishedPots) {
				if (p == i.getId()) {
					Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
					sleep(500);
				}
				if (hasTooMuch()) {
					if (i.getId() == herb() && Inventory.getCount(herb()) > 9) {
						Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
						sleep(1000);
						return;
					}
					if (i.getId() == other() && Inventory.getCount(other()) > 9) {
						Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
						sleep(1000);
						return;
					}
					if (i.getId() == vial && Inventory.getCount(vial) > 9) {
						Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
						sleep(1000);
						return;
					}
				}
			}

		}
	}

	public boolean atPrivate() {
		final Player me = Players.getMyPlayer();

		if (me.getLocation().getY() > 2780 && me.getLocation().getY() < 2820) {
			return true;
		}
		return false;
	}

	public boolean hasSupplies() {
		if (!hasHerb() || !hasOther() || !hasVial()) {
			return false;
		}
		return true;
	}

	public boolean moreThanNeeded() {
		if (Inventory.getCount(herb()) > 9)
			return true;

		if (Inventory.getCount(other()) > 9)
			return true;

		if (Inventory.getCount(vial) > 9)
			return true;

		return false;
	}

	public void combine() {

		if (hasHerb() && hasVial()) {
			makeUnf();
			return;
		}

		if (hasOther() && hasUnf()) {
			makePotion();
			return;
		}
	}

	long finishAnim = 0;

	public void makePotion() {
		final Player me = Players.getMyPlayer();

		if (!hasOther())
			finishAnim = 0;

		if (me.getAnimation() == 363 && hasFinished()) {
			finishAnim = System.currentTimeMillis();
			// return;
		}

		if (System.currentTimeMillis() - finishAnim < 2000 && hasOther())
			return;

		if (Game.getOpenBackDialogId() == 2492) {
			Menu.sendAction(315, 2434, 162, 2498);
			sleep(2000);
			return;
		}

		for (Item i : Inventory.getItems()) {

			if (i.getId() == unf()) {
				Menu.sendAction(447, i.getId() - 1, i.getSlot(), 3214);
				// sleep(1500);
				// return;
			}

			if (i.getId() == other()) {
				Menu.sendAction(870, i.getId() - 1, i.getSlot(), 3214);
				sleep(1000);
				return;
			}

		}
	}

	public void makeUnf() {
		final Player me = Players.getMyPlayer();

		if (me.getAnimation() == 363) {
			lastAnim = System.currentTimeMillis();
			// return;
		}

		if (System.currentTimeMillis() - lastAnim < 2000)
			return;

		if (Game.getOpenBackDialogId() == 2492) {
			Menu.sendAction(315, 2434, 162, 2498);
			sleep(2000);
			return;
		}

		Menu.sendAction(447, herb() - 1, getSlot(herb()), 3214);
		sleep(500);
		Menu.sendAction(870, vial - 1, getSlot(vial), 3214);
		sleep(1000);

	}

	public int getSlot(int itemid) {
		for (Item i : Inventory.getItems()) {
			if (itemid == i.getId()) {
				return i.getSlot();

			}

		}
		return 0;
	}

	public boolean hasBadItem() {
		if (getLevel() >= 48) {
			for (Item i : Inventory.getItems()) {
				if (i.getId() != gp && i.getId() != notedHerb()
						&& i.getId() != notedVial && i.getId() != notedOther()
						&& i.getId() != finished() && i.getId() != herb()
						&& i.getId() != vial && i.getId() != other()
						&& i.getId() != unf()) {
					return true;
				}
			}
		}

		if (getLevel() < 48) {
			for (Item i : Inventory.getItems()) {
				if (i.getId() != gp && i.getId() != herb() && i.getId() != vial
						&& i.getId() != other() && i.getId() != unf()
						&& i.getId() != finished()) {
					return true;
				}
			}
		}

		return false;
	}

	public void dropBadItems() {

		if (bankOpen()) {
			closeBank();
			return;
		}

		if (shopOpen()) {
			closeShop();
			return;
		}

		if (getLevel() >= 48) {
			for (Item i : Inventory.getItems()) {
				if (i.getId() != gp && i.getId() != notedHerb()
						&& i.getId() != notedVial && i.getId() != notedOther()) {
					Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
					sleep(500);
					return;
				}
			}
		}

		/*if (getLevel() < 48) {
			for (Item i : Inventory.getItems()) {
				if (i.getId() != gp && i.getId() != herb() && i.getId() != vial
						&& i.getId() != other() && i.getId() != unf()) {
					Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
					sleep(500);
					return;
				}
			}
		}*/

	}

	public void buyItems() {
		if (!atEdge()) {
			teleEdge();
			return;
		}
		
		if (getLevel() >= 48 && hasBadItem()) {
			dropBadItems();
			return;
		}

		final Npc[] shop = Npcs.getNearest(561);
		Npc fst = shop[0];

		if (Game.getOpenInterfaceId() != 3824) {
			fst.interact(index());
			sleep(fst.getLocation().distanceTo() * 600);
			return;
		}

		if (shopOpen() && fst.getLocation().distanceTo() > 1) {
			// closeShop();
			// return;
		}

		if (getLevel() >= 48) {
			buyNoted();
			return;
		}

		buyUnnoted();
	}

	public void closeShop() {
		if (Game.getOpenInterfaceId() == 3824) {
			Mouse.getInstance().click(462, 40, true);
			sleep(400);
			return;
		}
	}

	public boolean shopOpen() {
		if (Game.getOpenInterfaceId() == 3824)
			return true;

		return false;
	}

	/**
	 * Over level 48 herb buys item in notes. 0 == noted 2 == unnoted
	 */

	public int index() {
		if (getLevel() >= 48)
			return 0;

		return 2;
	}

	public void buyNoted() {
		if (needsHerb()) {
			Menu.sendAction(53, notedHerb() - 1, getHerbSlot(), 3900);
			sleep(1000);
			return;
		}

		if (needsVials()) {
			Menu.sendAction(53, notedVial - 1, 0, 3900);
			sleep(1000);
			return;
		}

		if (needsOther()) {
			Menu.sendAction(53, notedOther() - 1, getOtherSlot(), 3900);
			sleep(1000);
			return;
		}
	}

	public int herb() {
		if (getLevel() < 12) {
			return guam;
		}
		if (getLevel() < 22) {
			return tarromin;
		}
		if (getLevel() < 34) {
			return harralander;
		}
		if (getLevel() < 48) {
			return toadflax;
		}
		if (getLevel() < 81) {
			return irit;
		}

		return toadsFlax;
	}

	public int other() {
		if (getLevel() < 12) {
			return eye;
		}
		if (getLevel() < 22) {
			return limpwurt;
		}
		if (getLevel() < 34) {
			return spiderEggs;
		}
		if (getLevel() < 48) {
			return toadLegs;
		}
		if (getLevel() < 81) {
			return unicornDust;
		}

		return birdsNest;
	}

	public int finished() {
		if (getLevel() < 12) {
			return atkPotion;
		}
		if (getLevel() < 22) {
			return strengthPotion;
		}
		if (getLevel() < 34) {
			return restorePotion;
		}
		if (getLevel() < 48) {
			return agilityPotion;
		}
		if (getLevel() < 81) {
			return superAntiPotion;
		}

		return saraPotion;
	}

	public int unf() {
		if (getLevel() < 12) {
			return unfGuam;
		}
		if (getLevel() < 22) {
			return unfTarromin;
		}
		if (getLevel() < 34) {
			return unfHarralander;
		}
		if (getLevel() < 48) {
			return unfToadflax;
		}
		if (getLevel() < 81) {
			return unfIrit;
		}

		return unfToad;
	}

	public int getHerbSlot() {
		if (herb() == guam) {
			return 18;
		}
		if (herb() == tarromin) {
			return 20;
		}
		if (herb() == harralander) {
			return 21;
		}
		if (herb() == toadflax) {
			return 23;
		}
		if (herb() == irit) {
			return 24;
		}

		return 23;
	}

	public int getOtherSlot() {
		if (other() == eye) {
			return 2;
		}
		if (other() == limpwurt) {
			return 4;
		}
		if (other() == spiderEggs) {
			return 5;
		}
		if (other() == toadLegs) {
			return 7;
		}
		if (other() == unicornDust) {
			return 3;
		}

		return 13;
	}

	public void buyUnnoted() {
		if (!hasHerb()) {
			Menu.sendAction(867, herb() - 1, getHerbSlot(), 3900);
			sleep(500);
			return;
		}

		if (!hasVial()) {
			Menu.sendAction(867, vial - 1, 0, 3900);
			sleep(500);
			return;
		}

		if (!hasOther()) {
			Menu.sendAction(867, other() - 1, getOtherSlot(), 3900);
			sleep(500);
			return;
		}
	}

	public boolean hasFinished() {
		if (Inventory.getCount(finishedPots) > 0)
			return true;

		return false;
	}

	public boolean hasOther() {
		if (Inventory.getCount(other()) > 0)
			return true;

		return false;
	}

	public boolean hasUnf() {
		if (Inventory.getCount(unf()) > 0)
			return true;

		return false;
	}

	public boolean hasVial() {
		if (Inventory.getCount(vial) > 0)
			return true;

		return false;
	}

	public boolean hasHerb() {
		if (Inventory.getCount(herb()) > 0)
			return true;

		return false;
	}

	public void teleEdge() {
		final Player me = Players.getMyPlayer();

		if (me.getAnimation() > 0) {
			sleep(2000);
			return;
		}

		if (Game.getOpenBackDialogId() == 2400) {
			Mouse.getInstance().click(290, 405, true);
			sleep(2000);
		}

		if (Game.getOpenBackDialogId() > 0) {
			Mouse.getInstance().click(295, 445, true);
			sleep(1000);
			return;
		}

		Keyboard.getInstance().sendKeys("::shops");
		sleep(2000);
	}

	public int getLevel() {
		return Skill.values()[15].getLevel();
	}

	public boolean atEdge() {
		final Player me = Players.getMyPlayer();

		if (me.getLocation().getY() > 3450 && me.getLocation().getY() < 3550
				&& me.getLocation().getX() > 3070
				&& me.getLocation().getX() < 3100) {
			return true;
		}
		return false;
	}

	Npc rNPC;

	public boolean atIsland() {
		final Player me = Players.getMyPlayer();

		if (me.getLocation().getX() >= 2500 && me.getLocation().getX() <= 2600) {

			if (me.getLocation().getY() >= 4730
					&& me.getLocation().getX() <= 4800) {
				return true;
			}
		}
		return false;
	}

	public void handleRandom() {
		try {
			if (bankOpen()) {
				closeBank();
				return;
			}

			if (shopOpen()) {
				closeShop();
				return;
			}

			if (atIsland()) {
				for (SceneObject o : SceneObjects.getSceneObjects()) {
					if (o != null) {
						if (o.getId() == 8987) {
							o.interact(0);
							sleep(5000);
						}
					}
				}
				return;
			}

			if (rNPC == null)
				return;

			rNPC.interact(0);
			rTimer = System.currentTimeMillis();
			sleep(3000);
			System.out
					.println("INTERACTING WITH " + rNPC.getDef().getId() + "");

			// return;

		} catch (Exception e) {

		}
	}

	@Override
	public void paint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(6));
		g.drawRect(590, 315, 115, 62);
		g.setStroke(new BasicStroke(0));
		g.setColor(Color.WHITE);
		g.fillRect(590, 315, 115, 62);
		g.setColor(Color.BLACK);
		// g.drawString("GP/HR:  " + perHour(ballsMade * 9000) + "", 595, 310);
		g.drawString("State: " + state + "", 595, 330);
		g.drawString("Level: " + getLevel(), 595, 350);
		g.drawString("Runtime: " + runTime(startTime), 595, 370);
		// TODO Auto-generated method stub

	}

	public String runTime(long i) {
		DecimalFormat nf = new DecimalFormat("00");
		long millis = System.currentTimeMillis() - i;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		return nf.format(hours) + ":" + nf.format(minutes) + ":"
				+ nf.format(seconds);
	}

	public static long startTime = 0;

	public String perHour(int gained) {
		return formatNumber((int) ((gained) * 3600000D / (System
				.currentTimeMillis() - startTime)));
	}

	public String formatNumber(int start) {
		DecimalFormat nf = new DecimalFormat("0.0");
		double i = start;
		if (i >= 1000000) {
			return nf.format((i / 1000000)) + "m";
		}
		if (i >= 1000) {
			return nf.format((i / 1000)) + "k";
		}
		return "" + start;
	}

	public boolean inRandom() {

		try {
			if (atIsland()) {
				return true;
			}

			for (Npc n : Npcs.getNearest(randoms)) {
				if (n != null && n.getLocation().distanceTo() <= 1) {
					return true;
				}
			}

			for (Npc n : Npcs.getNpcs()) {
				if (n.getDef().getId() > 8000
						&& n.getLocation().distanceTo() <= 1) {
					return true;
				}
			}

		} catch (Exception e) {

		}

		return false;
	}

	private int[] randoms = { 410, 3117, 3022, 3351, 409, };

	private static boolean isLoggedIn() {
		try {
			Class<?> c = Loader.getClient().getClass();
			Field f = c.getDeclaredField("aj");
			f.setAccessible(true);
			return (boolean) f.get(Loader.getClient());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void login() {
		Mouse.getInstance().click(new Point(365, 300), true);
		System.out.println("Clicked login!");
		sleep(8000);
	}

	@Override
	public int loop() {
		final Player me = Players.getMyPlayer();
		
		//if (!getName().equalsIgnoreCase("z grip"))
		//	return -1;

		if (startTime == 0) {
			startTime = System.currentTimeMillis();
		}

		if (rTimer > 0) {
			if (System.currentTimeMillis() - rTimer > 50000) {
				rTimer = 0;
			}
		}

		for (Npc n : Npcs.getNpcs()) {
			if (n != null && n.getLocation().distanceTo() <= 1) {
				if (n.getDef().getId() > 8000) {
					rNPC = n;
					// sleep(500);
					Runnable runnable1 = (Runnable) Toolkit.getDefaultToolkit()
							.getDesktopProperty("win.sound.exclamation");

					if (runnable1 != null)
						runnable1.run();
				}
			}
		}

		for (Npc n : Npcs.getNearest(randoms)) {
			if (n != null && n.getLocation().distanceTo() <= 1) {
				rNPC = n;
				// sleep(500);
				Runnable runnable1 = (Runnable) Toolkit.getDefaultToolkit()
						.getDesktopProperty("win.sound.exclamation");

				if (runnable1 != null)
					runnable1.run();
			}
		}

		if (inRandom() && rTimer == 0) {
			handleRandom();
			return 0;

		}

		if (!isLoggedIn()) {
			login();
			return 0;
		}

		for (Item i : Inventory.getItems()) {
			if (i.getId() == 6963) {
				Menu.sendAction(847, i.getId() - 1, i.getSlot(), 3214);
				sleep(1000);
				return 0;
			}

		}

		getState();
		handleState();
		// TODO Auto-generated method stub
		return 0;
	}
}